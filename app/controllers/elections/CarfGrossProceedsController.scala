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
import forms.CarfGrossProceedsFormProvider
import models.Mode
import navigation.Navigator
import pages.{CarfGrossProceedsPage, FiNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CarfGrossProceedsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CarfGrossProceedsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: CarfGrossProceedsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CarfGrossProceedsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode, year: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userData
        .get(FiNamePage)
        .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)) {
          fiName =>
            val form = formProvider(year.toString)
            val preparedForm = request.userData.get(CarfGrossProceedsPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, mode, fiName, year))
        }
  }

  def onSubmit(mode: Mode, year: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(year.toString)
      request.userData
        .get(FiNamePage)
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))) {
          fiName =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiName, year))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userData.set(CarfGrossProceedsPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(CarfGrossProceedsPage, mode, updatedAnswers, Some(year)))
              )
        }

  }
}
