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

package controllers.manual.cpso

import connectors.DatabaseConnector
import controllers.actions.*
import forms.IndividualNameFormProvider
import models.{IndividualName, Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.IndividualNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.IndividualNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualNameController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  formProvider: IndividualNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IndividualNameView,
  actions: Actions
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdCreation() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val regime                      = reportId.regime.toString.toLowerCase
      val form                        = formProvider(reportId.regime)
      val preparedForm = request.userAnswers.get(IndividualNamePage(request.cpsoId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, regime))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdCreation().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val regime                      = reportId.regime.toString.toLowerCase
      val form                        = formProvider(reportId.regime)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, regime))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(IndividualNamePage(request.cpsoId), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IndividualNamePage(request.cpsoId), mode, updatedAnswers))
        )
  }
}
