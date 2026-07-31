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
import controllers.*
import controllers.actions.*
import forms.SponsorResidentForTaxFormProvider
import models.{Countries, Mode, ReportId, SponsorResidentTaxCountryCodes}
import navigation.ManualSubmissionNavigator
import pages.SponsorResidentForTaxPage
import pages.manual.sponsor.SponsorNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.sponsor.SponsorResidentForTaxView
import play.api.mvc.Results.Redirect

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SponsorResidentForTaxController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  formProvider: SponsorResidentForTaxFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SponsorResidentForTaxView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form                        = formProvider()
  private val journeyRecoveryCall = routes.JourneyRecoveryController.onPageLoad()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      request.userAnswers
        .get(SponsorNamePage())
        .fold(Redirect(journeyRecoveryCall)) {
          sponsorName =>
            //Todo prefill will be implemented in https://jira.tools.tax.service.gov.uk/browse/DAC6-4406 when currentCountryPage is present
            val preparedForm = form
            Ok(view(preparedForm, mode, sponsorName, Countries.all))
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      request.userAnswers
        .get(SponsorNamePage())
        .fold(Future.successful(Redirect(journeyRecoveryCall))) {
          sponsorName =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, sponsorName, Countries.all))),
                value =>
                  val sponsorResidentTaxCountryCodes = request.userAnswers.get(SponsorResidentForTaxPage()).getOrElse(SponsorResidentTaxCountryCodes(Seq()))
                  val updatedSponsorResidentTaxCountryCodes =
                    sponsorResidentTaxCountryCodes.copy(resCountryCodes = sponsorResidentTaxCountryCodes.resCountryCodes :+ value)
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(SponsorResidentForTaxPage(), updatedSponsorResidentTaxCountryCodes))
                    _              <- repository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(SponsorResidentForTaxPage(), mode, updatedAnswers))
              )

        }
  }
}
