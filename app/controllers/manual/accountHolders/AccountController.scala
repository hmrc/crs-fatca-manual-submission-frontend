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

package controllers.manual.accountHolders

import connectors.DatabaseConnector
import controllers.actions.*
import forms.manual.accountHolders.AccountFormProvider
import models.ServiceErrors.NoFiDetailFound
import models.SubmissionsConstants.CRS
import models.viewModels.{AccountId, Accounts}
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.AccountPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ViewFIService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.AccountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: AccountFormProvider,
  viewFIService: ViewFIService,
  val controllerComponents: MessagesControllerComponents,
  view: AccountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdCreation().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      viewFIService
        .getFIDetail(request.fatcaId, reportId.fiId)
        .map {
          fiDetail =>
            val accounts    = Accounts.existingAccounts(request.userAnswers, reportId)
            val existingIds = accounts.map(_._1).toSet
            val preparedForm = request.userAnswers
              .get(AccountPage(reportId, request.accountHolderId))
              .map(_ intersect existingIds)
              .fold(formProvider(existingIds.toSeq))(formProvider(existingIds.toSeq).fill)
            Ok(view(preparedForm, mode, fiDetail.FIName, Accounts.checkboxOptions(accounts), reportId.regime == CRS))
        }
        .recover {
          case NoFiDetailFound =>
            logger.error(s"Failed to retrieve FIDetail for FiID ${reportId.fiId}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountHolderIdCreation().async {
      implicit request =>

        implicit val reportId: ReportId = request.reportId
        viewFIService
          .getFIDetail(request.fatcaId, reportId.fiId)
          .flatMap {
            fiDetail =>
              val accounts    = Accounts.existingAccounts(request.userAnswers, reportId)
              val existingIds = accounts.map(_._1)

              val form    = formProvider(existingIds)
              val options = Accounts.checkboxOptions(accounts)

              form
                .bindFromRequest()
                .fold(
                  formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiDetail.FIName, options, reportId.regime == CRS))),
                  selectedAccountIds =>
                    for {
                      updatedAnswers <- Future.fromTry(
                        request.userAnswers.setWithReportId(
                          AccountPage(reportId, request.accountHolderId),
                          selectedAccountIds
                        )
                      )
                      _ <- repository.set(updatedAnswers)
                    } yield Redirect(
                      navigator.nextPage(
                        AccountPage(reportId, request.accountHolderId),
                        mode,
                        updatedAnswers
                      )
                    )
                )
          }
          .recover {
            case NoFiDetailFound =>
              logger.error(s"Failed to retrieve FIDetail for FiID ${reportId.fiId}")
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
    }
}
