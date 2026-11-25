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

package controllers.manual.cpso

import controllers.actions.*
import forms.manual.cpso.ControllingPersonsOrSubstantialOwnersFormProvider

import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.DoYouWantToAddAnotherCpsoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import models.ServiceErrors.NoFiDetailFound
import models.viewModels.UnlinkedAccountHolders
import models.viewModels.UnlinkedAccountHolders.*
import play.api.Logging
import services.{CpsoService, ViewFIService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.ControllingPersonsOrSubstantialOwnersView

import scala.concurrent.{ExecutionContext, Future}

class ControllingPersonsOrSubstantialOwnersController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  formProvider: ControllingPersonsOrSubstantialOwnersFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ControllingPersonsOrSubstantialOwnersView,
  viewFIService: ViewFIService,
  cpsoService: CpsoService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>

      val reportId: ReportId = request.reportId
      val form               = formProvider(reportId.regime)

      viewFIService
        .getFIDetail(request.fatcaId, reportId.fiId)
        .map {
          fiDetail =>
            Ok(
              view(
                form,
                mode,
                fiDetail.FIName,
                regimeType = reportId.regime,
                accountHoldersWithoutACpso(request.userAnswers, reportId),
                cpsoService.existingCpsos(request.userAnswers, reportId)
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
      val form                        = formProvider(reportId.regime)

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
                        reportId.regime,
                        accountHoldersWithoutACpso(request.userAnswers, reportId),
                        cpsoService.existingCpsos(request.userAnswers, reportId)
                      )
                    )
                  ),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(DoYouWantToAddAnotherCpsoPage(reportId), value))
                    _              <- repository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(DoYouWantToAddAnotherCpsoPage(reportId), mode, updatedAnswers))
              )
        }
        .recover {
          case NoFiDetailFound =>
            logger.error(s"Failed to retrieve FIDetail for FiID ${reportId.fiId}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }

  }
}
