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
import forms.manual.cpso.ResidentForTaxFormProvider
import models.response.Country
import models.{Countries, Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{CurrentTaxResidentCountryIndexPage, ResidentForTaxPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.ResidentForTaxView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ResidentForTaxController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: ResidentForTaxFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ResidentForTaxView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequiredAndTaxResidentIdCreation() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val form                        = formProvider(reportId.regime)

      val preparedForm = request.userAnswers.get(ResidentForTaxPage(request.currentIndex, request.cpsoId, reportId)) match {
        case None        => form
        case Some(value) => form.fill(value.code)
      }

      Ok(view(preparedForm, mode, request.cpsoName, Countries.allCountries(reportId.regime), reportId.regime.toString.toLowerCase))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequiredAndTaxResidentIdCreation().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val form                        = formProvider(reportId.regime)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode, request.cpsoName, Countries.allCountries(reportId.regime), reportId.regime.toString.toLowerCase))
            ),
          value =>
            for {
              selectedCountry <- Future.successful(Countries.allCountries(reportId.regime).find(_.code == value).get)
              updatedAnswers <- Future
                .fromTry(request.userAnswers.setWithReportId(ResidentForTaxPage(request.currentIndex, request.cpsoId, reportId), selectedCountry))
              updatedAnswersWithCurrentIndex <- Future
                .fromTry(updatedAnswers.setWithReportId(CurrentTaxResidentCountryIndexPage(request.cpsoId, reportId), request.currentIndex))
              _ <- repository.set(updatedAnswersWithCurrentIndex)
            } yield Redirect(navigator.nextPage(ResidentForTaxPage(request.currentIndex, request.cpsoId, reportId), mode, updatedAnswersWithCurrentIndex))
        )
  }
}
