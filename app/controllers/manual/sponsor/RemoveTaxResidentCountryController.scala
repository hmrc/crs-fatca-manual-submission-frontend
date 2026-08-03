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

import controllers.actions.*
import forms.manual.sponsor.RemoveTaxResidentCountryFormProvider

import javax.inject.Inject
import models.{Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.sponsor.{RemoveTaxResidentCountryPage, SponsorNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import connectors.DatabaseConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.sponsor.RemoveTaxResidentCountryView

import scala.concurrent.{ExecutionContext, Future}

class RemoveTaxResidentCountryController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  formProvider: RemoveTaxResidentCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveTaxResidentCountryView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      request.userAnswers
        .get(SponsorNamePage())
        .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())) {
          sponsorName =>
            val preparedForm = request.userAnswers.get(RemoveTaxResidentCountryPage()) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, mode, sponsorName, country = "Ethopia"))
        }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      request.userAnswers
        .get(SponsorNamePage())
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
          sponsorName =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, sponsorName, country = "Ethopia"))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(RemoveTaxResidentCountryPage(), value))
                    _              <- repository.set(updatedAnswers)
                  } yield redirectWithFlash(value, mode, updatedAnswers)
              )
        }
  }

  private def redirectWithFlash(value: Boolean, mode: Mode, useranswers: UserAnswers, country: String = "")(implicit reportId: ReportId): Result = {
    if value then Redirect(navigator.nextPage(RemoveTaxResidentCountryPage(), mode, useranswers)).flashing("country-removed" -> country)
    else Redirect(navigator.nextPage(RemoveTaxResidentCountryPage(), mode, useranswers))
  }
}
