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

import controllers.actions.*
import models.ReportId
import models.viewModels.AccountHolderId
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckAnswersUtil
import views.html.manual.accountHolders.AccountHolderCheckAnswersView

import javax.inject.Inject
import scala.concurrent.Future

class AccountHolderCheckAnswersController @Inject() (override val messagesApi: MessagesApi,
                                                     actions: Actions,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: AccountHolderCheckAnswersView,
                                                     util: CheckAnswersUtil
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequiredAndAccountHolderNameRequired() {
    implicit request =>
      implicit val accountHolderId: AccountHolderId = request.accountHolderId
      implicit val reportId: ReportId               = request.reportId

      Ok(view(util.getAccountHolderRows(request.userAnswers)))

  }

  def onSubmit: Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequiredAndAccountHolderNameRequired().async {
    implicit request =>
      Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoad().url))
  }

}
