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
import forms.SponsorResidentForTaxFormProvider
import models.SubmissionsConstants.CRS
import models.{NormalMode, ReportId, SponsorResidentTaxCountryCodes, Countries}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.manual.sponsor.SponsorNamePage
import pages.{ReportIdPage, SponsorResidentForTaxPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.sponsor.SponsorResidentForTaxView

import scala.concurrent.Future

class SponsorResidentForTaxControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute                 = Call("GET", "/foo")
  private val sponsorName         = "Test Sponsor"
  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  val formProvider = new SponsorResidentForTaxFormProvider()
  val form         = formProvider()

  lazy val sponsorResidentForTaxRoute = controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode).url

  "SponsorResidentForTax Controller" - {

    val ua = emptyUserAnswers.withPage(ReportIdPage, reportId)

    "must return OK and the correct view for a GET" in {
      val useranswers = ua.withPage(SponsorNamePage(), sponsorName)
      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request = FakeRequest(GET, sponsorResidentForTaxRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SponsorResidentForTaxView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, sponsorName, Countries.all, None)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")

      val userAnswers = ua
        .withPage(SponsorResidentForTaxPage(), SponsorResidentTaxCountryCodes(Seq("GB")))
        .withPage(SponsorNamePage(), sponsorName)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode, Some(0)).url)

        val view = application.injector.instanceOf[SponsorResidentForTaxView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("GB"), NormalMode, sponsorName, Countries.all, Some(0))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers           = ua.withPage(SponsorNamePage(), sponsorName)
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, sponsorResidentForTaxRoute)
            .withFormUrlEncodedBody(("country", "GB"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val useranswers = ua.withPage(SponsorNamePage(), sponsorName)
      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, sponsorResidentForTaxRoute)
            .withFormUrlEncodedBody(("country", ""))

        val boundForm = form.bind(Map("country" -> ""))

        val view = application.injector.instanceOf[SponsorResidentForTaxView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, sponsorName, Countries.all, None)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, sponsorResidentForTaxRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if an invalid idx is passed when sponsor resident is present" in {
      val userAnswers = ua
        .withPage(SponsorResidentForTaxPage(), SponsorResidentTaxCountryCodes(Seq("GB")))
        .withPage(SponsorNamePage(), sponsorName)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode, Some(23)).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if sponsor name is not present" in {
      val userAnswers = ua
        .withPage(ReportIdPage, reportId)
        .withPage(SponsorResidentForTaxPage(), SponsorResidentTaxCountryCodes(Seq("GB")))

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, sponsorResidentForTaxRoute)
            .withFormUrlEncodedBody(("country", "GB"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if sponsor name is not found" in {
      val userAnswers = ua
        .withPage(ReportIdPage, reportId)
        .withPage(SponsorResidentForTaxPage(), SponsorResidentTaxCountryCodes(Seq("GB")))

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, sponsorResidentForTaxRoute)
            .withFormUrlEncodedBody(("country", "GB"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if idx is invalid" in {
      val userAnswers = ua
        .withPage(ReportIdPage, reportId)
        .withPage(SponsorResidentForTaxPage(), SponsorResidentTaxCountryCodes(Seq("GB")))

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode, Some(23)).url)
            .withFormUrlEncodedBody(("country", "GB"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
