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

package controllers.manual.cpso

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.manual.cpso.IsCpsoSectionCompleteFormProvider
import models.ServiceErrors.NoFiDetailFound
import models.SubmissionsConstants.CRS
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.IsCpsoSectionCompletePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{CpsoService, ViewFIService}
import views.html.manual.cpso.IsCpsoSectionCompleteView

import scala.concurrent.Future

class IsCpsoSectionCompleteControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val mockFiService: ViewFIService = mock[ViewFIService]
  val mockCpsoService: CpsoService = mock[CpsoService]
  val formProvider                 = new IsCpsoSectionCompleteFormProvider()

  val form: Form[Boolean] = formProvider(CRS)

  override def afterEach(): Unit =
    try reset(mockFiService, mockCpsoService)
    finally super.afterEach()

  lazy val isCpsoSectionCompleteRoute: String = controllers.manual.cpso.routes.IsCpsoSectionCompleteController.onPageLoad().url

  def setUpMockCpso(result: Boolean = true): CpsoService = {
    when(mockCpsoService.areAllCpsosComplete(any(), any())).thenReturn(result)
    mockCpsoService
  }

  "IsCpsoSectionComplete Controller" - {

    val ua = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[ViewFIService].toInstance(mockFiService),
          bind[CpsoService].toInstance(setUpMockCpso())
        )
        .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request = FakeRequest(GET, isCpsoSectionCompleteRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsCpsoSectionCompleteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, reportId.regime.value, "fiName")(request, messages(application)).toString
      }
    }

    "must redirect to send report when cpsos are not complete for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[ViewFIService].toInstance(mockFiService),
          bind[CpsoService].toInstance(setUpMockCpso(false))
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, isCpsoSectionCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.routes.SendAReportController.onPageLoad().url
        verify(mockFiService, never()).getFIDetail(any(), any())(using any())
      }
    }

    "must redirect when FIDetail fails to retrieve" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[ViewFIService].toInstance(mockFiService),
          bind[CpsoService].toInstance(setUpMockCpso())
        )
        .build()
      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.failed(NoFiDetailFound))

      running(application) {
        val request = FakeRequest(GET, isCpsoSectionCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.set(IsCpsoSectionCompletePage(reportId), true).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(
          bind[ViewFIService].toInstance(mockFiService),
          bind[CpsoService].toInstance(setUpMockCpso())
        )
        .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request = FakeRequest(GET, isCpsoSectionCompleteRoute)

        val view = application.injector.instanceOf[IsCpsoSectionCompleteView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, reportId.regime.value, "fiName")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository),
            bind[ViewFIService].toInstance(mockFiService),
            bind[CpsoService].toInstance(setUpMockCpso())
          )
          .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request =
          FakeRequest(POST, isCpsoSectionCompleteRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the send report when cpsos are incomplete when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository),
            bind[ViewFIService].toInstance(mockFiService),
            bind[CpsoService].toInstance(setUpMockCpso(false))
          )
          .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request =
          FakeRequest(POST, isCpsoSectionCompleteRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.routes.SendAReportController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[ViewFIService].toInstance(mockFiService),
          bind[CpsoService].toInstance(setUpMockCpso())
        )
        .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request =
          FakeRequest(POST, isCpsoSectionCompleteRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsCpsoSectionCompleteView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, reportId.regime.value, "fiName")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, isCpsoSectionCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, isCpsoSectionCompleteRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
