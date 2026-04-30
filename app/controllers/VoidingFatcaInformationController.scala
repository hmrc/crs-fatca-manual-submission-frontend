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
import models.{FatcaCardDetail, FatcaVoidCardModel, Mode, NormalMode}
import navigation.Navigator
import pages.VoidingFatcaInformationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.VoidingFatcaInformationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VoidingFatcaInformationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: VoidingFatcaInformationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: VoidingFatcaInformationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form               = formProvider()
  val fiName             = "ABC Bank plc"
  val fatcaCardDetail1   = FatcaCardDetail("GB2026GB-ABC1234567890-FATCA_003_2", "Sent 30 May 2027", "11:59pm", "Amended information for an existing report")
  val fatcaCardDetail2   = FatcaCardDetail("GB2026GB-ABC1234567890-FATCA_003", "Sent 28 May 2027", "9:25am", "New information")
  val fatcaVoidCardModel = FatcaVoidCardModel(Seq(fatcaCardDetail1, fatcaCardDetail2))

  def onPageLoad(): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      Ok(view(form, fiName, fatcaVoidCardModel))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, fiName, fatcaVoidCardModel))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(VoidingFatcaInformationPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(VoidingFatcaInformationPage, NormalMode, updatedAnswers))
        )
  }
}
