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

package controllers.manual.cpso

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.manual.cpso.TaxResidentCountriesFormProvider
import models.manual.cpso.IndividualName
import models.response.Country
import models.viewModels.manual.cpso.CPSOId
import models.{NormalMode, TaxResidentInformation}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.*
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.TaxResidentCountriesView

import scala.concurrent.Future

class TaxResidentCountriesControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new TaxResidentCountriesFormProvider()
  val form         = formProvider()

  val cpsoId       = CPSOId("cpso-id")
  val cpsoName     = "Some Name"
  val currentIndex = 0

  private val regime = reportId.regime.value.toLowerCase()

  private val taxResidentCountries = Seq(
    TaxResidentInformation(Country("FR", "France")),
    TaxResidentInformation(Country("DE", "Germany"))
  )

  lazy val taxResidentCountriesRoute = controllers.manual.cpso.routes.TaxResidentCountriesController.onPageLoad(NormalMode).url

  "TaxResidentCountries Controller" - {

    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), cpsoId)
      .withPage(IndividualNamePage(cpsoId), IndividualName("Some", "Name"))
      .withPage(CurrentTaxResidentCountryIndexPage(cpsoId, reportId), currentIndex)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, taxResidentCountriesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxResidentCountriesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, cpsoName, regime, countries = Seq.empty)(request,
                                                                                                          messages(application),
                                                                                                          request.flash
        ).toString
      }
    }

    "must pass the tax resident countries from user answers to the view" in {

      val userAnswers = ua.withPage(new TaxResidentCountriesListPage(cpsoId, reportId), taxResidentCountries)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxResidentCountriesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxResidentCountriesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, cpsoName, regime, countries = Seq("France", "Germany"))(request, messages(application), request.flash).toString
      }
    }

    "must render the success banner when a country has been removed" in {

      val request = FakeRequest(GET, taxResidentCountriesRoute).withFlash("country-removed" -> "France")

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxResidentCountriesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, cpsoName, regime, countries = Seq.empty)(request, messages(application), request.flash).toString
        contentAsString(result) must include("govuk-notification-banner--success")
        contentAsString(result) must include("France removed.")
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = ua
        .withPage(DoYouWantToAddTaxResidentCountryPage(cpsoId, reportId), true)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxResidentCountriesRoute)

        val view = application.injector.instanceOf[TaxResidentCountriesView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, cpsoName, regime, countries = Seq.empty)(request,
                                                                                                                     messages(application),
                                                                                                                     request.flash
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, taxResidentCountriesRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, taxResidentCountriesRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[TaxResidentCountriesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, cpsoName, regime, countries = Seq.empty)(request,
                                                                                                               messages(application),
                                                                                                               request.flash
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, taxResidentCountriesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, taxResidentCountriesRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
