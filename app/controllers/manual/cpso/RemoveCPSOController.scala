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
import forms.manual.cpso.RemoveCPSOFormProvider

import javax.inject.Inject
import models.{Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{CPSOPage, RemoveCPSOPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import connectors.DatabaseConnector
import controllers.routes
import models.SubmissionsConstants.CRS
import models.viewModels.manual.cpso.CPSOId
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.RemoveCPSOView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class RemoveCPSOController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: RemoveCPSOFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCPSOView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val form                        = formProvider(reportId.regime)

      request.userAnswers.get(CPSOPage(request.cpsoId)) match {
        case None =>
          logger.error("Missing an cpso")
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        case Some(_) => Ok(view(form, mode, reportId.regime == CRS, request.cpsoName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val form                        = formProvider(reportId.regime)

      request.userAnswers.get(CPSOPage(request.cpsoId)) match {
        case None =>
          logger.error("Missing an cpso")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(_) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, reportId.regime == CRS, request.cpsoName))),
              value =>
                val removedUA =
                  if value then request.userAnswers.remove(CPSOPage(request.cpsoId))
                  else Success(request.userAnswers)
                for {
                  updatedAnswers <- Future.fromTry(removedUA)
                  _              <- repository.set(updatedAnswers)
                } yield redirectWithFlash(value, mode, updatedAnswers, request.cpsoName, request.cpsoId)
            )
      }

  }

  private def redirectWithFlash(value: Boolean, mode: Mode, useranswers: UserAnswers, cpsoName: String, cpsoId: CPSOId)(implicit reportId: ReportId): Result =
    if value then Redirect(navigator.nextPage(RemoveCPSOPage(reportId, cpsoId), mode, useranswers)).flashing("cpso-removed" -> cpsoName)
    else Redirect(navigator.nextPage(RemoveCPSOPage(reportId, cpsoId), mode, useranswers))
}
