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
import forms.manual.cpso.TaxIdentificationNumberFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.cpso.{IndividualName, IndividualOrOrganisation}
import models.response.Country
import models.viewModels.manual.cpso.CPSOId
import models.{CheckMode, NormalMode, ReportId, UserAnswers}
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
import views.html.manual.cpso.TaxIdentificationNumberView

import scala.concurrent.Future

class TaxIdentificationNumberControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute: Call = Call("GET", "/foo")

  private val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private val fatcaReportId: ReportId = ReportId(FATCA, 2025, None, "TestfiID")

  private val cpsoId   = CPSOId("cpso-id")
  private val cpsoName = "Some Name"

  private val previousIndex = 0
  private val currentIndex  = 1

  private val selectedCountry = Country.GB

  private val regime = reportId.regime.value.toLowerCase()

  private val tin = "Y1234567"

  private val formProvider = new TaxIdentificationNumberFormProvider()
  private val form         = formProvider(selectedCountry, reportId, Some(IndividualOrOrganisation.Individual))

  private lazy val taxIdentificationNumberRoute =
    controllers.manual.cpso.routes.TaxIdentificationNumberController.onPageLoad(NormalMode).url

  private lazy val changeTaxIdentificationNumberRoute =
    controllers.manual.cpso.routes.TaxIdentificationNumberController.onPageLoad(CheckMode).url

  private def userAnswersFor(
    rd: ReportId,
    maybeCountry: Option[Country] = Some(selectedCountry),
    individualOrOrganisation: Option[IndividualOrOrganisation] = None
  ): UserAnswers = {
    val answers = emptyUserAnswers
      .withPage(ReportIdPage, rd)
      .withPage(CurrentCPSOIdPage()(rd), cpsoId)
      .withPage(IndividualNamePage(cpsoId)(rd), IndividualName("Some", "Name"))
      .withPage(CurrentTaxResidentCountryIndexPage(cpsoId, rd), currentIndex)
      .withPage(ResidentForTaxPage(previousIndex, cpsoId, rd), selectedCountry)

    val withCountry =
      maybeCountry.fold(answers)(
        country => answers.withPage(ResidentForTaxPage(currentIndex, cpsoId, rd), country)
      )

    individualOrOrganisation.fold(withCountry)(
      value => withCountry.withPage(IndividualOrOrganisationPage(cpsoId)(rd), value)
    )
  }

  private lazy val defaultAnswers = userAnswersFor(reportId)

  "TaxIdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(defaultAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, taxIdentificationNumberRoute)

        val view = application.injector.instanceOf[TaxIdentificationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, cpsoName, selectedCountry, regime)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET in check mode" in {

      val application = applicationBuilder(maybeUserAnswers = Some(defaultAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, changeTaxIdentificationNumberRoute)

        val view = application.injector.instanceOf[TaxIdentificationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, CheckMode, cpsoName, selectedCountry, regime)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val answers = defaultAnswers
        .withPage(TaxIdentificationNumberPage(currentIndex, cpsoId, reportId), tin)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, taxIdentificationNumberRoute)

        val view = application.injector.instanceOf[TaxIdentificationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(tin), NormalMode, cpsoName, selectedCountry, regime)(request, messages(application)).toString
      }
    }

    "must save the answer against the current tax resident country and redirect to the next page when valid data is submitted" in {

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
          FakeRequest(POST, taxIdentificationNumberRoute)
            .withFormUrlEncodedBody(("tin", tin))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(TaxIdentificationNumberPage(currentIndex, cpsoId, reportId)) mustBe Some(tin)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(defaultAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, taxIdentificationNumberRoute)
            .withFormUrlEncodedBody(("tin", ""))

        val boundForm = form.bind(Map("tin" -> ""))

        val view = application.injector.instanceOf[TaxIdentificationNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, cpsoName, selectedCountry, regime)(request, messages(application)).toString
      }
    }

    "must render the view for a fatca report when the CP-SO is an individual" in {

      val answers = userAnswersFor(fatcaReportId, individualOrOrganisation = Some(IndividualOrOrganisation.Individual))
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers.withPage(ReportIdPage, fatcaReportId))))

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, taxIdentificationNumberRoute)

        val view = application.injector.instanceOf[TaxIdentificationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, cpsoName, selectedCountry, "fatca")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET when the individual or organisation has not been answered for a fatca report" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor(fatcaReportId))).build()

      running(application) {
        val request = FakeRequest(GET, taxIdentificationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, taxIdentificationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, taxIdentificationNumberRoute)
            .withFormUrlEncodedBody(("tin", tin))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when no country has been selected for the current tax resident country" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor(reportId, maybeCountry = None))).build()

      running(application) {
        val request = FakeRequest(GET, taxIdentificationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when no country has been selected for the current tax resident country" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor(reportId, maybeCountry = None))).build()

      running(application) {
        val request =
          FakeRequest(POST, taxIdentificationNumberRoute)
            .withFormUrlEncodedBody(("tin", tin))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
