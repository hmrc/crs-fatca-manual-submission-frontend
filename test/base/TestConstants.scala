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

package base

import models.SubmissionsConstants.{FATCA, FATCA1, PASSED}
import models.{AddressDetails, FIDetail, SubmissionsConstants, SubmittedReport, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

trait TestConstants {

  def now: LocalDateTime = LocalDateTime.now()

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val submittedReport: SubmittedReport = SubmittedReport(
    fiId = "id",
    fiName = "name",
    fileName = "fileName",
    submissionStatus = PASSED,
    uploadDateTime = now,
    regime = FATCA,
    reportingYear = "2016",
    submissionCaseId = "123",
    submissionType = SubmissionsConstants.XML,
    submissionFileType = FATCA1,
    messageRefId = "ref1",
    submissionDeleteStatus = None,
    originalMessageRefId = None
  )

  val addressDetails =
    AddressDetails(AddressLine1 = "line 1", AddressLine2 = None, AddressLine3 = None, AddressLine4 = None, CountryCode = None, PostalCode = None)

  val fiDetail = FIDetail(
    FIID = "fiId",
    FIName = "fiName",
    SubscriptionID = "12312312",
    TINDetails = None,
    GIIN = None,
    IsFIUser = true,
    AddressDetails = addressDetails,
    PrimaryContactDetails = None,
    SecondaryContactDetails = None
  )

  val userAnswersId: String = "id"
}
