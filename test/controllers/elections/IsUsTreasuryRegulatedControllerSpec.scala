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
import forms.elections.IsUsTreasuryRegulatedFormProvider
import models.{NormalMode, UserData}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{FiNamePage, IsUsTreasuryRegulatedPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.IsUsTreasuryRegulatedView

import scala.concurrent.Future

class IsUsTreasuryRegulatedControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute         = Call("GET", "/foo")
  private val fiName      = "fiName"
  private val year        = 2020
  val formProvider        = new IsUsTreasuryRegulatedFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val isUsTreasuryRegulatedRoute: String = controllers.elections.routes.IsUsTreasuryRegulatedController.onPageLoad(NormalMode, year).url

  "IsUsTreasuryRegulated Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userData = Some(emptyUserData.withPage(FiNamePage, fiName))).build()

      running(application) {
        val request = FakeRequest(GET, isUsTreasuryRegulatedRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsUsTreasuryRegulatedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, fiName, year)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserData(userAnswersId)
        .set(IsUsTreasuryRegulatedPage, true)
        .success
        .value
        .set(FiNamePage, fiName)
        .success
        .value

      val application = applicationBuilder(userData = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, isUsTreasuryRegulatedRoute)

        val view = application.injector.instanceOf[IsUsTreasuryRegulatedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, fiName, year)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userData              = emptyUserData.set(FiNamePage, fiName).success.value
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userData = Some(userData))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, isUsTreasuryRegulatedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userData = Some(emptyUserData.withPage(FiNamePage, fiName))).build()
      running(application) {
        val request =
          FakeRequest(POST, isUsTreasuryRegulatedRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsUsTreasuryRegulatedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, fiName, year)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userData = None).build()

      running(application) {
        val request = FakeRequest(GET, isUsTreasuryRegulatedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userData = None).build()

      running(application) {
        val request =
          FakeRequest(POST, isUsTreasuryRegulatedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
