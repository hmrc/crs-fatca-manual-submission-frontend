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
import models.SubmissionsConstants.{CRS, FATCA}
import models.viewModels.SendAReportSections
import models.viewModels.TaskStatus.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.ReportIdPage
import pages.manual.sponsor.HaveSponsorPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SendAReportService
import views.html.SendAReportView

import scala.concurrent.Future

class SendAReportControllerSpec extends SpecBase {

  "SendAReport Controller" - {

    "must return OK and the correct view for a GET - FATCA" in {
      val mockService = mock[SendAReportService]

      val fatcaReportId = ReportId(FATCA, 2025, None, "testFiID")
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, fatcaReportId)
        .withPage(HaveSponsorPage()(fatcaReportId), true)

      when(mockService.reportingFIStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.fillerStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.sponsorStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.reportingCPSOCompleteStatus(any(), any())).thenReturn(Completed)
      when(mockService.accountStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.accountHoldersCompleteStatus(any(), any())).thenReturn(Incomplete)
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[SendAReportService].toInstance(mockService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SendAReportView]
        val sections = SendAReportSections(
          reportDetails = Some(NotStarted),
          financialInstitutionDetails = Some(Incomplete),
          sponsorDetails = Some(Incomplete),
          filerCategory = Some(Incomplete),
          accounts = Some(Incomplete),
          accountHolders = Some(Incomplete),
          controllingPersons = Some(Completed),
          tbc1 = Some(Incomplete),
          tbc2 = Some(Incomplete),
          userAnswers = ua,
          reportId = fatcaReportId
        )

        status(result) mustBe OK
        contentAsString(result) mustEqual view(sections)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET - CRS" in {
      val mockService = mock[SendAReportService]

      val reportId = ReportId(CRS, 2025, None, "testFiID")
      val ua       = emptyUserAnswers.withPage(ReportIdPage, reportId)

      when(mockService.reportingFIStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.fillerStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.sponsorStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.reportingCPSOCompleteStatus(any(), any())).thenReturn(Completed)
      when(mockService.accountStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.accountHoldersCompleteStatus(any(), any())).thenReturn(Incomplete)
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[SendAReportService].toInstance(mockService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SendAReportView]
        val sections = SendAReportSections(
          reportDetails = Some(NotStarted),
          financialInstitutionDetails = Some(Incomplete),
          sponsorDetails = None,
          filerCategory = None,
          accounts = Some(Incomplete),
          accountHolders = Some(Incomplete),
          controllingPersons = Some(Completed),
          tbc1 = Some(Incomplete),
          tbc2 = Some(Incomplete),
          userAnswers = ua,
          reportId = reportId
        )

        status(result) mustBe OK
        contentAsString(result) mustEqual view(sections)(request, messages(application)).toString
      }
    }
  }
}
