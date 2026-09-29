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
import forms.manual.cpso.IndividualPlaceOfBirthFormProvider
import models.SubmissionsConstants.CRS
import models.{Countries, Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.IndividualPlaceOfBirthPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.IndividualPlaceOfBirthView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualPlaceOfBirthController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: IndividualPlaceOfBirthFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IndividualPlaceOfBirthView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired() {
    implicit request =>

      val reportId: ReportId = request.reportId
      val preparedForm = request.userAnswers.get(IndividualPlaceOfBirthPage(request.cpsoId, reportId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, reportId.regime == CRS, request.cpsoName, Countries.allCountries(reportId.regime)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, reportId.regime == CRS, request.cpsoName, Countries.allCountries(reportId.regime)))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(IndividualPlaceOfBirthPage(request.cpsoId, reportId), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IndividualPlaceOfBirthPage(request.cpsoId, reportId), mode, updatedAnswers))
        )
  }
}
