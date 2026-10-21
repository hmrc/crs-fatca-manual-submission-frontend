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
import forms.manual.cpso.ResidentForTaxFormProvider
import models.SubmissionsConstants.CRS
import models.manual.cpso.IndividualName
import models.viewModels.manual.cpso.CPSOId
import models.{Countries, NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.{CurrentCPSOIdPage, CurrentTaxResidentCountryIndexPage, IndividualNamePage, ResidentForTaxPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.ResidentForTaxView

import scala.concurrent.Future

class ResidentForTaxControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  implicit override val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private val cpsoId       = CPSOId("cpso-id")
  private val cpsoName     = "Some Name"
  private val currentIndex = 0

  private val regiment = reportId.regime.toString.toLowerCase

  private val unitedKingdom = Countries.allCountries(CRS).find(_.code == "GB").value

  private val formProvider = new ResidentForTaxFormProvider()
  private val form         = formProvider(CRS)

  lazy val residentForTaxRoute = controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(NormalMode).url

  "ResidentForTax Controller" - {

    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), cpsoId)
      .withPage(IndividualNamePage(cpsoId), IndividualName("Some", "Name"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, residentForTaxRoute)

        val view = application.injector.instanceOf[ResidentForTaxView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, cpsoName, Countries.allCountries(CRS), regiment)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = ua
        .withPage(ResidentForTaxPage(currentIndex, cpsoId, reportId), unitedKingdom)
        .withPage(CurrentTaxResidentCountryIndexPage(cpsoId, reportId), currentIndex)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, residentForTaxRoute)

        val view = application.injector.instanceOf[ResidentForTaxView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(unitedKingdom.code), NormalMode, cpsoName, Countries.allCountries(CRS), regiment)(request, messages(application)).toString
      }
    }

    "must save the selected country against the current index and redirect to the next page when valid data is submitted" in {

      val mockRepository = mock[DatabaseConnector]

      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, residentForTaxRoute)
            .withFormUrlEncodedBody("country" -> unitedKingdom.code)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        val saved = captor.getValue
        saved.get(ResidentForTaxPage(currentIndex, cpsoId, reportId)) mustBe Some(unitedKingdom)
        saved.get(CurrentTaxResidentCountryIndexPage(cpsoId, reportId)) mustBe Some(currentIndex)
      }
    }

    "must return a Bad Request and errors when no country is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, residentForTaxRoute)
            .withFormUrlEncodedBody("country" -> "")

        val boundForm = form.bind(Map("country" -> ""))

        val view = application.injector.instanceOf[ResidentForTaxView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, cpsoName, Countries.allCountries(CRS), regiment)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, residentForTaxRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, residentForTaxRoute)
            .withFormUrlEncodedBody("country" -> unitedKingdom.code)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
