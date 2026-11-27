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

package controllers.manual.reportdetails

import base.SpecBase
import connectors.DatabaseConnector
import controllers.manual.reportdetails.routes.ReportDetailsCheckAnswersController
import controllers.manual.routes.SendAReportController
import controllers.routes.JourneyRecoveryController
import models.CrsOrFatca.Crs
import models.FiIdentifiers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.FiDetailsPage
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.InternalServerException
import viewmodels.checkAnswers.manual.CheckAnswersViewModel
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ReportDetailsCheckAnswersView

import scala.concurrent.Future

class ReportDetailsCheckAnswersControllerSpec extends SpecBase {

  "ReportDetailsCheckAnswers Controller" - {
    val year   = 2026
    val fiName = "name"
    val fiId   = "TestfiID"
    val list   = SummaryListViewModel(Seq.empty)

    val ua = emptyUserAnswers
      .withPage(FiDetailsPage, FiIdentifiers(fiId, fiName))
      .withPage(ReportingYearPage, year)
      .withPage(CrsOrFatcaPage, Crs)
      .withPage(pages.manual.reportdetails.TypeOfReportPage, models.TypeOfReport.Information)

    "must return OK and the correct view for a GET" in {
      val mockUtil = mock[CheckAnswersViewModel]
      when(mockUtil.getReportDetailsRows(any())(any())).thenReturn(SummaryListViewModel(Seq.empty))

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[CheckAnswersViewModel].toInstance(mockUtil))
        .build()

      running(application) {
        val request = FakeRequest(GET, ReportDetailsCheckAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ReportDetailsCheckAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, fiName)(request, messages(application)).toString
      }
    }

    "onSaveAndContinue" - {

      "must redirect to /send-a-report when saving data succeeds" in {
        val mockDatabaseConnector = mock[DatabaseConnector]

        when(mockDatabaseConnector.get()(any())).thenReturn(Future(None))
        when(mockDatabaseConnector.set(any())(any())).thenReturn(Future.successful(()))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(bind[DatabaseConnector].toInstance(mockDatabaseConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, ReportDetailsCheckAnswersController.onSaveAndContinue().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual SendAReportController.onPageLoad().url

          verify(mockDatabaseConnector).set(any())(any())
        }
      }

      "must recover without saving when the report type is missing" in {
        val connector = mock[DatabaseConnector]
        val answers   = ua.remove(pages.manual.reportdetails.TypeOfReportPage).success.value

        val application = applicationBuilder(Some(answers))
          .overrides(bind[DatabaseConnector].toInstance(connector))
          .build()

        running(application) {
          val result = route(
            application,
            FakeRequest(POST, ReportDetailsCheckAnswersController.onSaveAndContinue().url)
          ).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe JourneyRecoveryController.onPageLoad().url
          verify(connector, org.mockito.Mockito.never()).set(any())(any())
        }
      }

      models.TypeOfReport.values.foreach {
        reportType =>
          s"must persist $reportType against the selected report and preserve other drafts" in {
            val report = models.ReportId(models.SubmissionsConstants.CRS, year, None, fiId)
            val other  = report.copy(fiId = "other-fi")

            val existing = emptyUserAnswers.withPage(
              pages.manual.reportdetails.DraftReportTypePage(other),
              models.TypeOfReport.NilReport
            )

            val connector = mock[DatabaseConnector]

            when(connector.get()(any())).thenReturn(Future.successful(Some(existing)))
            when(connector.set(any())(any())).thenReturn(Future.successful(()))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val answers = ua.withPage(pages.manual.reportdetails.TypeOfReportPage, reportType)

            val application = applicationBuilder(Some(answers))
              .overrides(bind[DatabaseConnector].toInstance(connector))
              .build()

            running(application) {
              val result = route(
                application,
                FakeRequest(POST, ReportDetailsCheckAnswersController.onSaveAndContinue().url)
              ).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe SendAReportController.onPageLoad().url

              val saved = org.mockito.ArgumentCaptor.forClass(classOf[models.UserAnswers])
              verify(connector).set(saved.capture())(any())

              saved.getValue.get(pages.manual.reportdetails.DraftReportTypePage(report)) mustBe Some(reportType)

              saved.getValue.get(pages.manual.reportdetails.DraftReportTypePage(other)) mustBe
                Some(models.TypeOfReport.NilReport)

              saved.getValue.get(pages.manual.FINamePage()(report)) mustBe Some(fiName)
            }
          }
      }

      "must redirect to JourneyRecoveryController when saving data fails" in {
        val mockDatabaseConnector = mock[DatabaseConnector]

        when(mockDatabaseConnector.get()(any())).thenReturn(Future(None))
        when(mockDatabaseConnector.set(any())(any()))
          .thenReturn(Future.failed(new InternalServerException("Unable to save UserAnswer")))

        val application = applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(bind[DatabaseConnector].toInstance(mockDatabaseConnector))
          .build()

        running(application) {
          val request = FakeRequest(POST, ReportDetailsCheckAnswersController.onSaveAndContinue().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
