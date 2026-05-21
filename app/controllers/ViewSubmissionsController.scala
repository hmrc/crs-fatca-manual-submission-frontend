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

import cats.data.OptionT
import cats.data.OptionT.*
import config.FrontendAppConfig
import controllers.actions.*
import models.FiIdentifier
import models.ServiceErrors.NoFiDetailFound
import pages.{FiDetailsPage, SubmissionsHistoryPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{SubmissionHistoryService, ViewFIService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ViewSubmissionsView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewSubmissionsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  setData: DataCreationAction,
  sessionRepository: SessionRepository,
  historyService: SubmissionHistoryService,
  viewFIService: ViewFIService,
  val controllerComponents: MessagesControllerComponents,
  view: ViewSubmissionsView
)(implicit config: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(chosenYear: Int, fiId: String): Action[AnyContent] = (identify andThen getData andThen setData).async {
    implicit request =>
      (for {
        fiName <- liftF(
          Future.successful(
          request.userAnswers.get(FiDetailsPage).map(_.fiName)
          .getOrElse(
            viewFIService.getFIDetail(request.fatcaId, fiId).map(_.FIName)
          )).flatten)
        
          
        fiDetail <- liftF(
          Future.fromTry(request.userAnswers.set(FiDetailsPage, FiIdentifier(fiId, fiName)))
        ) // FiIdentifier to prevent confusion with viewFi models
        _           <- liftF(sessionRepository.set(fiDetail)) // set in session repository (FE database to facilitate view election stuff)
        submissions <- liftF(historyService.getSubmissionHistory(fiId))
        cards           = historyService.prepareSubmissionHistoryCards(submissions.submissionsList, chosenYear)
        currentYear     = LocalDate.now().getYear
        submissionYears = (currentYear - 12 to currentYear).toList.sorted
      } yield Ok(view(cards, chosenYear, fiName, submissionYears, fiId)))
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())).recover{
          case custom_error => 
            logger.error("no fi details")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          
        }
  }

}
