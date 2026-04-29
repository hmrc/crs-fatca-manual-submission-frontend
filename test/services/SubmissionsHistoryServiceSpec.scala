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
import models.SubmissionsConstants.FATCA1
import models.{ReadSubmissionRequest, ReadSubmissionResponseDetails, SubmissionCard, SubmissionsConstants, SubmittedReport}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import pages.SubmissionsHistoryPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SubmissionHistoryService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ViewSubmissionsView

import scala.concurrent.Future

class SubmissionsHistoryServiceSpec extends SpecBase {

  private val submissionCard: SubmissionCard = SubmissionCard(
    isVoided = Some(false),
    messageRefId = "ref1",
    originalMessageRefId = "ref1",
    timeSent = now,
    fileType = FATCA1,
    submissionType = SubmissionsConstants.XML
  )
  private val submissionRequest = ReadSubmissionRequest(shouldCache = true, fiId = None)
  private val mockConnector: ReadSubmissionConnector = mock[ReadSubmissionConnector]
  private val mappedCards: Map[String, List[SubmissionCard]] = Map("ref1" -> List(submissionCard))
  private val submissionHistorySuccessResponse : ReadSubmissionResponseDetails = ReadSubmissionResponseDetails(List(submittedReport))
  "getAndMaybeCacheSubmissionHistory" must {

    "return list of submissions upon a successful call" in {
      val app = applicationBuilder().overrides(bind[ReadSubmissionConnector].toInstance(mockConnector)).build()
  
      val service: SubmissionHistoryService = app.injector.instanceOf[SubmissionHistoryService]
      running(app) {
        val result = service.getAndMaybeCacheSubmissionHistory("id", submissionRequest)
        when(mockConnector.getSubmissionsList(using submissionRequest)(using any[HeaderCarrier])).thenReturn(Future.successful(submissionHistorySuccessResponse))
      }
    }

//    "must redirect to journey recovery if user answers are empty" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      running(application) {
//        val request = FakeRequest(GET, routes.ViewSubmissionsController.onPageLoad(2018, Some("fiId")).url)
//        val result  = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
//      }
//    }
  }
}
