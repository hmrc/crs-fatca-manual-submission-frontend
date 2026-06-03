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
import models.elections.ElectionsSent
import models.elections.RegimeType.CRS
import pages.ElectionsSentPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ElectionsSentView

class ElectionsSentControllerSpec extends SpecBase {
  "ElectionsSent Controller" - {

    "must return OK and the correct view for a GET" in {
      val regime        = CRS
      val reportingYear = 2026
      val testFIName    = "Test FI"
      val enquiryEmail  = "aeoi.enquiries@hmrc.gov.uk"

      val ua = emptyUserAnswers.withPage(ElectionsSentPage, ElectionsSent(regime, reportingYear, testFIName))

      val application = applicationBuilder(userData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.elections.routes.ElectionsSentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ElectionsSentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(regime.toString, testFIName, reportingYear.toString, enquiryEmail)(request, messages(application)).toString
      }
    }

    "must return REDIRECT when ElectionSentPage not present" in {

      val application = applicationBuilder(userData = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.elections.routes.ElectionsSentController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
