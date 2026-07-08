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
import models.viewModels.SendAReportSections
import models.viewModels.TaskStatus.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.ReportIdPage
import pages.manual.FINamePage
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.SendAReportView

import scala.concurrent.Future

class SendAReportControllerSpec extends SpecBase {
  "SendAReport Controller" - {
    val testFiName        = "TestFI"
    implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")
    val frontEndUA        = emptyUserAnswers.withPage(ReportIdPage, reportId)
    val backEndUA         = emptyUserAnswers.withPage(FINamePage(), testFiName)
    val mockRepository    = mock[SessionRepository]

    val sections = SendAReportSections(
      reportDetails = Some(NotStarted),
      financialInstitutionDetails = Some(NotStarted),
      sponsorDetails = Some(NotStarted),
      filerCategory = Some(NotStarted),
      accounts = Some(NotStarted),
      accountHolders = Some(NoStatus),
      controllingPersons = Some(Completed),
      tbc1 = Some(Incomplete),
      tbc2 = Some(Incomplete)
    )
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(maybeUserAnswers = Some(backEndUA))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockRepository)
        )
        .build()

      running(application) {
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(frontEndUA)))
        val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SendAReportView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(sections)(request, messages(application)).toString
      }
    }
  }
}
