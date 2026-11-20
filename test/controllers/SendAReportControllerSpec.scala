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
import models.viewModels.TaskStatus.*
import models.viewModels.{SendAReportSections, TaskStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.ReportIdPage
import pages.manual.sponsor.HaveSponsorPage
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SendAReportService
import services.manual.account.AccountsService
import views.html.SendAReportView

import scala.concurrent.Future

class SendAReportControllerSpec extends SpecBase {
  "SendAReport Controller" - {

    val mockService: SendAReportService     = mock[SendAReportService]
    val mockAccountService: AccountsService = mock[AccountsService]

    "must return OK and the correct view for a GET - FATCA" in {

      val fatcaReportId = ReportId(FATCA, 2025, None, "testFiID")
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, fatcaReportId)
        .withPage(HaveSponsorPage()(fatcaReportId), true)
      val sections = SendAReportSections(
        reportDetails = Some(NotStarted),
        financialInstitutionDetails = Some(Incomplete),
        sponsorDetails = Some(Incomplete),
        filerCategory = Some(Incomplete),
        accounts = Some(Incomplete),
        accountHolders = Some(NoStatus),
        controllingPersons = Some(Completed),
        tbc1 = Some(Incomplete),
        tbc2 = Some(Incomplete),
        userAnswers = ua,
        reportId = fatcaReportId
      )
      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[SendAReportService].toInstance(mockService)
        )
        .build()
      running(application) {
        when(mockService.reportingFIStatus(any(), any())).thenReturn(TaskStatus.Incomplete)
        when(mockService.fillerStatus(any(), any())).thenReturn(TaskStatus.Incomplete)
        when(mockService.sponsorStatus(any(), any())).thenReturn(TaskStatus.Incomplete)
        when(mockService.reportingCPSOCompleteStatus(any(), any())).thenReturn(TaskStatus.Completed)
        when(mockService.accountStatus(any(), any())).thenReturn(Incomplete)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))
        val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SendAReportView]
        status(result) mustBe OK
        contentAsString(result) mustEqual view(sections)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET - CRS" in {

      val reportId = ReportId(CRS, 2025, None, "testFiID")
      val ua       = emptyUserAnswers.withPage(ReportIdPage, reportId)
      val sections = SendAReportSections(
        reportDetails = Some(NotStarted),
        financialInstitutionDetails = Some(Incomplete),
        sponsorDetails = None,
        filerCategory = None,
        accounts = Some(Incomplete),
        accountHolders = Some(NoStatus),
        controllingPersons = Some(Completed),
        tbc1 = Some(Incomplete),
        tbc2 = Some(Incomplete),
        userAnswers = emptyUserAnswers,
        reportId = reportId
      )
      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[SendAReportService].toInstance(mockService),
          bind[AccountsService].toInstance(mockAccountService)
        )
        .build()
      running(application) {
        when(mockService.reportingFIStatus(any(), any())).thenReturn(TaskStatus.Incomplete)
        when(mockService.reportingCPSOCompleteStatus(any(), any())).thenReturn(Completed)
        when(mockService.accountStatus(any(), any())).thenReturn(Incomplete)
        val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SendAReportView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(sections)(request, messages(application)).toString
      }
    }
  }
}
