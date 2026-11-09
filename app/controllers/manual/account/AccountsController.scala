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
import forms.manual.account.AccountsFormProvider
import models.ServiceErrors.NoFiDetailFound
import models.SubmissionsConstants.CRS
import models.requests.ReportIdRequest
import models.viewModels.AccountId
import models.{NormalMode, ReportId}
import pages.manual.account.{AccountsPage, AddAnotherAccountPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Status
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import services.ViewFIService
import services.manual.account.AccountsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.manual.account.AccountsSummary
import views.html.manual.account.AccountsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  repository: DatabaseConnector,
  formProvider: AccountsFormProvider,
  viewFIService: ViewFIService,
  accountsService: AccountsService,
  val controllerComponents: MessagesControllerComponents,
  view: AccountsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        val preparedForm = request.userAnswers
          .get(AddAnotherAccountPage(request.reportId))
          .fold(form)(form.fill)

        renderPage(preparedForm, Ok)
    }

  def onSubmit(): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        given reportId: ReportId = request.reportId

        val accountIds = request.userAnswers
          .get(AccountsPage(reportId))
          .map(_.accounts.keys.toSeq.map(AccountId.apply))
          .getOrElse(Seq.empty)

        if (accountIds.isEmpty) {
          Future.successful(
            Redirect(routes.AccountsController.onPageLoad())
          )
        } else {
          form
            .bindFromRequest()
            .fold(
              formWithErrors => renderPage(formWithErrors, BadRequest),
              addAnother =>
                for {
                  updatedAnswers <- Future.fromTry(
                    request.userAnswers.setWithReportId(
                      AddAnotherAccountPage(reportId),
                      addAnother
                    )
                  )

                  destination =
                    if (addAnother) {
                      routes.AccountsController.onAddAccount()
                    } else if (
                      accountIds.exists {
                        accountId =>
                          !accountsService.isComplete(
                            updatedAnswers,
                            reportId,
                            accountId
                          )
                      }
                    ) {
                      controllers.manual.routes.SendAReportController.onPageLoad()
                    } else {
                      controllers.routes.UnderConstructionController.onPageLoad()
                    }

                  _ <- repository.set(updatedAnswers)
                } yield Redirect(destination)
            )
        }
    }

  def onAddAccount(): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        for {
          answersWithoutCurrentAccount <- Future.fromTry(
            accountsService.clearCurrentAccount(
              request.userAnswers,
              request.reportId
            )
          )
          updatedAnswers <- Future.fromTry(
            answersWithoutCurrentAccount.remove(
              AddAnotherAccountPage(request.reportId)
            )
          )
          _ <- repository.set(updatedAnswers)
        } yield Redirect(
          routes.HaveNumberController.onPageLoad(NormalMode)
        )
    }

  def onChange(accountId: String): Action[AnyContent] =
    selectAccount(
      accountId,
      routes.AccountCheckAnswersController.onPageLoad()
    )

  def onRemove(accountId: String): Action[AnyContent] =
    selectAccount(
      accountId,
      routes.RemoveAccountController.onPageLoad()
    )

  private def selectAccount(
    accountId: String,
    destination: Call
  ): Action[AnyContent] =
    actions.withReportIdRequired().async {
      implicit request =>
        val accountExists = request.userAnswers
          .get(AccountsPage(request.reportId))
          .exists(_.accounts.contains(accountId))

        if (!accountExists) {
          Future.successful(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )
        } else {
          for {
            updatedAnswers <- Future.fromTry(
              accountsService.selectCurrentAccount(
                request.userAnswers,
                request.reportId,
                AccountId(accountId)
              )
            )
            _ <- repository.set(updatedAnswers)
          } yield Redirect(destination)
        }
    }

  private def renderPage(
    preparedForm: Form[Boolean],
    status: Status
  )(implicit request: ReportIdRequest[AnyContent]): Future[Result] = {

    val accounts = accountsService.orderedAccounts(
      request.userAnswers,
      request.reportId
    )

    viewFIService
      .getFIDetail(request.fatcaId, request.reportId.fiId)
      .map {
        fiDetail =>
          val summaryList = AccountsSummary(
            accounts = accounts,
            isComplete = accountId =>
              accountsService.isComplete(
                request.userAnswers,
                request.reportId,
                accountId
              ),
            changeAction = accountId => routes.AccountsController.onChange(accountId.value),
            removeAction = accountId => routes.AccountsController.onRemove(accountId.value)
          )

          status(
            view(
              form = preparedForm,
              summaryList = summaryList,
              fiName = fiDetail.FIName,
              isCrs = request.reportId.regime == CRS,
              submitAction = routes.AccountsController.onSubmit(),
              addAccountAction = routes.AccountsController.onAddAccount(),
              sendAReportAction = controllers.manual.routes.SendAReportController.onPageLoad()
            )
          )
      }
      .recover {
        case NoFiDetailFound =>
          logger.error(
            s"Failed to retrieve FIDetail for FiID ${request.reportId.fiId}"
          )
          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )
      }
  }
}
