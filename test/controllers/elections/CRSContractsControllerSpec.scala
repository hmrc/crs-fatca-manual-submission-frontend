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
import forms.elections.CRSContractsFormProvider
import models.{FiIdentifiers, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.FiDetailsPage
import pages.elections.CRSContractsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CRSContractsView

import scala.concurrent.Future

class CRSContractsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                             = new CRSContractsFormProvider()
  val form: Form[Boolean]                      = formProvider()
  val testFIName                               = "Test FI" // TODO : Require update after integration
  val reportingYear                            = 2027
  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  lazy val cRSContractsRoute: String = controllers.elections.routes.CRSContractsController.onPageLoad(NormalMode, reportingYear).url

  override def beforeEach(): Unit = reset(mockSessionRepository)
  super.beforeEach()

  "CRSContracts Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers.withPage(FiDetailsPage, FiIdentifiers("fiID", testFIName))))
        .build()

      running(application) {
        val request = FakeRequest(GET, cRSContractsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CRSContractsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, testFIName, reportingYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery Page when FIName not present in UserData" in {

      val application = applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, cRSContractsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CRSContractsPage, true)
        .success
        .value
        .set(FiDetailsPage, FiIdentifiers("fiID", testFIName))
        .success
        .value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
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

    "must redirect to Journey Recovery Page when FIName not present" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cRSContractsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers.withPage(FiDetailsPage, FiIdentifiers("fiID", "Test FI"))))
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
      val application = applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers.withPage(FiDetailsPage, FiIdentifiers("fiID", testFIName))))
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
