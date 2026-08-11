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

package controllers.manual.sponsor

import base.SpecBase
import models.ReportId
import models.SubmissionsConstants.CRS
import pages.ReportIdPage
import pages.manual.FINamePage
import pages.manual.sponsor.HaveSponsorPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.manual.sponsor.CheckAnswersSummary
import views.html.manual.sponsor.CheckAnswersView

class CheckAnswersControllerSpec extends SpecBase {

  "CheckAnswers Controller" - {

    implicit val reportId = ReportId(CRS, 2025, None, "testFiID")
    val fiName            = "testFiName"
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(HaveSponsorPage(), false)
      .withPage(FINamePage(), fiName)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.manual.sponsor.routes.CheckAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CheckAnswersSummary(ua)(messages(application), reportId), fiName)(request, messages(application)).toString
      }
    }
  }
}
