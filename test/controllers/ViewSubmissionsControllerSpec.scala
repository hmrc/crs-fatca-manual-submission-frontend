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
import models.SubmissionsConstants.FATCA1
import models.{SubmissionCard, SubmissionsConstants, SubmittedReport}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import pages.SubmissionsHistoryPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SubmissionHistoryService
import views.html.ViewSubmissionsView

class ViewSubmissionsControllerSpec extends SpecBase {

  private val submissionCard: SubmissionCard = SubmissionCard(
    isVoided = Some(false),
    messageRefId = "ref1",
    originalMessageRefId = "ref1",
    timeSent = now,
    fileType = FATCA1,
    submissionType = SubmissionsConstants.XML
  )

  private val mappedCards: Map[String, List[SubmissionCard]] = Map("ref1" -> List(submissionCard))
  val service: SubmissionHistoryService                      = mock[SubmissionHistoryService]
  "ViewSubmissions Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(SubmissionsHistoryPage, List(submittedReport)).success.value))
        .overrides(bind[SubmissionHistoryService].toInstance(service))
        .build()
      implicit val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
      running(application) {
        val request = FakeRequest(GET, routes.ViewSubmissionsController.onPageLoad(2018, "fiId", "fiName").url)
        when(service.prepareSubmissionHistoryCards(any(), eqTo(2018))).thenReturn(mappedCards)
        val result = route(application, request).value

        val view = application.injector.instanceOf[ViewSubmissionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(mappedCards, 2018, "fiName")(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ViewSubmissionsController.onPageLoad(2018, "fiId", "fiName").url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
