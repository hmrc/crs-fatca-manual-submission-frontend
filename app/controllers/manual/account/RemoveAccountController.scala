/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.manual.account

import connectors.DatabaseConnector
import controllers.actions.Actions
import forms.manual.account.RemoveAccountFormProvider
import models.SubmissionsConstants.CRS
import models.viewModels.{AccountHolderId, AccountId}
import models.{ReportId, UserAnswers}
import pages.manual.account.{AccountDataPage, CurrentAccountIdPage}
import pages.manual.accountHolders.{AccountHoldersPage, AccountPage as AccountHolderAccountsPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.manual.account.AccountsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.RemoveAccountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class RemoveAccountController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  repository: DatabaseConnector,
  formProvider: RemoveAccountFormProvider,
  accountsService: AccountsService,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAccountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountIdRequired() {
      implicit request =>
        val selectedAccount = accountsService
          .orderedAccounts(request.userAnswers, request.reportId)
          .find(_._1 == request.accountId)

        selectedAccount match {
          case Some((_, accountNumber)) =>
            Ok(view(form, accountNumber, request.reportId.regime == CRS))

          case None =>
            logger.error("Missing selected account on remove-account GET. Redirecting to journey recovery.")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountIdRequired().async {
      implicit request =>
        implicit val reportId: ReportId = request.reportId

        val selectedAccount = accountsService
          .orderedAccounts(request.userAnswers, reportId)
          .find(_._1 == request.accountId)

        selectedAccount match {
          case Some((_, accountNumber)) =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, accountNumber, reportId.regime == CRS))),
                confirmed => {
                  val answersAfterRemoval =
                    if (confirmed) removeAccountAndUnlinkHolders(request.userAnswers, request.accountId)
                    else Success(request.userAnswers)

                  for {
                    updatedAnswers <- Future.fromTry(answersAfterRemoval)
                    _              <- repository.set(updatedAnswers)
                  } yield redirectWithFlash(confirmed, accountNumber)
                }
              )

          case None =>
            logger.error("Missing selected account on remove-account POST. Redirecting to journey recovery.")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }

  private def removeAccountAndUnlinkHolders(
    userAnswers: UserAnswers,
    accountId: AccountId
  )(implicit reportId: ReportId): Try[UserAnswers] =
    for {
      withoutAccount <- userAnswers.remove(AccountDataPage(accountId))
      withoutLinks   <- unlinkAccountFromHolders(withoutAccount, accountId)
      updatedAnswers <-
        if (withoutLinks.get(CurrentAccountIdPage()).contains(accountId)) {
          accountsService.clearCurrentAccount(withoutLinks, reportId)
        } else {
          Success(withoutLinks)
        }
    } yield updatedAnswers

  private def unlinkAccountFromHolders(
    userAnswers: UserAnswers,
    accountId: AccountId
  )(implicit reportId: ReportId): Try[UserAnswers] =
    holderIdsLinkedTo(userAnswers, accountId).foldLeft(Try(userAnswers)) {
      (answers, holderId) =>
        answers.flatMap {
          current =>
            val page      = AccountHolderAccountsPage(reportId, holderId)
            val remaining = current.get(page).getOrElse(Set.empty) - accountId

            current.setWithReportId(page, remaining)
        }
    }

  private def holderIdsLinkedTo(
    userAnswers: UserAnswers,
    accountId: AccountId
  )(implicit reportId: ReportId): Seq[AccountHolderId] =
    userAnswers
      .get(AccountHoldersPage())
      .toSeq
      .flatMap(_.accountHolders.keys)
      .map(AccountHolderId.apply)
      .filter {
        holderId =>
          userAnswers.get(AccountHolderAccountsPage(reportId, holderId)).exists(_.contains(accountId))
      }

  private def redirectWithFlash(confirmed: Boolean, accountNumber: String): Result = {
    val redirect = Redirect(routes.AccountsController.onPageLoad())

    if (confirmed) {
      logger.info("Account removal confirmed. Redirecting to accounts.")
      redirect.flashing("account-removed" -> accountNumber)
    } else {
      logger.info("Account removal declined. Redirecting to accounts.")
      redirect
    }
  }
}
