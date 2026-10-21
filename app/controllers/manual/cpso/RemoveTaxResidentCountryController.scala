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
import forms.manual.cpso.RemoveTaxResidentCountryFormProvider

import javax.inject.Inject
import models.{Countries, Mode, ReportId, TaxResidentInformation, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{RemoveTaxResidentCountryPage, ResidentForTaxPage, TaxResidentCountriesListPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import connectors.DatabaseConnector
import models.viewModels.manual.cpso.CPSOId
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.RemoveTaxResidentCountryView

import scala.concurrent.{ExecutionContext, Future}

class RemoveTaxResidentCountryController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: RemoveTaxResidentCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveTaxResidentCountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequiredAndTaxResidentIdRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val form                        = formProvider(reportId.regime)

      request.userAnswers.get(ResidentForTaxPage(request.currentIndex, request.cpsoId, reportId)) match {
        case None => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Some(value) =>
          Countries
            .allCountries(reportId.regime)
            .find(_.code == value.code)
            .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())) {
              country =>
                Ok(view(form, mode, country.description, request.cpsoName, reportId.regime.value.toLowerCase))
            }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequiredAndTaxResidentIdRequired().async {
    implicit request =>
      val id                          = request.currentIndex
      implicit val reportId: ReportId = request.reportId
      val form                        = formProvider(reportId.regime)

      request.userAnswers
        .get(ResidentForTaxPage(request.currentIndex, request.cpsoId, reportId))
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
          country =>
            Countries
              .allCountries(reportId.regime)
              .find(_.code == country.code)
              .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
                country =>
                  form
                    .bindFromRequest()
                    .fold(
                      formWithErrors =>
                        Future.successful(BadRequest(view(formWithErrors, mode, country.description, request.cpsoName, reportId.regime.value.toLowerCase))),
                      value =>
                        val taxResidentCountries: Seq[TaxResidentInformation] =
                          request.userAnswers.get(TaxResidentCountriesListPage(request.cpsoId, reportId)).getOrElse(Seq())
                        val updatedTaxResidentCountries = if (value) taxResidentCountries.patch(id, Nil, 1) else taxResidentCountries
                        for {
                          updatedAnswers <- Future
                            .fromTry(request.userAnswers.set(TaxResidentCountriesListPage(request.cpsoId, reportId), updatedTaxResidentCountries))
                          _ <- repository.set(updatedAnswers)
                        } yield redirectWithFlash(value, mode, updatedAnswers, country.description, request.cpsoId)
                    )
              }

        }

  }

  private def redirectWithFlash(value: Boolean, mode: Mode, useranswers: UserAnswers, country: String, cpsoId: CPSOId)(implicit
    reportId: ReportId
  ): Result =
    if value then Redirect(navigator.nextPage(RemoveTaxResidentCountryPage(cpsoId, reportId), mode, useranswers)).flashing("country-removed" -> country)
    else Redirect(navigator.nextPage(RemoveTaxResidentCountryPage(cpsoId, reportId), mode, useranswers))

}
