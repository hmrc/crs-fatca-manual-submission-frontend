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
import forms.manual.cpso.IndividualHaveDateOfBirthFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.cpso.{IndividualName, IndividualOrOrganisation}
import models.viewModels.manual.cpso.CPSOId
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.{CurrentCPSOIdPage, IndividualHaveDateOfBirthPage, IndividualNamePage, IndividualOrOrganisationPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ViewFIService
import views.html.manual.cpso.IndividualHaveDateOfBirthView

import scala.concurrent.Future

class IndividualHaveDateOfBirthControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                 = new IndividualHaveDateOfBirthFormProvider()
  val form                         = formProvider(CRS)
  val mockFiService: ViewFIService = mock[ViewFIService]

  lazy val individualHaveDateOfBirthRoute = controllers.manual.cpso.routes.IndividualHaveDateOfBirthController.onPageLoad(NormalMode).url

  "IndividualHaveDateOfBirth Controller" - {

    val reportId = ReportId(CRS, 2025, None, "TestfiID")
    val cpsoId   = CPSOId("testid")
    val cpsoName = IndividualName("test", "last")
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
      .withPage(IndividualOrOrganisationPage(cpsoId)(reportId), IndividualOrOrganisation.Individual)
      .withPage(IndividualNamePage(cpsoId)(reportId), cpsoName)

    "must return OK and the correct view for a GET - CRS" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()
      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request = FakeRequest(GET, individualHaveDateOfBirthRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualHaveDateOfBirthView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, true, fiDetail.FIName, cpsoName.fullName)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET - FATCA" in {

      val reportId = ReportId(FATCA, 2025, None, "TestfiID")
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(IndividualOrOrganisationPage(cpsoId)(reportId), IndividualOrOrganisation.Individual)
        .withPage(IndividualNamePage(cpsoId)(reportId), cpsoName)

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()
      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request = FakeRequest(GET, individualHaveDateOfBirthRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualHaveDateOfBirthView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, false, fiDetail.FIName, cpsoName.fullName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.set(IndividualHaveDateOfBirthPage(reportId, cpsoId), true).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request = FakeRequest(GET, individualHaveDateOfBirthRoute)

        val view = application.injector.instanceOf[IndividualHaveDateOfBirthView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, true, fiDetail.FIName, cpsoName.fullName)(request, messages(application)).toString
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
            bind[ViewFIService].toInstance(mockFiService)
          )
          .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request =
          FakeRequest(POST, individualHaveDateOfBirthRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[ViewFIService].toInstance(mockFiService))
        .build()

      when(mockFiService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(fiDetail))

      running(application) {
        val request =
          FakeRequest(POST, individualHaveDateOfBirthRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IndividualHaveDateOfBirthView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, true, fiDetail.FIName, cpsoName.fullName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, individualHaveDateOfBirthRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, individualHaveDateOfBirthRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
