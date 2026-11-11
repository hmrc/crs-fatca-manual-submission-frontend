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
import forms.manual.accountHolders.AccountHolderListFormProvider
import models.ServiceErrors.NoFiDetailFound
import models.viewModels.UnlinkedAccounts
import models.viewModels.UnlinkedAccounts.*
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.DoYouWantToAddAnotherAccountHolder
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AccountHolderService, ViewFIService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.AccountHolderListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountHolderListController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  formProvider: AccountHolderListFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AccountHolderListView,
  viewFIService: ViewFIService,
  accountHolderService: AccountHolderService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      viewFIService
        .getFIDetail(request.fatcaId, reportId.fiId)
        .map {
          fiDetail =>
            Ok(
              view(
                form,
                mode,
                fiDetail.FIName,
                accountHolderService.accountHolders(request.userAnswers),
                accountsWithoutAnAccountHolder(request.userAnswers),
                reportId.regime
              )
            )
        }
        .recover {
          case NoFiDetailFound =>
            logger.error(s"Failed to retrieve FIDetail for FiID ${reportId.fiId}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      viewFIService
        .getFIDetail(request.fatcaId, reportId.fiId)
        .flatMap {
          fiDetail =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(
                        formWithErrors,
                        mode,
                        fiDetail.FIName,
                        accountHolderService.accountHolders(request.userAnswers),
                        accountsWithoutAnAccountHolder(request.userAnswers),
                        reportId.regime
                      )
                    )
                  ),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(DoYouWantToAddAnotherAccountHolder(), value))
                    _              <- repository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(DoYouWantToAddAnotherAccountHolder(), mode, updatedAnswers))
              )
        }
        .recover {
          case NoFiDetailFound =>
            logger.error(s"Failed to retrieve FIDetail for FiID ${reportId.fiId}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }
}
