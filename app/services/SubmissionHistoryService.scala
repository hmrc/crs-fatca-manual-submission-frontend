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

import com.google.inject.Inject
import connectors.ReadSubmissionConnector
import models.SubmissionsConstants.PASSED
import models.{ReadSubmissionRequest, ReadSubmissionResponseDetails, SubmissionCard, SubmittedReport, UserAnswers}
import play.api.libs.json.{JsObject, Json}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubmissionHistoryService @Inject() (connector: ReadSubmissionConnector, sessionRepo: SessionRepository)(implicit ec: ExecutionContext) {

  def getSubmissionHistory(id: String, submissionRequest: ReadSubmissionRequest)(implicit hc: HeaderCarrier): Future[Boolean] =
    connector
      .submissionList(submissionRequest)
      .flatMap(
        items => sessionRepo.set(UserAnswers(id, Json.toJson(items).as[JsObject]))
      )

  def prepareSubmissionHistoryCards(submissions: List[SubmittedReport], submissionYear: Int): Map[String, List[SubmissionCard]] = {
    submissions
      .filter(_.submissionStatus == PASSED)
      .filter(_.uploadDateTime.getYear == submissionYear)
      .map(submissionToCardConverter)
      .sortBy(_.timeSent)
      .groupBy(_.originalMessageRefId)
  }

  private def submissionToCardConverter(report: SubmittedReport) =
    SubmissionCard(
      isVoided = report.submissionDeleteStatus,
      messageRefId = report.messageRefId,
      originalMessageRefId = report.originalMessageRefId.getOrElse(report.messageRefId),
      timeSent = report.uploadDateTime,
      messageType = report.submissionFileType
    )
}
