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

import cats.data.OptionT.liftF
import controllers.actions.*
import models.FiIdentifiers
import pages.FiDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ElectionsService, ViewFIService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ManageElectionsView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManageElectionsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  setData: DataCreationAction,
  val controllerComponents: MessagesControllerComponents,
  view: ManageElectionsView,
  electionsService: ElectionsService,
  viewFIService: ViewFIService,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(year: Int, fiId: String): Action[AnyContent] = (identify andThen getData andThen setData).async {
    implicit request =>
      val currentYear = LocalDate.now().getYear
      val allYears    = currentYear - 12 to currentYear

      (for {
        fiName <- liftF(viewFIService.getFIDetail(request.fatcaId, fiId).map(_.FIName))
        updatedUA <- liftF(
          Future.fromTry(request.userAnswers.set(FiDetailsPage, FiIdentifiers(fiId, fiName)))
        )
        _    <- liftF(sessionRepository.set(updatedUA))
        rows <- liftF(electionsService.getElectionsRows(fiId, year))
      } yield Ok(view(allYears, rows, year, fiName, fiId)))
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        .recover {
          case _ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

        }
  }
}
