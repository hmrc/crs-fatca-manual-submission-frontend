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

package controllers.manual.sponsor

import connectors.DatabaseConnector
import controllers.actions.*
import forms.manual.sponsor.TaxResidentCountriesFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.sponsor.{TaxResidentCountriesListPage, TaxResidentCountriesPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.sponsor.TaxResidentCountriesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxResidentCountriesController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: TaxResidentCountriesFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TaxResidentCountriesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndSponsorNameRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      val taxResidentCountries = request.userAnswers
        .get(TaxResidentCountriesListPage())
        .getOrElse(Seq.empty)
        .map(_.country)

      val preparedForm = request.userAnswers.get(TaxResidentCountriesPage()) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, request.sponsorName, taxResidentCountries))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndSponsorNameRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      val taxResidentCountries = request.userAnswers
        .get(TaxResidentCountriesListPage())
        .getOrElse(Seq.empty)
        .map(_.country)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.sponsorName, taxResidentCountries))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(TaxResidentCountriesPage(), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(TaxResidentCountriesPage(), mode, updatedAnswers))
        )
  }
}
