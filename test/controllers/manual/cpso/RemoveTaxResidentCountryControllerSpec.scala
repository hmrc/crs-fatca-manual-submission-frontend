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
import forms.manual.cpso.RemoveTaxResidentCountryFormProvider
import models.manual.cpso.IndividualName
import models.response.Country
import models.viewModels.manual.cpso.CPSOId
import models.{Countries, NormalMode, TaxResidentInformation, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.*
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.RemoveTaxResidentCountryView

import scala.concurrent.Future

class RemoveTaxResidentCountryControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute: Call = Call("GET", "/foo")

  private val cpsoId   = CPSOId("cpso-id")
  private val cpsoName = "Some Name"

  private val currentIndex = 1

  private val regime = reportId.regime.value.toLowerCase()

  private val country = Countries.allCountries(reportId.regime).find(_.code == "DE").value

  private val taxResidentCountries = Seq(
    TaxResidentInformation(Countries.allCountries(reportId.regime).find(_.code == "FR").value),
    TaxResidentInformation(country),
    TaxResidentInformation(Countries.allCountries(reportId.regime).find(_.code == "ES").value)
  )

  private val formProvider = new RemoveTaxResidentCountryFormProvider()
  private val form         = formProvider(reportId.regime)

  private lazy val removeTaxResidentCountryRoute =
    controllers.manual.cpso.routes.RemoveTaxResidentCountryController.onPageLoad(NormalMode).url

  private def taxResidentCountriesPage: TaxResidentCountriesListPage =
    new TaxResidentCountriesListPage(cpsoId, reportId)

  private def userAnswersFor(
    residentCountry: Option[Country] = Some(country),
    countries: Option[Seq[TaxResidentInformation]] = Some(taxResidentCountries)
  ): UserAnswers = {
    val answers = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), cpsoId)
      .withPage(IndividualNamePage(cpsoId), IndividualName("Some", "Name"))
      .withPage(CurrentTaxResidentCountryIndexPage(cpsoId, reportId), currentIndex)

    val withCountries = countries.fold(answers)(
      value => answers.withPage(taxResidentCountriesPage, value)
    )

    residentCountry.fold(withCountries)(
      value => withCountries.withPage(ResidentForTaxPage(currentIndex, cpsoId, reportId), value)
    )
  }

  private lazy val defaultAnswers = userAnswersFor()

  private def savesTheAnswers(answers: UserAnswers, repository: DatabaseConnector): UserAnswers = {
    val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
    verify(repository).set(captor.capture())(any())
    captor.getValue
  }

  "RemoveTaxResidentCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(defaultAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val view = application.injector.instanceOf[RemoveTaxResidentCountryView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, country.description, cpsoName, regime)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET when no country has been selected for the current tax resident country" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor(residentCountry = None, countries = None))).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when the selected country is not a known country" in {

      val unknownCountry = Country("ZZ", "Nowhere")

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor(residentCountry = Some(unknownCountry)))).build()

      running(application) {
        val request = FakeRequest(GET, removeTaxResidentCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must remove the country, save the answers, flash the removed country and redirect to the next page when the user says yes" in {

      val mockRepository = mock[DatabaseConnector]

      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(defaultAnswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        flash(result).get("country-removed") mustBe Some(country.description)

        savesTheAnswers(defaultAnswers, mockRepository).get(taxResidentCountriesPage) mustBe
          Some(taxResidentCountries.patch(currentIndex, Nil, 1))
      }
    }

    "must keep the country, save the answers and redirect without a flash message when the user says no" in {

      val mockRepository = mock[DatabaseConnector]

      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(defaultAnswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        flash(result).get("country-removed") mustBe None

        savesTheAnswers(defaultAnswers, mockRepository).get(taxResidentCountriesPage) mustBe Some(taxResidentCountries)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(defaultAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveTaxResidentCountryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, country.description, cpsoName, regime)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a POST when no country has been selected for the current tax resident country" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor(residentCountry = None, countries = None))).build()

      running(application) {
        val request =
          FakeRequest(POST, removeTaxResidentCountryRoute)
            .withFormUrlEncodedBody(("value", "true"))

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
  }
}
