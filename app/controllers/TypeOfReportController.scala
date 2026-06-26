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

import connectors.DatabaseConnector
import controllers.actions.*
import forms.TypeOfReportFormProvider
import models.Mode
import navigation.ManualSubmissionNavigator
import pages.{FiDetailsPage, TypeOfReportPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TypeOfReportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeOfReportController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TypeOfReportFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TypeOfReportView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(year: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = formProvider(year)
      request.userAnswers
        .get(FiDetailsPage)
        .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)) {
          fiDetail =>
            val preparedForm = request.userAnswers.get(TypeOfReportPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, mode, fiDetail.fiName, year))
        }

  }

  def onSubmit(year: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val fiName = request.userAnswers.get(FiDetailsPage).get.fiName

      formProvider(year)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiName, year))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TypeOfReportPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(TypeOfReportPage, mode, updatedAnswers))
        )
  }
}
