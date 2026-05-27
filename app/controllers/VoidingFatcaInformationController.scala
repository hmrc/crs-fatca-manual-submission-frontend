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
import pages.FiDetailsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SubmissionHistoryService, VoidService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.VoidingFatcaInformationView
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VoidingFatcaInformationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: VoidingFatcaInformationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: VoidingFatcaInformationView,
  voidService: VoidService,
  submissionService: SubmissionHistoryService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

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
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, reportBeingVoided.fiName, reportBeingVoided.cardModel, originalMessageRefId))),
          value =>
            if (value) {
              voidService
                .fatcaVoid(originalMessageRefId, reportBeingVoided.fiId)
                .map(
                  _ => Redirect(controllers.routes.InformationVoidedController.onPageLoad(originalMessageRefId))
                )
            } else {
              Future.successful(
                Redirect(
                  controllers.routes.ViewSubmissionsController
                    .onPageLoad(year = LocalDateTime.now.getYear - 1, fiId = reportBeingVoided.fiId)
                )
              )
            }
        )).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))).flatMap(identity)
  }
}
