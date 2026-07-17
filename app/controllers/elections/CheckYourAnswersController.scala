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

package controllers.elections

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, ElectionIdRequiredAction, FrontendDataRetrievalAction, IdentifierAction}
import controllers.routes
import models.ElectionsId
import pages.FiDetailsPage
import pages.elections.CRSContractsPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{CheckYourAnswersValidatorService, ElectionsService}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.CheckYourAnswersElections
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  validator: CheckYourAnswersValidatorService,
  val controllerComponents: MessagesControllerComponents,
  electionIdRequiredAction: ElectionIdRequiredAction,
  electionsService: ElectionsService,
  view: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(year: Int): Action[AnyContent] = (identify andThen getData andThen requireData andThen electionIdRequiredAction) {
    implicit request =>
      implicit val electionsId = request.electionsId
      validator.validate(request.userAnswers, year) match {
        case Left(redirectUrl) =>
          logger.error("Mandatory values are missing in UA")
          Redirect(
            controllers.elections.routes.ElectionInformationIsMissingController
              .onPageLoad(RedirectUrl(redirectUrl))
          )

        case Right(()) =>
          request.userAnswers.get(FiDetailsPage) match {
            case Some(fiDetails) =>
              val list   = CheckYourAnswersElections(request.userAnswers, year)
              val regime = if (request.userAnswers.get(CRSContractsPage()).isEmpty) "fatca" else "crs"

              Ok(view(list, year, fiDetails, regime))

            case None =>
              logger.error("Unable to find FIDetailsPage")
              Redirect(routes.JourneyRecoveryController.onPageLoad())
          }
      }
  }

  def onSubmit(year: Int): Action[AnyContent] = (identify andThen getData andThen requireData andThen electionIdRequiredAction).async {
    implicit request =>
      implicit val electionsId = request.electionsId
      (for {
        _ <- electionsService.submitAndDeleteElectionData(request.userAnswers, year)
      } yield Redirect(controllers.elections.routes.ElectionsSentController.onPageLoad().url))
        .recover {
          case err =>
            logger.error(s"Failed to process the request $err")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }
}
