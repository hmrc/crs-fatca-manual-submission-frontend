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
import models.SubmissionsConstants.{CRS701, CRSAdditional701, PASSED}
import models.{ReadSubmissionRequest, SubmissionCard, SubmittedReport}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubmissionHistoryService @Inject() (readSubmissionConnector: ReadSubmissionConnector)(implicit ec: ExecutionContext) {

  def getAndMaybeCacheSubmissionHistory(id: String, submissionRequest: ReadSubmissionRequest)(implicit hc: HeaderCarrier): Future[Boolean] =
    readSubmissionConnector
      .getSubmissionsList(submissionRequest)
      .map(
        _ => true
      )

  def prepareSubmissionHistoryCards(submissions: List[SubmittedReport], submissionYear: Int): Map[String, List[SubmissionCard]] =
    submissions
      .filter(_.submissionStatus == PASSED)
      .filter(_.reportingYear.toInt == submissionYear)
      .map(submissionToCardConverter)
      .sortBy(_.timeSent)
      .reverse
      .groupBy(_.originalMessageRefId)

  private def submissionToCardConverter(report: SubmittedReport) =
    SubmissionCard(
      isVoided = report.submissionDeleteStatus,
      messageRefId = report.messageRefId,
      reportingYear = report.reportingYear.toInt,
      originalMessageRefId = report.originalMessageRefId.getOrElse(report.messageRefId),
      timeSent = report.uploadDateTime,
      fileType = if report.originalMessageRefId.isDefined & report.submissionFileType == CRS701 then CRSAdditional701 else report.submissionFileType,
      submissionType = report.submissionType
    )
}
