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
import models.response.Country
import models.sponsor.RemoveCountryMessage
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.sponsor.{
  CurrentTaxResidentCountryIndexPage,
  RemoveTaxResidentCountryPage,
  SponsorNamePage,
  SponsorResidentForTaxPage,
  TaxResidentCountriesListPage
}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.sponsor.RemoveTaxResidentCountryView

import scala.concurrent.Future

class RemoveTaxResidentCountryControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute  = Call("GET", "/foo")
  val formProvider = new RemoveTaxResidentCountryFormProvider()
  val form         = formProvider()

  lazy val removeTaxResidentCountryRoute = controllers.manual.sponsor.routes.RemoveTaxResidentCountryController.onPageLoad(NormalMode).url
  val country                            = "United Kingdom"
  val GB                                 = Country("GB", "United Kingdom")
  val sponsorName                        = "Some Sponsor Name"
  implicit val reportId: ReportId        = ReportId(CRS, 2025, None, "TestfiID")
  "RemoveTaxResidentCountry Controller" - {

    val ua = emptyUserAnswers.withPage(ReportIdPage, reportId)

    "must return OK and the correct view for a GET" in {
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(GB))
        .withPage(SponsorResidentForTaxPage(0), GB)
        .withPage(SponsorNamePage(), sponsorName)
        .withPage(CurrentTaxResidentCountryIndexPage(), 0)
      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveTaxResidentCountryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, sponsorName, country, RemoveCountryMessage.NationsWithDefiniteArticlesMessage)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(GB))
        .withPage(SponsorResidentForTaxPage(0), GB)
        .withPage(SponsorNamePage(), sponsorName)
        .withPage(CurrentTaxResidentCountryIndexPage(), 0)

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
        .withPage(TaxResidentCountriesListPage(), Seq(GB))
        .withPage(SponsorResidentForTaxPage(0), GB)
        .withPage(SponsorNamePage(), sponsorName)
        .withPage(CurrentTaxResidentCountryIndexPage(), 0)
      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveTaxResidentCountryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, sponsorName, country, RemoveCountryMessage.NationsWithDefiniteArticlesMessage)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no sponsor name  is found" in {
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(GB))
        .withPage(SponsorResidentForTaxPage(0), GB)

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
      val countryCode    = "ZWD"
      val invalidCountry = Country(countryCode, "some-other-planet")
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(invalidCountry))
        .withPage(SponsorResidentForTaxPage(0), invalidCountry)
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
        .withPage(TaxResidentCountriesListPage(), Seq(GB))
        .withPage(SponsorResidentForTaxPage(0), GB)

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

    "must redirect to Journey Recovery for a POST for invalid country code" in {
      val countryCode    = "ZWD"
      val invalidCountry = Country(countryCode, "some-other-planet")
      val useranswers = ua
        .withPage(TaxResidentCountriesListPage(), Seq(invalidCountry))
        .withPage(SponsorResidentForTaxPage(0), invalidCountry)
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
