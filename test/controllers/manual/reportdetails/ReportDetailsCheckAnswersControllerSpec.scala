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
import controllers.routes
import controllers.routes.JourneyRecoveryController
import models.CrsOrFatca.Crs
import models.FiIdentifiers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import pages.FiDetailsPage
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.InternalServerException
import utils.ReportDetailsCheckAnswersUtil
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

    "must return OK and the correct view for a GET" in {
      val mockUtil = mock[ReportDetailsCheckAnswersUtil]
      when(mockUtil.getReportDetailsRows(any())(any())).thenReturn(SummaryListViewModel(Seq.empty))

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[ReportDetailsCheckAnswersUtil].toInstance(mockUtil))
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

      "must redirect to JourneyRecoveryController when backend saving data fails" in {

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

      "must redirect to JourneyRecoveryController when frontend saving data fails" in {

        val mockDatabaseConnector = mock[DatabaseConnector]
        val mockRepository        = mock[SessionRepository]
        when(mockDatabaseConnector.get()(any())).thenReturn(Future(None))
        when(mockDatabaseConnector.set(any())(any())).thenReturn(Future.successful(()))
        when(mockRepository.set(any()))
          .thenReturn(Future.failed(new InternalServerException("Unable to save UserAnswer")))

        val application = applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[DatabaseConnector].toInstance(mockDatabaseConnector),
            bind[SessionRepository].toInstance(mockRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, ReportDetailsCheckAnswersController.onSaveAndContinue().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url

          verify(mockDatabaseConnector).set(any())(any())
          verify(mockRepository).set(any())
        }
      }
    }
  }
}
