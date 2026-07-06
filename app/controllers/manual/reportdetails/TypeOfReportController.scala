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

package controllers.manual.reportdetails

import controllers.actions.*
import forms.manual.reportdetails.TypeOfReportFormProvider
import models.Mode
import navigation.ManualSubmissionNavigator
import pages.manual.reportdetails.{ReportingYearPage, TypeOfReportPage}
import pages.FiDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.reportdetails.TypeOfReportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeOfReportController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TypeOfReportFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TypeOfReportView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        fiDetail <- request.userAnswers.get(FiDetailsPage)
        year     <- request.userAnswers.get(ReportingYearPage)
      } yield {
        val form = formProvider(year)

        val preparedForm = request.userAnswers.get(TypeOfReportPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, mode, fiDetail.fiName, year))
      }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (for {
        fiDetail <- request.userAnswers.get(FiDetailsPage)
        year     <- request.userAnswers.get(ReportingYearPage)
      } yield formProvider(year)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiDetail.fiName, year))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TypeOfReportPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPageWithoutReportId(TypeOfReportPage, mode, updatedAnswers))
        )).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)))
  }
}
