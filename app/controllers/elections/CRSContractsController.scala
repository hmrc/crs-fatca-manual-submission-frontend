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
import forms.elections.CRSContractsFormProvider
import models.Mode
import navigation.Navigator
import pages.elections.CRSContractsPage
import pages.FiDetailsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CRSContractsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CRSContractsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  requireData: DataRequiredAction,
  getData: FrontendDataRetrievalAction,
  formProvider: CRSContractsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CRSContractsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, year: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers
        .get(FiDetailsPage)
        .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)) {
          fiDetail =>
            val preparedForm = request.userAnswers.get(CRSContractsPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, mode, fiDetail.fiName, year))
        }
  }

  def onSubmit(mode: Mode, year: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(FiDetailsPage)
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))) {
          fiDetail =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiDetail.fiName, year))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(CRSContractsPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(CRSContractsPage, mode, updatedAnswers, Some(year)))
              )
        }
  }
}
