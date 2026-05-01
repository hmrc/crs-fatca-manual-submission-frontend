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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VoidService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.InformationVoidedView

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, ZoneId}
import javax.inject.Inject

class InformationVoidedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: InformationVoidedView,
  voidService: VoidService,
  clock: Clock
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(originalMessageId: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      voidService
        .getVoidReportDetails(originalMessageId, request.userAnswers)
        .map {
          details =>
            val emails      = Seq("email1@test.com") // TODO: from subscription/FI contacts
            val emailString = formatEmailList(emails)
            val dateTime    = LocalDateTime.now(clock.withZone(ZoneId.of("Europe/London"))).format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma"))
            val allRefIds: Seq[String] = details.cardModel.cardDetailList.map(_.messageRefId)
            val year                   = details.reportingYear
            Ok(view(details.fiName, dateTime, allRefIds, emailString, year))
        }
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def formatEmailList(emails: Seq[String]): String = emails match {
    case Seq(a)       => a
    case Seq(a, b)    => s"$a and $b"
    case init :+ last => init.mkString(", ") + s" and $last"
    case _            => ""
  }
}
