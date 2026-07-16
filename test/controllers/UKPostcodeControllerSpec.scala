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
import connectors.{AddressLookupConnector, DatabaseConnector}
import forms.manual.sponsor.UKPostcodeFormProvider
import models.SubmissionsConstants.CRS
import models.response.{AddressLookup, Country}
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.sponsor.{SponsorNamePage, UKPostcodePage}
import play.api.data.FormError
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.sponsor.UKPostcodeView

import scala.concurrent.Future

class UKPostcodeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new UKPostcodeFormProvider()
  val form         = formProvider()
  val testPostcode = "ZZ1 1ZZ"
  val fieldName    = "value"

  lazy val uKPostcodeRoute = controllers.manual.sponsor.routes.UKPostcodeController.onPageLoad(NormalMode).url

  "UKPostcode Controller" - {

    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
    val sponsorName                 = "Test Sponsor Name"
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(SponsorNamePage(), sponsorName)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, uKPostcodeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UKPostcodeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, sponsorName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.set(UKPostcodePage(), testPostcode).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, uKPostcodeRoute)

        val view = application.injector.instanceOf[UKPostcodeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(testPostcode), NormalMode, sponsorName)(request, messages(application)).toString
      }
    }

    "must show error page when valid data is submitted & connector response no address" in {

      val mockSessionRepository = mock[DatabaseConnector]
      val mockConnector         = mock[AddressLookupConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())
      when(mockConnector.findByPostCode(any())(any(), any())) thenReturn Future.successful(Seq())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[AddressLookupConnector].toInstance(mockConnector),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, uKPostcodeRoute)
            .withFormUrlEncodedBody((fieldName, testPostcode))

        val boundForm = form
          .bind(Map(fieldName -> testPostcode))
          .withError(FormError(fieldName, "No addresses found — enter a different postcode or enter the address manually"))

        val view = application.injector.instanceOf[UKPostcodeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, sponsorName)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted & connector response with address" in {

      val mockSessionRepository = mock[DatabaseConnector]
      val mockConnector         = mock[AddressLookupConnector]
      val addressLookup         = AddressLookup(
        990091234514L,
        Some("2 Other place"),
        None, Some("Some District"),
        None, "Town", Some("County"), "postcode", Some(Country.GB))

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())
      when(mockConnector.findByPostCode(any())(any(), any())) thenReturn Future.successful(Seq(addressLookup))

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[AddressLookupConnector].toInstance(mockConnector),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, uKPostcodeRoute)
            .withFormUrlEncodedBody((fieldName, testPostcode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, uKPostcodeRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UKPostcodeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, sponsorName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, uKPostcodeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, uKPostcodeRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
