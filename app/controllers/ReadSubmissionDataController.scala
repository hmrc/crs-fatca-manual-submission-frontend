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

import connectors.ReadSubmissionConnector
import controllers.Execution.trampoline
import controllers.actions.*
import models.ReadSubmissionRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionHistoryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReadSubmissionDataView

import java.time.{LocalDate, Year, ZoneOffset}
import javax.inject.Inject

class ReadSubmissionDataController @Inject() (
                                               override val messagesApi: MessagesApi,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               setData: DataCreationAction,
                                               service: SubmissionHistoryService,
                                               val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(fiid: Option[String]): Action[AnyContent] = (identify andThen getData andThen setData).async {
    implicit request =>
      val defaultYear = Year.now(ZoneOffset.UTC).getValue - 1
      service
        .getSubmissionHistory(request.fatcaId, ReadSubmissionRequest(true, fiid))
        .map {
          _ =>
            Redirect(controllers.routes.ViewSubmissionsController.onPageLoad(defaultYear))
        }
  }

}
