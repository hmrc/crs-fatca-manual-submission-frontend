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

package controllers

import cats.data.OptionT.{fromOption, liftF}
import controllers.actions.*
import forms.VoidingFatcaInformationFormProvider
import models.VoidedReportData
import pages.{FiDetailsPage, VoidedReportDataPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ConfirmationEmailRecipientsService, SubmissionHistoryService, VoidService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.VoidingFatcaInformationView

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VoidingFatcaInformationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: VoidingFatcaInformationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: VoidingFatcaInformationView,
  sessionRepository: SessionRepository,
  voidService: VoidService,
  submissionService: SubmissionHistoryService,
  confirmationEmailRecipientsService: ConfirmationEmailRecipientsService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(originalMessageRefId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (for {
        fiDetail          <- fromOption[Future](request.userAnswers.get(FiDetailsPage))
        pastSubmissions   <- liftF(submissionService.getSubmissionHistory(fiDetail.fiId))
        reportBeingVoided <- fromOption[Future](voidService.getVoidFatcaReportDetails(originalMessageRefId, pastSubmissions.submissionsList))
      } yield Ok(view(form, reportBeingVoided.fiName, reportBeingVoided.cardModel, originalMessageRefId)))
        .getOrElse(
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        )
        .recover {
          case _ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }

  def onSubmit(originalMessageRefId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (for {
        fiDetail          <- fromOption[Future](request.userAnswers.get(FiDetailsPage))
        pastSubmissions   <- liftF(submissionService.getSubmissionHistory(fiDetail.fiId))
        reportBeingVoided <- fromOption[Future](voidService.getVoidFatcaReportDetails(originalMessageRefId, pastSubmissions.submissionsList))
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  reportBeingVoided.fiName,
                  reportBeingVoided.cardModel,
                  originalMessageRefId
                )
              )
            ),
          value =>
            if (value) {
              for {
                emails <- confirmationEmailRecipientsService.getEmailRecipients(reportBeingVoided.fiId, request.fatcaId)
                _      <- voidService.fatcaVoid(originalMessageRefId, reportBeingVoided.fiId)
                voidedData = VoidedReportData(
                  messageRefIds = reportBeingVoided.cardModel.cardDetailList.map(_.messageRefId),
                  emails = emails
                )
                updatedAnswers <- Future.fromTry(
                  request.userAnswers.set(VoidedReportDataPage, voidedData)
                )
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                controllers.routes.InformationVoidedController.onPageLoad(originalMessageRefId)
              )
            } else {
              Future.successful(
                Redirect(
                  controllers.routes.ViewSubmissionsController
                    .onPageLoad(year = LocalDateTime.now.getYear - 1, fiId = reportBeingVoided.fiId)
                )
              )
            }
        ))
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        .flatMap(identity)
        .recover {
          case _ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }
}
