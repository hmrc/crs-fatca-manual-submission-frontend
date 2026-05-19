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
import forms.CRSContractsFormProvider
import models.Mode
import navigation.Navigator
import pages.{CRSContractsPage, FiNamePage}
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
  setData: DataCreationAction,
  getData: FrontendDataRetrievalAction,
  formProvider: CRSContractsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CRSContractsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, year: Int): Action[AnyContent] = (identify andThen getData andThen setData) {
    implicit request =>
      request.userData
        .get(FiNamePage)
        .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)) {
          fiName =>
            val preparedForm = request.userData.get(CRSContractsPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, mode, fiName, year))
        }
  }

  def onSubmit(mode: Mode, year: Int): Action[AnyContent] = (identify andThen getData andThen setData).async {
    implicit request =>
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
                    updatedAnswers <- Future.fromTry(request.userData.set(CRSContractsPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(CRSContractsPage, mode, updatedAnswers, Some(year)))
              )
        }
  }
}
