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
import forms.CRSContractsFormProvider
import models.{NormalMode, UserData}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.CRSContractsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CRSContractsView

import scala.concurrent.Future

class CRSContractsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider          = new CRSContractsFormProvider()
  val form                  = formProvider()
  val testFIName            = "Test FI" // TODO : Require update after integration
  val reportingYear         = "2027" // TODO : Require update after integration
  val mockSessionRepository = mock[SessionRepository]

  lazy val cRSContractsRoute = routes.CRSContractsController.onPageLoad(NormalMode).url

  override def beforeEach(): Unit = reset(mockSessionRepository)
  super.beforeEach()

  "CRSContracts Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userData = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)
        val request = FakeRequest(GET, cRSContractsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CRSContractsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, testFIName, reportingYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswers           = UserData(userAnswersId).set(CRSContractsPage, true).success.value
      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(userAnswers))

      val application = applicationBuilder(userData = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, cRSContractsRoute)

        val view = application.injector.instanceOf[CRSContractsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, testFIName, reportingYear)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.get(any())) thenReturn Future.successful(None)
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userData = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cRSContractsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockSessionRepository.get(any())) thenReturn Future.successful(None)
      val application = applicationBuilder(userData = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, cRSContractsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CRSContractsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, testFIName, reportingYear)(request, messages(application)).toString
      }
    }

  }
}
