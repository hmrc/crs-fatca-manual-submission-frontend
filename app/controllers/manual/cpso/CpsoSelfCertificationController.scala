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
import forms.manual.cpso.CpsoSelfCertificationFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.CpsoSelfCertificationPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.CpsoSelfCertificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CpsoSelfCertificationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: CpsoSelfCertificationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  cpsoCRSOnlyFilterAction: CPSOCRSOnlyFilterAction,
  view: CpsoSelfCertificationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (actions.withReportIdRequiredAndCPSOIdRequired() andThen cpsoCRSOnlyFilterAction) {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val cpsoId                      = request.cpsoId
      val reportingPeriod             = reportId.reportingYear

      val preparedForm =
        request.userAnswers
          .get(CpsoSelfCertificationPage(cpsoId, reportId))
          .fold(form)(form.fill)

      request.userAnswers
        .get(pages.manual.cpso.IndividualNamePage(request.cpsoId)) match {
        case Some(individualName) =>
          val controllingPerson =
            s"${individualName.firstName} ${individualName.lastName}".trim
          Ok(view(preparedForm, mode, reportingPeriod, controllingPerson))
        case None =>
          logger.error("Mandatory individual name is missing from User Answers")
          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (actions.withReportIdRequiredAndCPSOIdRequired() andThen cpsoCRSOnlyFilterAction).async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val reportingPeriod             = reportId.reportingYear
      val cpsoId                      = request.cpsoId
      request.userAnswers
        .get(pages.manual.cpso.IndividualNamePage(request.cpsoId)) match {
        case Some(individualName) =>
          val controllingPerson = s"${individualName.firstName} ${individualName.lastName}".trim
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, reportingPeriod, controllingPerson))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(CpsoSelfCertificationPage(cpsoId, reportId), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(CpsoSelfCertificationPage(cpsoId, reportId), mode, updatedAnswers))
            )
        case None =>
          logger.error("Mandatory individual name is missing from User Answers")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }

  }
}
