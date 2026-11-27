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
import org.mockito.Mockito.{verify, when}
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
        .withPage(pages.manual.reportdetails.DraftReportTypePage(fatcaReportId), models.TypeOfReport.Information)
        .withPage(HaveSponsorPage()(fatcaReportId), true)

      val sections = SendAReportSections(
        financialInstitutionDetails = Some(Incomplete),
        sponsorDetails = Some(Incomplete),
        filerCategory = Some(Incomplete),
        accounts = Some(Incomplete),
        accountHolders = Some(CannotStartYet),
        controllingPersons = Some(Completed),
        userAnswers = ua,
        reportId = fatcaReportId
      )

      when(mockService.reportingFIStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.fillerStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.sponsorStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.cpsoStatus(any(), any())).thenReturn(Some(Completed))
      when(mockService.accountStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.accountHolderStatus(any(), any(), any())).thenReturn(CannotStartYet)
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[SendAReportService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SendAReportView]

        status(result) mustBe OK
        contentAsString(result) mustEqual view(sections)(request, messages(application)).toString

        verify(mockService).accountHolderStatus(ua, fatcaReportId, Incomplete)
      }
    }

    "must return OK and the correct view for a GET - CRS" in {
      val mockService = mock[SendAReportService]

      val reportId = ReportId(CRS, 2025, None, "testFiID")
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(pages.manual.reportdetails.DraftReportTypePage(reportId), models.TypeOfReport.Information)

      val sections = SendAReportSections(
        financialInstitutionDetails = Some(Incomplete),
        sponsorDetails = None,
        filerCategory = None,
        accounts = Some(Incomplete),
        accountHolders = Some(CannotStartYet),
        controllingPersons = Some(Completed),
        userAnswers = ua,
        reportId = reportId
      )

      when(mockService.reportingFIStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.fillerStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.sponsorStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.cpsoStatus(any(), any())).thenReturn(Some(Completed))
      when(mockService.accountStatus(any(), any())).thenReturn(Incomplete)
      when(mockService.accountHolderStatus(any(), any(), any())).thenReturn(CannotStartYet)
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[SendAReportService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, SendAReportController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[SendAReportView]

        status(result) mustBe OK
        contentAsString(result) mustEqual view(sections)(request, messages(application)).toString

        verify(mockService).accountHolderStatus(ua, reportId, Incomplete)
      }
    }

    "must recover when the selected report has no saved report type" in {
      val report = ReportId(CRS, 2026, None, "fi")
      val answers = emptyUserAnswers
        .withPage(ReportIdPage, report)
        .withPage(pages.manual.FINamePage()(report), "Example Bank")
        .withPage(pages.manual.reportdetails.TypeOfReportPage, models.TypeOfReport.Information)
        .withPage(
          pages.manual.reportdetails.DraftReportTypePage(report.copy(fiId = "other-fi")),
          models.TypeOfReport.NilReport
        )

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(answers)))
      val application = applicationBuilder(Some(answers)).build()

      running(application) {
        val result = route(application, FakeRequest(GET, SendAReportController.onPageLoad().url)).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    Seq(CRS, FATCA).foreach {
      regime =>
        s"must render the initial nil report tasks for $regime" in {
          val report = ReportId(regime, 2026, None, "fi")
          val answers = emptyUserAnswers
            .withPage(ReportIdPage, report)
            .withPage(pages.manual.FINamePage()(report), "Example Bank")
            .withPage(pages.manual.reportdetails.DraftReportTypePage(report), models.TypeOfReport.NilReport)

          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(answers)))
          val application = applicationBuilder(Some(answers)).build()

          running(application) {
            val result = route(application, FakeRequest(GET, SendAReportController.onPageLoad().url)).value

            status(result) mustBe OK

            val page = org.jsoup.Jsoup.parse(contentAsString(result))

            page.select("h1").text() mustBe s"Send a $regime nil report for Example Bank for 2026"
            page.select(".govuk-task-list__item").size() mustBe (if (regime == CRS) 1 else 4)
            page.text() must not include "Add accounts details"

            val send = page.select(".govuk-task-list").last().select(".govuk-task-list__item")

            send.select(".govuk-task-list__status").text() mustBe
              (if (regime == CRS) "Incomplete" else "Cannot start yet")

            send.select("a").size() mustBe (if (regime == CRS) 1 else 0)
            page.text() must include("We’ll save this draft until")
          }
        }
    }

    Seq(CRS, FATCA).foreach {
      regime =>
        s"must hide CP/SO for a new information report with no holders for $regime" in {
          val report = ReportId(regime, 2026, None, "fi")
          val answers = emptyUserAnswers
            .withPage(ReportIdPage, report)
            .withPage(pages.manual.FINamePage()(report), "Example Bank")
            .withPage(pages.manual.reportdetails.DraftReportTypePage(report), models.TypeOfReport.Information)

          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(answers)))
          val application = applicationBuilder(Some(answers)).build()

          running(application) {
            val result = route(application, FakeRequest(GET, SendAReportController.onPageLoad().url)).value

            status(result) mustBe OK

            val page = org.jsoup.Jsoup.parse(contentAsString(result))

            page.select("h1").text() mustBe s"Send a $regime report for Example Bank for 2026"
            page.title() must startWith(s"Send a $regime report for the financial institution for 2026")
            page.select(".govuk-task-list__item").size() mustBe (if (regime == CRS) 4 else 6)
            page.select(".govuk-task-list").text() must not include "Controlling persons"
            page.select(".govuk-task-list").text() must not include "Substantial owners"
          }
        }
    }

    "must not let a hidden CP row block Send when the other sections are complete" in {
      val service = mock[SendAReportService]
      val report  = ReportId(CRS, 2026, None, "fi")

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, report)
        .withPage(pages.manual.FINamePage()(report), "Example Bank")
        .withPage(pages.manual.reportdetails.DraftReportTypePage(report), models.TypeOfReport.Information)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(answers)))
      when(service.reportingFIStatus(any(), any())).thenReturn(Completed)
      when(service.accountStatus(any(), any())).thenReturn(Completed)
      when(service.accountHolderStatus(any(), any(), any())).thenReturn(Completed)
      when(service.cpsoStatus(any(), any())).thenReturn(None)

      val application = applicationBuilder(Some(answers))
        .overrides(bind[SendAReportService].toInstance(service))
        .build()

      running(application) {
        val result = route(application, FakeRequest(GET, SendAReportController.onPageLoad().url)).value

        status(result) mustBe OK

        val page = org.jsoup.Jsoup.parse(contentAsString(result))
        val send = page.select(".govuk-task-list").last()

        page.select(".govuk-task-list").text() must not include "Controlling persons"
        send.select(".govuk-task-list__status").text() mustBe "Incomplete"
        send.select("a").attr("href") mustBe controllers.routes.UnderConstructionController.onPageLoad().url

        verify(service).accountHolderStatus(answers, report, Completed)
      }
    }
  }
}