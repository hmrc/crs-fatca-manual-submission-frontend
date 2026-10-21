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

package controllers.manual.accountHolders

import controllers.actions.*
import forms.manual.accountHolders.RemoveTaxResidentCountryFormProvider

import javax.inject.Inject
import models.{Countries, Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{RemoveTaxResidentCountryPage, ResidentTaxPage, TaxResidentCountriesListPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import connectors.DatabaseConnector
import models.manual.{RemoveCountryMessage, TaxResidentInformation}
import models.response.Country
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.RemoveTaxResidentCountryView
import models.viewModels.AccountHolderId

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

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderNameRequiredAndTaxResidentIdRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      request.userAnswers.get(ResidentTaxPage(request.currentIndex, request.accountHolderId, reportId)) match {
        case None => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Some(value) =>
          Countries
            .allCountries(reportId.regime)
            .find(_.code == value.code)
            .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())) {
              country =>
                Ok(view(form, mode, country.description, request.accountHolderName, RemoveCountryMessage.getRemoveCountryMessage(value.code)))
            }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderNameRequiredAndTaxResidentIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val id                          = request.currentIndex

      request.userAnswers
        .get(ResidentTaxPage(id, request.accountHolderId, reportId))
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
          country =>
            Countries
              .allCountries(reportId.regime)
              .find(_.code == country.code)
              .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
                country =>
                  val messageType = RemoveCountryMessage.getRemoveCountryMessage(country.code)
                  form
                    .bindFromRequest()
                    .fold(
                      formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, country.description, request.accountHolderName, messageType))),
                      value =>
                        val taxResidentCountries: Seq[TaxResidentInformation] =
                          request.userAnswers.get(TaxResidentCountriesListPage(request.accountHolderId, reportId)).getOrElse(Seq())
                        val updatedTaxResidentCountries = if (value) taxResidentCountries.patch(id, Nil, 1) else taxResidentCountries
                        for {
                          updatedAnswers <- Future
                            .fromTry(request.userAnswers.set(TaxResidentCountriesListPage(request.accountHolderId, reportId), updatedTaxResidentCountries))
                          _ <- repository.set(updatedAnswers)
                        } yield redirectWithFlash(value, mode, updatedAnswers, country.description, request.accountHolderId)
                    )
              }

        }
  }

  private def redirectWithFlash(value: Boolean, mode: Mode, useranswers: UserAnswers, country: String, accountHolderId: AccountHolderId)(implicit
    reportId: ReportId
  ): Result =
    if value then
      Redirect(navigator.nextPage(RemoveTaxResidentCountryPage(reportId, accountHolderId), mode, useranswers)).flashing("country-removed" -> country)
    else Redirect(navigator.nextPage(RemoveTaxResidentCountryPage(reportId, accountHolderId), mode, useranswers))

}
