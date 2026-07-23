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

package controllers.manual.sponsor

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.manual.sponsor.IsThisAddressForSponsorFormProvider
import models.SubmissionsConstants.CRS
import models.response.{AddressLookup, Country}
import models.{NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.sponsor.{AddressLookupPage, IsThisAddressForSponsorPage, SponsorNamePage, WhatIsAddressForSponsorPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.sponsor.IsThisAddressForSponsorView

import scala.concurrent.Future

class IsThisAddressForSponsorControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val formProvider = new IsThisAddressForSponsorFormProvider()
  private val form         = formProvider()

  private lazy val isThisAddressForSponsorRoute = controllers.manual.sponsor.routes.IsThisAddressForSponsorController.onPageLoad(NormalMode).url

  private val sponsorName                 = "Test Sponsor Ltd"
  implicit private val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private val addressLookup: AddressLookup =
    AddressLookup(200000706253L, Some("1 Address line 1 Road"), None, Some("Address line 2 Road"), None, "Town", Some("County"), "zz11zz", Some(Country.GB))

  private val addresses: Seq[AddressLookup] = Seq(addressLookup)

  private val address = addressLookup.toAddress.value

  private val ua = emptyUserAnswers
    .withPage(ReportIdPage, reportId)
    .withPage(SponsorNamePage(), sponsorName)

  private val uaWithAddress = ua.withPage(AddressLookupPage(), addresses)

  private val uaWithAddressOnly = emptyUserAnswers
    .withPage(ReportIdPage, reportId)
    .withPage(AddressLookupPage(), addresses)

  "IsThisAddressForSponsor Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(uaWithAddress)).build()

      running(application) {
        val request = FakeRequest(GET, isThisAddressForSponsorRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsThisAddressForSponsorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, address, sponsorName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = uaWithAddress.set(IsThisAddressForSponsorPage(), true).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, isThisAddressForSponsorRoute)

        val view = application.injector.instanceOf[IsThisAddressForSponsorView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, address, sponsorName)(request, messages(application)).toString
      }
    }

    "must save the boolean and the address when the user answers Yes, then redirect" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(uaWithAddress))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, isThisAddressForSponsorRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())(any())

        val savedAnswers = captor.getValue
        savedAnswers.get(IsThisAddressForSponsorPage()) mustBe Some(true)
        savedAnswers.get(WhatIsAddressForSponsorPage()) mustBe Some(address)
      }
    }

    "must save the boolean but not the address when the user answers No, then redirect, " in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(uaWithAddress))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, isThisAddressForSponsorRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())(any())

        val savedAnswers = captor.getValue
        savedAnswers.get(IsThisAddressForSponsorPage()) mustBe Some(false)
        savedAnswers.get(WhatIsAddressForSponsorPage()) mustBe None
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(uaWithAddress)).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisAddressForSponsorRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsThisAddressForSponsorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, address, sponsorName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, isThisAddressForSponsorRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if AddressLookupPage is missing" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, isThisAddressForSponsorRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if SponsorNamePage is missing" in {

      val application = applicationBuilder(maybeUserAnswers = Some(uaWithAddressOnly)).build()

      running(application) {
        val request = FakeRequest(GET, isThisAddressForSponsorRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisAddressForSponsorRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
