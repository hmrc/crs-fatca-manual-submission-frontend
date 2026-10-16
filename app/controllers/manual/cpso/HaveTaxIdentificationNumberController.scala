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
import forms.manual.cpso.HaveTaxIdentificationNumberFormProvider

import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{HaveTaxIdentificationNumberPage, ResidentForTaxPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.HaveTaxIdentificationNumberView

import scala.concurrent.{ExecutionContext, Future}

class HaveTaxIdentificationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: HaveTaxIdentificationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: HaveTaxIdentificationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequiredAndTaxResidentIdRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val form                        = formProvider(reportId.regime)

      val preparedForm = request.userAnswers.get(HaveTaxIdentificationNumberPage(request.currentIndex, request.cpsoId, reportId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(ResidentForTaxPage(request.currentIndex, request.cpsoId, reportId)) match {
        case Some(country) =>
          Ok(view(preparedForm, mode, country, request.cpsoName, reportId.regime.value.toLowerCase()))
        case None =>
          logger.error("missing country name")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequiredAndTaxResidentIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val form                        = formProvider(reportId.regime)

      request.userAnswers.get(ResidentForTaxPage(request.currentIndex, request.cpsoId, reportId)) match {
        case Some(country) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, country, request.cpsoName, reportId.regime.value.toLowerCase()))),
              value =>
                for {
                  updatedAnswers <- Future
                    .fromTry(request.userAnswers.setWithReportId(HaveTaxIdentificationNumberPage(request.currentIndex, request.cpsoId, reportId), value))
                  _ <- repository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(HaveTaxIdentificationNumberPage(request.currentIndex, request.cpsoId, reportId), mode, updatedAnswers))
            )
        case None =>
          logger.error("missing country name")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }

  }
}
