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
import forms.CarfGrossProceedsFormProvider
import models.{NormalMode, UserData}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.CarfGrossProceedsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CarfGrossProceedsView

import scala.concurrent.Future

class CarfGrossProceedsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val reportingYear         = "2027"
  val fiName                = "Test FI"
  val formProvider          = new CarfGrossProceedsFormProvider()
  val form                  = formProvider(reportingYear)
  val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit =
    reset(mockSessionRepository)
    super.beforeEach()

  lazy val carfGrossProceedsRoute = controllers.elections.routes.CarfGrossProceedsController.onPageLoad(NormalMode).url

  "CarfGrossProceeds Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userData = Some(emptyUserData)).build()

      running(application) {
        val request = FakeRequest(GET, carfGrossProceedsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CarfGrossProceedsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, fiName, reportingYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userData = UserData(userAnswersId).set(CarfGrossProceedsPage, true).success.value

      val application = applicationBuilder(userData = Some(userData))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(userData))
        val request = FakeRequest(GET, carfGrossProceedsRoute)

        val view = application.injector.instanceOf[CarfGrossProceedsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, fiName, reportingYear)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userData = Some(emptyUserData))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, carfGrossProceedsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userData = Some(emptyUserData)).build()

      running(application) {
        val request =
          FakeRequest(POST, carfGrossProceedsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CarfGrossProceedsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, fiName, reportingYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userData = None).build()

      running(application) {
        val request = FakeRequest(GET, carfGrossProceedsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userData = None).build()

      running(application) {
        val request =
          FakeRequest(POST, carfGrossProceedsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
