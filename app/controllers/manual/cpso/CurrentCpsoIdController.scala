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
import controllers.actions.Actions
import models.ReportId
import models.requests.ReportIdRequest
import models.viewModels.manual.cpso.CPSOId
import pages.manual.cpso.CurrentCPSOIdPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrentCpsoIdController @Inject() (
  repository: DatabaseConnector,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onChangeRedirect(cpsoId: String): Action[AnyContent] = actions.withReportIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      redirectWithValidCpsoId(cpsoId, controllers.routes.UnderConstructionController.onPageLoad()) // todo change when url is available
  }

  def onRemoveRedirect(cpsoId: String): Action[AnyContent] = actions.withReportIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      redirectWithValidCpsoId(cpsoId, controllers.routes.UnderConstructionController.onPageLoad()) // todo change when url is available

  }

  private def redirectWithValidCpsoId(cpsoId: String, successCall: Call)(implicit
    request: ReportIdRequest[AnyContent],
    reportId: ReportId
  ): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(CurrentCPSOIdPage(), CPSOId(cpsoId)))
      _              <- repository.set(updatedAnswers)
    } yield Redirect(successCall)
}
