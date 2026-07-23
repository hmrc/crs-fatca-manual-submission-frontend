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
import forms.manual.sponsor.WhatIsAddressForSponsorFormProvider
import models.SubmissionsConstants.CRS
import models.response.{Address, AddressLookup, Country}
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.sponsor.{AddressLookupPage, SponsorNamePage, WhatIsAddressForSponsorPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.manual.sponsor.WhatIsAddressForSponsorView

import scala.concurrent.Future

class WhatIsAddressForSponsorControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private lazy val whatIsAddressForSponsorRoute = controllers.manual.sponsor.routes.WhatIsAddressForSponsorController.onPageLoad(NormalMode).url

  private val formProvider = new WhatIsAddressForSponsorFormProvider()
  private val form         = formProvider()

  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private val sponsorName = "Test Sponsor Ltd"

  val addressLookup: AddressLookup =
    AddressLookup(200000706253L, Some("1 Address line 1 Road"), None, Some("Address line 2 Road"), None, "Town", Some("County"), "zz11zz", Some(Country.GB))

  private val addresses: Seq[AddressLookup] = Seq(addressLookup)

  private val options: Seq[RadioItem] = addresses.map(
    a => RadioItem(content = Text(s"${a.formatRadios}"), value = Some(s"${a.format}"))
  )

  private val baseAnswers =
    emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(SponsorNamePage(), sponsorName)
      .withPage(AddressLookupPage(), addresses)

  "WhatIsAddressForSponsor Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatIsAddressForSponsorRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsAddressForSponsorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, sponsorName, options)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val savedAddress: Address = addressLookup.toAddress.value

      val userAnswers = baseAnswers.set(WhatIsAddressForSponsorPage(), savedAddress).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatIsAddressForSponsorRoute)

        val view = application.injector.instanceOf[WhatIsAddressForSponsorView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(addressLookup.format), NormalMode, sponsorName, options)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(baseAnswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsAddressForSponsorRoute)
            .withFormUrlEncodedBody(("value", addressLookup.format))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to Journey Recovery when the selected value matches no address lookup" in {

      val application = applicationBuilder(maybeUserAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsAddressForSponsorRoute)
            .withFormUrlEncodedBody(("value", "some-format-not-in-addresses"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsAddressForSponsorRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WhatIsAddressForSponsorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, sponsorName, options)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whatIsAddressForSponsorRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if SponsorNamePage is missing" in {

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .set(AddressLookupPage(), addresses)
        .success
        .value

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, whatIsAddressForSponsorRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if AddressLookupPage is missing" in {

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .set(SponsorNamePage(), sponsorName)
        .success
        .value

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, whatIsAddressForSponsorRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsAddressForSponsorRoute)
            .withFormUrlEncodedBody(("value", "any-value"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
