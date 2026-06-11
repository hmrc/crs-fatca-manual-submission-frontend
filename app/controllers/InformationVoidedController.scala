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

package controllers

import controllers.actions.*
import models.viewModels.InformationVoidedViewModel
import pages.{FiDetailsPage, VoidedReportDataPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.formatTimeVoidSubmitted
import utils.formatEmailList
import views.html.InformationVoidedView

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject

class InformationVoidedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: InformationVoidedView,
  clock: Clock
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(originalMessageId: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        fiDetail     <- request.userAnswers.get(FiDetailsPage)
        voidedReport <- request.userAnswers.get(VoidedReportDataPage)
      } yield

        val infoVoidedViewModel = InformationVoidedViewModel(
          fiName = fiDetail.fiName,
          dateTime = LocalDateTime.now(clock).formatTimeVoidSubmitted,
          messageRefIds = voidedReport.messageRefIds.reverse,
          emailString = formatEmailList(voidedReport.emails),
          fiId = fiDetail.fiId
        )
        Ok(view(infoVoidedViewModel))
      )
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  }
}
