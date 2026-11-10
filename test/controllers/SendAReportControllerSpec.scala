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
import controllers.manual.routes.SendAReportController
import models.ReportId
import models.SubmissionsConstants.CRS
import models.viewModels.TaskStatus.*
import models.viewModels.{SendAReportSections, TaskStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.ReportIdPage
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SendAReportService
import views.html.SendAReportView

class SendAReportControllerSpec extends SpecBase {
  "SendAReport Controller" - {
    val reportId = ReportId(CRS, 2025, None, "testFiID")
    val ua       = emptyUserAnswers.withPage(ReportIdPage, reportId)
    val sections = SendAReportSections(
      reportDetails = Some(NotStarted),
      financialInstitutionDetails = Some(Incomplete),
      sponsorDetails = Some(NotStarted),
      filerCategory = Some(NotStarted),
      accounts = Some(NotStarted),
      accountHolders = Some(NoStatus),
      controllingPersons = Some(Completed),
      tbc1 = Some(Incomplete),
      tbc2 = Some(Incomplete),
      userAnswers = emptyUserAnswers,
      reportId = reportId
    )
    val mockService: SendAReportService = mock[SendAReportService]

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[SendAReportService].toInstance(mockService)
        )
        .build()
      running(application) {
        when(mockService.reportingFIStatus(any(), any())).thenReturn(TaskStatus.Incomplete)
        val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SendAReportView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(sections)(request, messages(application)).toString
      }
    }
  }
}
