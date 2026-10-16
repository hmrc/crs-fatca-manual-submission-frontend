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
import forms.manual.cpso.HaveTaxIdentificationNumberFormProvider
import models.SubmissionsConstants.CRS
import models.manual.cpso.IndividualName
import models.response.Country
import models.viewModels.manual.cpso.CPSOId
import models.{CheckMode, NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.{CurrentCPSOIdPage, CurrentTaxResidentCountryIndexPage, HaveTaxIdentificationNumberPage, IndividualNamePage, ResidentForTaxPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.HaveTaxIdentificationNumberView

import scala.concurrent.Future

class HaveTaxIdentificationNumberControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private val cpsoId   = CPSOId("cpso-id")
  private val cpsoName = "Some Name"

  private val previousIndex = 0
  private val currentIndex  = 1

  private val previousCountry = Country("FR", "France")
  private val country         = Country.GB

  private val regiment = reportId.regime.value.toLowerCase()

  private val formProvider = new HaveTaxIdentificationNumberFormProvider()
  private val form         = formProvider(CRS)

  lazy val haveTaxIdentificationNumberRoute =
    controllers.manual.cpso.routes.HaveTaxIdentificationNumberController.onPageLoad(NormalMode).url

  lazy val changeHaveTaxIdentificationNumberRoute =
    controllers.manual.cpso.routes.HaveTaxIdentificationNumberController.onPageLoad(CheckMode).url

  private def userAnswersWith(maybeCountry: Option[Country] = Some(country)): UserAnswers = {
    val answers = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), cpsoId)
      .withPage(IndividualNamePage(cpsoId), IndividualName("Some", "Name"))
      .withPage(ResidentForTaxPage(previousIndex, cpsoId, reportId), previousCountry)
      .withPage(CurrentTaxResidentCountryIndexPage(cpsoId, reportId), currentIndex)

    maybeCountry.fold(answers)(
      c => answers.withPage(ResidentForTaxPage(currentIndex, cpsoId, reportId), c)
    )
  }

  "HaveTaxIdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersWith())).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdentificationNumberRoute)

        val view = application.injector.instanceOf[HaveTaxIdentificationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, country, cpsoName, regiment)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET in check mode" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersWith())).build()

      running(application) {
        val request = FakeRequest(GET, changeHaveTaxIdentificationNumberRoute)

        val view = application.injector.instanceOf[HaveTaxIdentificationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, CheckMode, country, cpsoName, regiment)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersWith()
        .withPage(HaveTaxIdentificationNumberPage(currentIndex, cpsoId, reportId), true)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdentificationNumberRoute)

        val view = application.injector.instanceOf[HaveTaxIdentificationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(true), NormalMode, country, cpsoName, regiment)(request, messages(application)).toString
      }
    }

    "must save the answer against the current index and redirect to the next page when valid data is submitted" in {

      val mockRepository = mock[DatabaseConnector]

      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswersWith()))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, haveTaxIdentificationNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(HaveTaxIdentificationNumberPage(currentIndex, cpsoId, reportId)) mustBe Some(true)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersWith())).build()

      running(application) {
        val request =
          FakeRequest(POST, haveTaxIdentificationNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[HaveTaxIdentificationNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, country, cpsoName, regiment)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdentificationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, haveTaxIdentificationNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when no country has been selected" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersWith(maybeCountry = None))).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdentificationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST when no country has been selected" in {

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersWith(maybeCountry = None))).build()

      running(application) {
        val request =
          FakeRequest(POST, haveTaxIdentificationNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
