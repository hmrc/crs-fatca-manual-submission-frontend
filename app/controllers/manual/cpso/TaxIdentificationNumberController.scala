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
import forms.manual.cpso.TaxIdentificationNumberFormProvider
import models.manual.cpso.IndividualOrOrganisation.Individual
import models.response.Country
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{IndividualOrOrganisationPage, ResidentForTaxPage, TaxIdentificationNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.TaxIdentificationNumberView
import cats.syntax.apply.*
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: TaxIdentificationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TaxIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequiredAndTaxResidentIdRequired() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      val countryOption                  = request.userAnswers.get(ResidentForTaxPage(request.currentIndex, request.cpsoId, reportId))
      val individualOrOrganizationOption = request.userAnswers.get(IndividualOrOrganisationPage(request.cpsoId))

      countryOption match {
        case Some(country) =>
          val form = formProvider(country, reportId, individualOrOrganizationOption)
          val preparedForm = request.userAnswers.get(TaxIdentificationNumberPage(request.currentIndex, request.cpsoId, reportId)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, mode, request.cpsoName, country, reportId.regime.value.toLowerCase))
        case None =>
          logger.error("missing country name or individualOrOrganization information")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequiredAndTaxResidentIdRequired().async {
    implicit request =>
      implicit val reportId: ReportId    = request.reportId
      val currentIndex                   = request.currentIndex
      val countryOption                  = request.userAnswers.get(ResidentForTaxPage(request.currentIndex, request.cpsoId, reportId))
      val individualOrOrganizationOption = request.userAnswers.get(IndividualOrOrganisationPage(request.cpsoId))

      countryOption match {
        case Some(country) =>
          val form = formProvider(country, reportId, individualOrOrganizationOption)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.cpsoName, country, reportId.regime.value.toLowerCase))),
              value =>
                for {
                  updatedAnswers <- Future
                    .fromTry(request.userAnswers.setWithReportId(TaxIdentificationNumberPage(currentIndex, request.cpsoId, reportId), value))
                  _ <- repository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(TaxIdentificationNumberPage(currentIndex, request.cpsoId, reportId), mode, updatedAnswers))
            )

        case None =>
          logger.error("missing country name or individualOrOrganization information")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
