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

package services

import connectors.FatcaVoidConnector
import models.{FatcaCardDetail, FatcaVoidCardModel, UserAnswers, VoidReportDetails}
import pages.SubmissionsHistoryPage
import uk.gov.hmrc.http.HeaderCarrier

import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VoidService @Inject() (fatcaConnector: FatcaVoidConnector) {

  def fatcaVoid(messageRefId: String, fiid: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val request = models.VoidFatcaRequest(messageRefId, fiid)
    fatcaConnector.submit(request)
  }

  def getVoidReportDetails(originalMessageId: String, userAnswers: UserAnswers): Option[VoidReportDetails] =
    for {
      allReports <- userAnswers.get(SubmissionsHistoryPage)
      matchingReports = allReports.filter {
        report =>
          report.originalMessageRefId.contains(originalMessageId) ||
          (report.originalMessageRefId.isEmpty && report.messageRefId == originalMessageId)
      }
      headReport <- matchingReports.headOption
      cardDetails = matchingReports.map {
        report =>
          FatcaCardDetail(
            messageRefId = report.messageRefId,
            dateSent = report.uploadDateTime.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
            dateSentTime = report.uploadDateTime.toLocalTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            reportType = report.regime.toString
          )
      }
    } yield VoidReportDetails(FatcaVoidCardModel(cardDetails), headReport.fiName, headReport.fiId, headReport.reportingYear)

}
