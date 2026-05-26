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

package controllers.elections

import base.SpecBase
import forms.elections.IsApplyingThresholdsFormProvider
import models.{FiIdentifiers, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{FiDetailsPage, IsApplyingThresholdsPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.IsApplyingThresholdsView

import scala.concurrent.Future

class IsApplyingThresholdsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute                            = Call("GET", "/foo")
  private val year                           = 2020
  val formProvider                           = new IsApplyingThresholdsFormProvider()
  val form: Form[Boolean]                    = formProvider()
  private val fiName                         = "fiName"
  lazy val isApplyingThresholdsRoute: String = controllers.elections.routes.IsApplyingThresholdsController.onPageLoad(NormalMode, year).url

  "IsApplyingThresholds Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userData = Some(emptyUserData.withPage(FiDetailsPage, FiIdentifiers("fiID", fiName)))).build()

      running(application) {
        val request = FakeRequest(GET, isApplyingThresholdsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsApplyingThresholdsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, fiName, year)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userData = emptyUserData
        .set(IsApplyingThresholdsPage, true)
        .success
        .value
        .withPage(FiDetailsPage, FiIdentifiers("fiID", fiName))

      val application = applicationBuilder(userData = Some(userData)).build()

      running(application) {
        val request = FakeRequest(GET, isApplyingThresholdsRoute)

        val view = application.injector.instanceOf[IsApplyingThresholdsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, fiName, year)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userData              = emptyUserData.withPage(FiDetailsPage, FiIdentifiers("fiID", fiName))
      val mockSessionRepository = mock[SessionRepository]

      val application =
        applicationBuilder(userData = Some(userData))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      running(application) {
        val request =
          FakeRequest(POST, isApplyingThresholdsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userData = Some(emptyUserData.withPage(FiDetailsPage, FiIdentifiers("fiID", fiName)))).build()

      running(application) {
        val request =
          FakeRequest(POST, isApplyingThresholdsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsApplyingThresholdsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, fiName, year)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userData = None).build()

      running(application) {
        val request = FakeRequest(GET, isApplyingThresholdsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userData = None).build()

      running(application) {
        val request =
          FakeRequest(POST, isApplyingThresholdsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
