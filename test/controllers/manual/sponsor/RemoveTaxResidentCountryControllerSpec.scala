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
import forms.manual.sponsor.RemoveTaxResidentCountryFormProvider
import models.SubmissionsConstants.CRS
import models.manual.sponsor.TaxResidentCountry
import models.sponsor.RemoveCountryMessage
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.sponsor.{RemoveTaxResidentCountryPage, SponsorNamePage, SponsorResidentForTaxPage, TaxResidentCountriesListPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.sponsor.RemoveTaxResidentCountryView

import scala.concurrent.Future

class RemoveTaxResidentCountryControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute  = Call("GET", "/foo")
  val id           = 0
  val formProvider = new RemoveTaxResidentCountryFormProvider()
  val form         = formProvider()

  lazy val removeTaxResidentCountryRoute = controllers.manual.sponsor.routes.RemoveTaxResidentCountryController.onPageLoad(NormalMode, id).url
  val country                            = "Ethiopia"
  val code                               = "ET"
  val sponsorName                        = "Some Sponsor Name"
  implicit val reportId: ReportId        = ReportId(CRS, 2025, None, "TestfiID")
  "RemoveTaxResidentCountry Controller" - {

    val ua = emptyUserAnswers.withPage(ReportIdPage, reportId)

    "must return OK and the correct view for a GET" in {
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(TaxResidentCountry(code)))
        .withPage(SponsorResidentForTaxPage(0), code)
        .withPage(SponsorNamePage(), sponsorName)
      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveTaxResidentCountryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, id, sponsorName, country, RemoveCountryMessage.AllOtherCountryMessage)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" ignore {

      implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")

      val userAnswers = ua.set(RemoveTaxResidentCountryPage(), true).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val view = application.injector.instanceOf[RemoveTaxResidentCountryView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, id, sponsorName, country, RemoveCountryMessage.NationsWithDefiniteArticlesMessage)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(TaxResidentCountry(code)))
        .withPage(SponsorResidentForTaxPage(0), code)
        .withPage(SponsorNamePage(), sponsorName)
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(useranswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(TaxResidentCountry(code)))
        .withPage(SponsorResidentForTaxPage(0), code)
        .withPage(SponsorNamePage(), sponsorName)
      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveTaxResidentCountryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, id, sponsorName, country, RemoveCountryMessage.AllOtherCountryMessage)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no sponsor name  is found" in {
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(TaxResidentCountry(code)))
        .withPage(SponsorResidentForTaxPage(0), code)

      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no SponsorResidentForTaxPage is found" in {
      val useranswers = ua.withPage(SponsorNamePage(), sponsorName)

      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET for invalid country code" in {
      val countryCode = "ZWD"
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(TaxResidentCountry(countryCode)))
        .withPage(SponsorResidentForTaxPage(0), countryCode)
        .withPage(SponsorNamePage(), sponsorName)
      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no sponsor name is found" in {
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(TaxResidentCountry(code)))
        .withPage(SponsorResidentForTaxPage(0), code)

      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no SponsorResidentForTax Page is found" in {
      val useranswers = ua.withPage(SponsorNamePage(), sponsorName)

      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if invalid index is in url" in {
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(TaxResidentCountry(code)))
        .withPage(SponsorResidentForTaxPage(0), code)
        .withPage(SponsorNamePage(), sponsorName)

      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.manual.sponsor.routes.RemoveTaxResidentCountryController.onPageLoad(NormalMode, 100).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST for invalid country code" in {
      val countryCode = "ZWD"
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(TaxResidentCountry(countryCode)))
        .withPage(SponsorResidentForTaxPage(0), countryCode)
        .withPage(SponsorNamePage(), sponsorName)
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(useranswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
