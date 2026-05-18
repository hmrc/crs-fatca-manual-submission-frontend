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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubmissionHistoryService
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class ReadSubmissionDataControllerSpec extends SpecBase {

  val mockService: SubmissionHistoryService = mock[SubmissionHistoryService]

  override def beforeEach(): Unit =
    super.beforeEach()

  "ReadSubmissionData Controller" - {

    "must redirect to view submissions page upon successful call to retrieve submission history" in {
      val mockSessionRepository :SessionRepository = mock[SessionRepository]
      val application = applicationBuilder(userData = Some(emptyUserData))
        .overrides(bind[SubmissionHistoryService].toInstance(mockService))
        .build()
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      running(application) {
        when(mockService.getAndMaybeCacheSubmissionHistory(any(), any())(using any()))
          .thenReturn(Future.successful(true))
        val request = FakeRequest(GET, routes.ReadSubmissionDataController.onPageLoad("fiId", "fiName").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewSubmissionsController.onPageLoad(now.getYear.intValue - 1, "fiId", "fiName").url
      }
    }
    "must return Internal Server Error and the correct view for a GET" in {

      val application = applicationBuilder(userData = Some(emptyUserData))
        .overrides(
          bind[SubmissionHistoryService].toInstance(mockService)
        )
        .build()

      running(application) {
        when(mockService.getAndMaybeCacheSubmissionHistory(any, any)(using any)).thenReturn(Future.failed(InternalServerException("Failed")))
        val request = FakeRequest(GET, routes.ReadSubmissionDataController.onPageLoad("fiId", "fiName").url)
        val result  = route(application, request).get

        val ex = intercept[Throwable] {
          status(result)
        }
        ex mustBe a[InternalServerException]
        ex.getMessage must include("Failed")
      }
    }
  }
}
