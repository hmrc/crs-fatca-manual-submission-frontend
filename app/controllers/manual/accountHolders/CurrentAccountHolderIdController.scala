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

package controllers.manual.accountHolders

import connectors.DatabaseConnector
import controllers.actions.Actions
import models.{NormalMode, ReportId}
import models.requests.ReportIdRequest
import models.viewModels.AccountHolderId
import pages.manual.accountHolders.CurrentAccountHolderIdPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrentAccountHolderIdController @Inject() (
  repository: DatabaseConnector,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onChangeRedirect(accountHolderId: String): Action[AnyContent] = actions.withReportIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      // todo need accountHolder check your answers
      redirectWithValidAccountHolderId(accountHolderId, controllers.routes.UnderConstructionController.onPageLoad())
  }

  def onRemoveRedirect(accountHolderId: String): Action[AnyContent] = actions.withReportIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      redirectWithValidAccountHolderId(accountHolderId, controllers.manual.accountHolders.routes.RemoveAccountHolderController.onPageLoad(NormalMode))

  }

  private def redirectWithValidAccountHolderId(accountHolderId: String, successCall: Call)(implicit
    request: ReportIdRequest[AnyContent],
    reportId: ReportId
  ): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(CurrentAccountHolderIdPage(), AccountHolderId(accountHolderId)))
      _              <- repository.set(updatedAnswers)
    } yield Redirect(successCall)

}
