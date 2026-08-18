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
import forms.manual.sponsor.RemoveTaxResidentCountryFormProvider
import models.response.Country
import models.sponsor.RemoveCountryMessage
import models.{Countries, Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.sponsor.{RemoveTaxResidentCountryPage, SponsorResidentForTaxPage, TaxResidentCountriesListPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.sponsor.RemoveTaxResidentCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveTaxResidentCountryController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  actions: Actions,
  formProvider: RemoveTaxResidentCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveTaxResidentCountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndSponsorNameRequiredAndIdCreation() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      request.userAnswers.get(SponsorResidentForTaxPage(request.currentId)) match {
        case None => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Some(value) =>
          Countries.all
            .find(_.code == value.code)
            .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())) {
              country =>
                val messageType = RemoveCountryMessage.getRemoveCountryMessage(value.code)
                Ok(view(form, mode, request.sponsorName, country.description, messageType))
            }
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndSponsorNameRequiredAndIdCreation().async {
    implicit request =>
      val id                          = request.currentId
      implicit val reportId: ReportId = request.reportId
      request.userAnswers
        .get(SponsorResidentForTaxPage(request.currentId))
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
          country =>
            Countries.all
              .find(_.code == country.code)
              .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
                country =>
                  val messageType = RemoveCountryMessage.getRemoveCountryMessage(country.code)
                  form
                    .bindFromRequest()
                    .fold(
                      formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.sponsorName, country.description, messageType))),
                      value =>
                        val taxResidentCountries: Seq[Country] = request.userAnswers.get(TaxResidentCountriesListPage()).getOrElse(Seq())
                        val updatedTaxResidentCountries        = if (value) taxResidentCountries.patch(id, Nil, 1) else taxResidentCountries
                        for {
                          updatedAnswers <- Future.fromTry(request.userAnswers.set(TaxResidentCountriesListPage(), updatedTaxResidentCountries))
                          _              <- repository.set(updatedAnswers)
                        } yield redirectWithFlash(value, mode, updatedAnswers, country.description)
                    )
              }

        }
  }

  private def redirectWithFlash(value: Boolean, mode: Mode, useranswers: UserAnswers, country: String)(implicit reportId: ReportId): Result =
    if value then Redirect(navigator.nextPage(RemoveTaxResidentCountryPage(), mode, useranswers)).flashing("country-removed" -> country)
    else Redirect(navigator.nextPage(RemoveTaxResidentCountryPage(), mode, useranswers))
}
