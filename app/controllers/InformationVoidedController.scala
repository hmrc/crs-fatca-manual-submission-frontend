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

import cats.data.OptionT.{fromOption, liftF}
import controllers.actions.*
import models.viewModels.InformationVoidedViewModel
import pages.{FiDetailsPage, SubmissionsHistoryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SubmissionHistoryService, VoidService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.formatTimeVoidSubmitted
import utils.formatEmailList
import views.html.InformationVoidedView

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InformationVoidedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: InformationVoidedView,
  voidService: VoidService,
  submissionService: SubmissionHistoryService,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(originalMessageId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (for {
        fiDetail          <- fromOption[Future](request.userAnswers.get(FiDetailsPage))
        pastSubmissions   <- liftF(submissionService.getSubmissionHistory(fiDetail.fiId))
        reportBeingVoided <- fromOption[Future](voidService.getVoidFatcaReportDetails(originalMessageId, pastSubmissions.submissionsList))
      } yield
        val emails = Seq("email1@test.com") // TODO: [DAC6-4271]

        val infoVoidedViewModel = InformationVoidedViewModel(
          fiName = reportBeingVoided.fiName,
          dateTime = LocalDateTime.now(clock).formatTimeVoidSubmitted,
          messageRefIds = reportBeingVoided.cardModel.cardDetailList.map(_.messageRefId).reverse,
          emailString = formatEmailList(emails),
          fiId = reportBeingVoided.fiId
        )
        Ok(view(infoVoidedViewModel))
      )
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }
}
