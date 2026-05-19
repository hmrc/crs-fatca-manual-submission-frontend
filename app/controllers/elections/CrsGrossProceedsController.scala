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
import forms.CrsGrossProceedsFormProvider
import models.Mode
import navigation.Navigator
import pages.CrsGrossProceedsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CrsGrossProceedsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CrsGrossProceedsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: CrsGrossProceedsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CrsGrossProceedsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val reportingYear = "2027" // TODO : Will be updated once we integrate in DAC6-4282
  val fiName        = "Test FI" // TODO : Will be updated once we integrate in DAC6-4282
  val form          = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userData.get(CrsGrossProceedsPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, fiName, reportingYear))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiName, reportingYear))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userData.set(CrsGrossProceedsPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(CrsGrossProceedsPage, mode, updatedAnswers, None))
        )
  }
}
