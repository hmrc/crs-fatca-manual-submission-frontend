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

import controllers.actions.*
import forms.VoidingFatcaInformationFormProvider
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VoidService
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
  voidService: VoidService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(originalMessageRefId: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      voidService
        .getVoidFatcaReportDetails(originalMessageRefId, request.userAnswers)
        .map(
          details => Ok(view(form, details.fiName, details.cardModel, originalMessageRefId))
        )
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(originalMessageRefId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      voidService
        .getVoidFatcaReportDetails(originalMessageRefId, request.userAnswers)
        .map {
          details =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, details.fiName, details.cardModel, originalMessageRefId))),
                value =>
                  for {
                    _ <- if (value) voidService.fatcaVoid(originalMessageRefId, details.fiId) else Future.unit
                  } yield
                    if (value) {
                      Redirect(controllers.routes.InformationVoidedController.onPageLoad(originalMessageRefId))
                    } else {
                      Redirect(
                        controllers.routes.ViewSubmissionsController
                          .onPageLoad(year = LocalDateTime.now.getYear - 1, fiId = details.fiId, fiName = details.fiName)
                      )
                    }
              )
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}
