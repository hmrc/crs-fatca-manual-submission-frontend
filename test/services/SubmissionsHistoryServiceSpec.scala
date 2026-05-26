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

import base.SpecBase
import connectors.ReadSubmissionConnector
import models.SubmissionsConstants.{CRS701, CRSAdditional701, FAILED, FATCA1}
import models.{ReadSubmissionResponseDetails, SubmissionsConstants, SubmittedReport}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.Helpers.*
import uk.gov.hmrc.http.InternalServerException

import java.time.LocalDateTime
import scala.concurrent.Future

class SubmissionsHistoryServiceSpec extends SpecBase {

  private val lastYear: LocalDateTime                                         = now.minusYears(1)
  private val nextYear: LocalDateTime                                         = now.plusYears(1)
  private val mockConnector: ReadSubmissionConnector                          = mock[ReadSubmissionConnector]
  private val submissionHistorySuccessResponse: ReadSubmissionResponseDetails = ReadSubmissionResponseDetails(List(submittedReport))
  "getAndMaybeCacheSubmissionHistory" - {

    "return list of submissions upon a successful call" in {
      val app                               = applicationBuilder().overrides(bind[ReadSubmissionConnector].toInstance(mockConnector)).build()
      val service: SubmissionHistoryService = app.injector.instanceOf[SubmissionHistoryService]
      running(app) {
        when(mockConnector.getSubmissionsList(eqTo("id"))(using any)).thenReturn(Future.successful(submissionHistorySuccessResponse))
        val result = service.getSubmissionHistory("id")
        result.futureValue mustEqual submissionHistorySuccessResponse
      }
    }
    "return failed response if the call to BE fails" in {
      val app                               = applicationBuilder().overrides(bind[ReadSubmissionConnector].toInstance(mockConnector)).build()
      val service: SubmissionHistoryService = app.injector.instanceOf[SubmissionHistoryService]
      running(app) {
        when(mockConnector.getSubmissionsList(eqTo("id"))(using any))
          .thenReturn(Future.failed(InternalServerException("Unable to retrieve submission history")))
        val result = service.getSubmissionHistory("id")
        result.failed.futureValue mustBe a[InternalServerException]
        result.failed.futureValue.getMessage must include("Unable to retrieve submission history")
      }
    }
  }

  "prepareSubmissionHistoryCards" - {

    val app                               = applicationBuilder().build()
    val service: SubmissionHistoryService = app.injector.instanceOf[SubmissionHistoryService]
    running(app) {

      "return empty map when submissions list is empty" in {
        val result = service.prepareSubmissionHistoryCards(List(), 2016)
        result mustEqual Map()
      }

      "filter out submissions with status other than PASSED" in {
        val failedReport = submittedReport.copy(submissionStatus = FAILED)
        val result       = service.prepareSubmissionHistoryCards(List(failedReport, submittedReport), 2016)
        result.size mustEqual 1
        result("ref1").length mustEqual 1
      }

      "filter out submissions from different years" in {
        val oldReport = submittedReport.copy(reportingYear = lastYear.getYear.toString)
        val result    = service.prepareSubmissionHistoryCards(List(oldReport, submittedReport), 2016)
        result.size mustEqual 1
        result("ref1").length mustEqual 1
      }

      "group submissions by originalMessageRefId" in {
        val report1 = submittedReport.copy(messageRefId = "msg1", originalMessageRefId = Some("orig1"))
        val report2 = submittedReport.copy(messageRefId = "msg2", originalMessageRefId = Some("orig1"))
        val result  = service.prepareSubmissionHistoryCards(List(report1, report2), 2016)
        result.size mustEqual 1
        result("orig1").length mustEqual 2
      }

      "use messageRefId as key when originalMessageRefId is None" in {
        val report = submittedReport.copy(messageRefId = "msg1", originalMessageRefId = None)
        val result = service.prepareSubmissionHistoryCards(List(report), 2016)
        result.keys.toList must contain("msg1")
      }

      "sort submissions by timeSent in descending order (newest first)" in {
        val report1 = submittedReport.copy(messageRefId = "msg1", uploadDateTime = now.minusHours(2))
        val report2 = submittedReport.copy(messageRefId = "msg2", uploadDateTime = now)
        val report3 = submittedReport.copy(messageRefId = "msg3", uploadDateTime = now.minusHours(1))

        val result = service.prepareSubmissionHistoryCards(List(report1, report2, report3), 2016)

        val allCards = result.values.flatten.toList

        allCards.head.messageRefId mustEqual "msg2"
        allCards(1).messageRefId mustEqual "msg1"
        allCards(2).messageRefId mustEqual "msg3"
      }

      "convert SubmittedReport to SubmissionCard correctly" in {
        val report = submittedReport.copy(
          submissionDeleteStatus = Some(true),
          messageRefId = "ref123",
          originalMessageRefId = None,
          submissionFileType = FATCA1,
          submissionType = SubmissionsConstants.XML
        )
        val result = service.prepareSubmissionHistoryCards(List(report), 2016)

        val card = result("ref123").head
        card.isVoided mustBe Some(true)
        card.messageRefId mustEqual "ref123"
        card.originalMessageRefId mustEqual "ref123"
        card.fileType mustEqual FATCA1
        card.submissionType mustEqual SubmissionsConstants.XML
      }

      "set fileType to CRSAdditional701 when originalMessageRefId is defined and fileType is CRS701" in {
        val report = submittedReport.copy(
          messageRefId = "msg1",
          originalMessageRefId = Some("orig1"),
          submissionFileType = CRS701
        )
        val result = service.prepareSubmissionHistoryCards(List(report, submittedReport), 2016)

        result("orig1").head.fileType mustEqual CRSAdditional701
      }

      "keep fileType as is when originalMessageRefId is None despite being CRS701" in {
        val report = submittedReport.copy(
          messageRefId = "msg1",
          originalMessageRefId = None,
          submissionFileType = CRS701
        )
        val result = service.prepareSubmissionHistoryCards(List(report), 2016)

        result("msg1").head.fileType mustEqual CRS701
      }

      "handle multiple submissions with different years correctly" in {
        val currentYearReport = submittedReport.copy(reportingYear = now.getYear.toString)
        val nextYearReport    = submittedReport.copy(reportingYear = nextYear.getYear.toString)
        val lastYearReport    = submittedReport.copy(reportingYear = lastYear.getYear.toString)

        val result = service.prepareSubmissionHistoryCards(
          List(currentYearReport, nextYearReport, lastYearReport),
          now.getYear
        )

        result.size mustEqual 1
        result("ref1").length mustEqual 1
      }

      "handle complex scenario with multiple groupings and sorting" in {
        lazy val now = LocalDateTime.now()
        val report1 = submittedReport.copy(
          messageRefId = "msg1",
          originalMessageRefId = Some("group1"),
          uploadDateTime = now.minusHours(3)
        )
        val report2 = submittedReport.copy(
          messageRefId = "msg2",
          originalMessageRefId = Some("group1"),
          uploadDateTime = now
        )
        val report3 = submittedReport.copy(
          messageRefId = "msg3",
          originalMessageRefId = Some("group2"),
          uploadDateTime = now.minusHours(1)
        )

        val result = service.prepareSubmissionHistoryCards(
          List(report1, report2, report3),
          2016
        )

        result.size mustEqual 2
        result("group1").length mustEqual 2
        result("group1")(0).timeSent mustEqual now
        result("group1")(1).timeSent mustEqual now.minusHours(3)
        result("group2")(0).timeSent mustEqual now.minusHours(1)
      }
    }
  }

}
