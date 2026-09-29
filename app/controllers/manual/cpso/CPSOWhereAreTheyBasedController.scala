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
import forms.manual.cpso.CPSOWhereAreTheyBasedFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{CPSOWhereAreTheyBasedPage, IndividualNamePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.CPSOWhereAreTheyBasedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CPSOWhereAreTheyBasedController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: CPSOWhereAreTheyBasedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CPSOWhereAreTheyBasedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      request.userAnswers.get(IndividualNamePage(request.cpsoId)) match {
        case Some(name) =>
          val preparedForm = request.userAnswers.get(CPSOWhereAreTheyBasedPage(request.cpsoId, reportId)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode, name.fullName, reportId.regime))
        case None =>
          logger.error("Individual Name value is missing")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      // TODO: ORG NAME SHOULD BE ADDED ONCE IMPLEMENTED
      request.userAnswers.get(IndividualNamePage(request.cpsoId)) match {
        case Some(name) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name.fullName, reportId.regime))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(CPSOWhereAreTheyBasedPage(request.cpsoId, reportId), value))
                  _              <- repository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(CPSOWhereAreTheyBasedPage(request.cpsoId, reportId), mode, updatedAnswers))
            )
        case None =>
          logger.error("Individual Name value is missing")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }

  }
}
