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

package controllers.manual.reportdetails

import connectors.DatabaseConnector
import controllers.actions.*
import models.{ReportId, UserAnswers}
import pages.manual.FINamePage
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage}
import pages.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ReportDetailsCheckAnswersUtil
import views.html.ReportDetailsCheckAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReportDetailsCheckAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReportDetailsCheckAnswersView,
  dbConnector: DatabaseConnector,
  sessionRepository: SessionRepository,
  util: ReportDetailsCheckAnswersUtil
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        fiDetail <- request.userAnswers.get(FiDetailsPage)
        year     <- request.userAnswers.get(ReportingYearPage)
        list = util.getReportDetailsRows(request.userAnswers)
      } yield Ok(view(list, fiDetail.fiName)))
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))

  }

  def onSaveAndContinue: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val reportData = for {
        fiDetail   <- request.userAnswers.get(FiDetailsPage)
        crsOrFatca <- request.userAnswers.get(CrsOrFatcaPage)
        year       <- request.userAnswers.get(ReportingYearPage)
      } yield (ReportId(crsOrFatca.toRegime, year, None, fiDetail.fiId), fiDetail.fiName)

      reportData.fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
        (reportId, fiName) =>
          (for {
            dbAnswers         <- dbConnector.get().map(_.getOrElse(UserAnswers(request.fatcaId)))
            uaWithDraftId     <- Future.fromTry(dbAnswers.set(FINamePage()(reportId), fiName))
            updatedFrontEndUA <- Future.fromTry(request.userAnswers.set(ReportIdPage, reportId))
            _                 <- dbConnector.set(uaWithDraftId)
            _                 <- sessionRepository.set(updatedFrontEndUA)
          } yield Redirect(controllers.manual.routes.SendAReportController.onPageLoad().url))
            .recover {
              case err =>
                logger.error("Failed to process onSaveAndContinue request", err)
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
      }
  }

}
