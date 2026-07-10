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
import controllers.routes
import forms.elections.CRSThresholdsFormProvider
import models.{ElectionsId, FiIdentifiers, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{ElectionsIdPage, FiDetailsPage}
import pages.elections.CRSThresholdsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CRSThresholdsView

import scala.concurrent.Future

class CRSThresholdsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute   = Call("GET", "/foo")
  val testFIName    = "Test FI"
  val reportingYear = 2027

  val formProvider                             = new CRSThresholdsFormProvider()
  val form: Form[Boolean]                      = formProvider()
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  implicit val electionsId: ElectionsId        = ElectionsId(reportingYear, "test-fi-id")

  lazy val cRSThresholdsRoute: String = controllers.elections.routes.CRSThresholdsController.onPageLoad(NormalMode, reportingYear).url

  override def beforeEach(): Unit =
    reset(mockSessionRepository)
    super.beforeEach()

  "CRSThresholds Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers =
        Some(
          emptyUserAnswers.withPage(FiDetailsPage, FiIdentifiers("fiID", "Test FI"))
            withPage (ElectionsIdPage, electionsId)
        )
      )
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
        val request = FakeRequest(GET, cRSThresholdsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CRSThresholdsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, testFIName, reportingYear)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .withPage(FiDetailsPage, FiIdentifiers("fiID", "Test FI"))
        .withPage(CRSThresholdsPage(), true)
        .withPage(ElectionsIdPage, electionsId)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(userAnswers))
        val request = FakeRequest(GET, cRSThresholdsRoute)

        val view = application.injector.instanceOf[CRSThresholdsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, testFIName, reportingYear)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(maybeUserAnswers =
          Some(
            emptyUserAnswers
              .withPage(FiDetailsPage, FiIdentifiers("fiID", "Test FI"))
              .withPage(ElectionsIdPage, electionsId)
          )
        )
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cRSThresholdsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted for year less than 2026" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(maybeUserAnswers =
          Some(
            emptyUserAnswers
              .withPage(FiDetailsPage, FiIdentifiers("fiID", "Test FI"))
              .withPage(ElectionsIdPage, electionsId)
          )
        )
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.elections.routes.CRSThresholdsController.onPageLoad(NormalMode, 2025).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers =
        Some(
          emptyUserAnswers
            .withPage(FiDetailsPage, FiIdentifiers("fiID", "Test FI"))
            .withPage(ElectionsIdPage, electionsId)
        )
      )
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

      running(application) {
        val request =
          FakeRequest(POST, cRSThresholdsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CRSThresholdsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, testFIName, reportingYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, cRSThresholdsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no Fidtails data is found" in {
      val userAnswers = emptyUserAnswers
        .withPage(ElectionsIdPage, electionsId)
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, cRSThresholdsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, cRSThresholdsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no Fidtails data is found" in {
      val userAnswers = emptyUserAnswers
        .withPage(ElectionsIdPage, electionsId)
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, cRSThresholdsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
