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

import base.SpecBase
import config.FrontendAppConfig
import models.ServiceErrors.NoFiDetailFound
import models.SubmissionsConstants.FATCA1
import models.{ReadSubmissionResponseDetails, SubmissionCard, SubmissionsConstants, SubmittedReport}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{SubmissionHistoryService, ViewFIService}
import uk.gov.hmrc.http.InternalServerException
import views.html.ViewSubmissionsView

import java.time.LocalDate
import scala.concurrent.Future

class ViewSubmissionsControllerSpec extends SpecBase {

  private val submissionCard: SubmissionCard = SubmissionCard(
    isVoided = Some(false),
    messageRefId = "ref1",
    reportingYear = 2016,
    originalMessageRefId = "ref1",
    timeSent = now,
    fileType = FATCA1,
    submissionType = SubmissionsConstants.XML
  )
  private val fiName: String                                 = "fiName"
  private val fiId: String                                   = "fiId"
  private val currentYear                                    = LocalDate.now().getYear
  private val mappedCards: Map[String, List[SubmissionCard]] = Map("ref1" -> List(submissionCard))
  val mockSubmissionService: SubmissionHistoryService        = mock[SubmissionHistoryService]
  val mockFiService: ViewFIService                           = mock[ViewFIService]
  val mockSessionRepository: SessionRepository               = mock[SessionRepository]
  "ViewSubmissions Controller" - {
    val reportStartYear = 2014

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SubmissionHistoryService].toInstance(mockSubmissionService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[ViewFIService].toInstance(mockFiService)
        )
        .build()
      when(mockFiService.getFIDetail(any(), eqTo(fiId))(using any())).thenReturn(Future.successful(fiDetail))
      implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
      running(application) {
        val request = FakeRequest(GET, routes.ViewSubmissionsController.onPageLoad(2016, fiId).url)
        when(mockSubmissionService.getSubmissionHistory(eqTo(fiId))(any())).thenReturn(Future.successful(ReadSubmissionResponseDetails(List(submittedReport))))
        when(mockSubmissionService.prepareSubmissionHistoryCards(any(), eqTo(2016))).thenReturn(mappedCards)
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = route(application, request).value

        val view = application.injector.instanceOf[ViewSubmissionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(mappedCards, 2016, fiName, (reportStartYear to currentYear).toList, fiId)(request,
                                                                                                                         messages(application)
        ).toString
      }
    }

    "must redirect to journey recovery if no FI detail can be fetched" in {
      val application = applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers))
        .overrides(bind[SubmissionHistoryService].toInstance(mockSubmissionService), bind[ViewFIService].toInstance(mockFiService))
        .build()
      when(mockFiService.getFIDetail(any(), eqTo(fiId))(using any())).thenReturn(Future.failed(NoFiDetailFound))
      running(application) {
        val request = FakeRequest(GET, routes.ViewSubmissionsController.onPageLoad(2018, fiId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to journey recovery if call to get submission data fails" in {
      val application = applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SubmissionHistoryService].toInstance(mockSubmissionService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[ViewFIService].toInstance(mockFiService)
        )
        .build()
      when(mockFiService.getFIDetail(any(), eqTo(fiId))(using any())).thenReturn(Future.successful(fiDetail))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockSubmissionService.getSubmissionHistory(eqTo(fiId))(any())).thenReturn(Future.failed(InternalServerException("failed")))
      running(application) {
        val request = FakeRequest(GET, routes.ViewSubmissionsController.onPageLoad(2018, fiId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
