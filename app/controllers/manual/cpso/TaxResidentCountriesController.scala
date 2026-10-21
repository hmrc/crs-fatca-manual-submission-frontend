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

import controllers.actions.*
import forms.manual.cpso.TaxResidentCountriesFormProvider

import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{DoYouWantToAddTaxResidentCountryPage, TaxResidentCountriesListPage, TaxResidentCountriesPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.TaxResidentCountriesView

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

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      val taxResidentCountries = request.userAnswers
        .get(TaxResidentCountriesListPage(request.cpsoId, reportId)) match {
        case Some(countries) => countries.map(_.country.description)
        case None            => Seq.empty
      }

      val preparedForm = request.userAnswers.get(DoYouWantToAddTaxResidentCountryPage(request.cpsoId, reportId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, request.cpsoName, reportId.regime.value.toLowerCase(), taxResidentCountries))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      val taxResidentCountries = request.userAnswers
        .get(TaxResidentCountriesListPage(request.cpsoId, reportId)) match {
        case Some(countries) => countries.map(_.country.description)
        case None            => Seq.empty
      }

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, request.cpsoName, reportId.regime.value.toLowerCase(), taxResidentCountries))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(DoYouWantToAddTaxResidentCountryPage(request.cpsoId, reportId), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(DoYouWantToAddTaxResidentCountryPage(request.cpsoId, reportId), mode, updatedAnswers))
        )
  }
}
