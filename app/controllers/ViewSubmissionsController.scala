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

import config.FrontendAppConfig
import controllers.actions.*
import pages.SubmissionsHistoryPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionHistoryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ViewSubmissionsView

import java.time.LocalDate
import javax.inject.Inject

class ViewSubmissionsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  service: SubmissionHistoryService,
  val controllerComponents: MessagesControllerComponents,
  view: ViewSubmissionsView
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(chosenYear: Int, fiId: String, fiName: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        submissions <- request.userData.get(SubmissionsHistoryPage)
        cards = service.prepareSubmissionHistoryCards(submissions, chosenYear)
        currentYear = LocalDate.now().getYear
        submissionYears = submissions.map(_.reportingYear.toInt).distinct.dropWhile(_ < currentYear - 12).sorted
      } yield {
        logger.info(s"years List = ${submissionYears.toString()}, chosenYear = ${chosenYear.toString}")
        Ok(view(cards, chosenYear, fiName ,submissionYears, fiId))
      })
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  }

}
