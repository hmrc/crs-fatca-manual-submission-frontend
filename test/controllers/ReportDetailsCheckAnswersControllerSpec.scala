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
import models.ReportId
import models.SubmissionsConstants.CRS
import pages.ReportIdPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ReportDetailsCheckAnswersView

class ReportDetailsCheckAnswersControllerSpec extends SpecBase {

  "ReportDetailsCheckAnswers Controller" - {

    val ua   = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS, 2025, None, "testFiID"))
    val list = SummaryListViewModel(Seq.empty)
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReportDetailsCheckAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReportDetailsCheckAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }
  }
}
