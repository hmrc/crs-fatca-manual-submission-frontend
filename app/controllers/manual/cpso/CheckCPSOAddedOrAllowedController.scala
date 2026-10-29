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

import controllers.actions.Actions
import models.viewModels.AccountHolders
import models.viewModels.manual.cpso.ControllingOrSubstantialOwners
import models.{Mode, ReportId}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject

class CheckCPSOAddedOrAllowedController @Inject() (
  actions: Actions,
  val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onChangeRedirect(mode: Mode): Action[AnyContent] = actions.withReportIdRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val haveCpsoBeenAdded           = ControllingOrSubstantialOwners.existingCpsos(request.userAnswers).nonEmpty

      val accountHoldersRequireCpso = AccountHolders
        .existingAccountHolders(request.userAnswers)
        .filter(
          (_, accountHolder) => accountHolder.canHaveCPSO() && accountHolder.organisationName.nonEmpty
        )
        .nonEmpty

      if (haveCpsoBeenAdded) {
        logger.info("redirecting to controlling-persons-or-substantial-owners")
        Redirect(controllers.routes.UnderConstructionController.onPageLoad())
      } else if (accountHoldersRequireCpso) {
        Redirect(controllers.manual.cpso.routes.AccountHolderController.onPageLoad(mode))
      } else {
        logger.error("Account holder does not require controlling person or substantial owner")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }
}
