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
import connectors.DatabaseConnector
import forms.TypeOfReportFormProvider
import models.{FiIdentifiers, NormalMode, TypeOfReport}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{FiDetailsPage, TypeOfReportPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.TypeOfReportView

import scala.concurrent.Future

class TypeOfReportControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute            = Call("GET", "/foo")
  val year                   = 2026
  val fiName                 = "name"
  val fiId                   = "TestfiID"
  lazy val typeOfReportRoute = routes.TypeOfReportController.onPageLoad(year, NormalMode).url

  val formProvider = new TypeOfReportFormProvider()
  val form         = formProvider(year)

  "TypeOfReport Controller" - {
    val ua = emptyUserAnswers.withPage(FiDetailsPage, FiIdentifiers(fiId, fiName))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, typeOfReportRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TypeOfReportView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, fiName, year)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua2         = ua.withPage(TypeOfReportPage, TypeOfReport.values.head)
      val application = applicationBuilder(maybeUserAnswers = Some(ua2)).build()

      running(application) {
        val request = FakeRequest(GET, typeOfReportRoute)

        val view = application.injector.instanceOf[TypeOfReportView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(TypeOfReport.values.head), NormalMode, fiName, year)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, typeOfReportRoute)
            .withFormUrlEncodedBody(("value", TypeOfReport.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, typeOfReportRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TypeOfReportView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, fiName, year)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, typeOfReportRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, typeOfReportRoute)
            .withFormUrlEncodedBody(("value", TypeOfReport.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
