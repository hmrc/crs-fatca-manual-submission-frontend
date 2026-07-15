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

package controllers.elections

import controllers.actions.*
import forms.elections.IsApplyingThresholdsFormProvider
import models.Mode
import navigation.Navigator
import pages.IsApplyingThresholdsPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IsApplyingThresholdsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsApplyingThresholdsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  electionIdRequiredAction: ElectionIdRequiredAction,
  formProvider: IsApplyingThresholdsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsApplyingThresholdsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, year: Int): Action[AnyContent] = (identify andThen getData andThen requireData andThen electionIdRequiredAction) {
    implicit request =>
      println(s"onPageLoad: userAnswers = ${request.userAnswers}")
      implicit val electionsId = request.electionsId

      val preparedForm = request.userAnswers.get(IsApplyingThresholdsPage()) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode, request.fiDetail.fiName, year))
  }

  def onSubmit(mode: Mode, year: Int): Action[AnyContent] = (identify andThen getData andThen requireData andThen electionIdRequiredAction).async {
    implicit request =>
      implicit val electionsId = request.electionsId
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.fiDetail.fiName, year))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IsApplyingThresholdsPage(), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IsApplyingThresholdsPage(), mode, updatedAnswers, Some(year)))
        )
  }
}
