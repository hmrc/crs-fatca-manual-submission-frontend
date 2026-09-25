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
import forms.manual.cpso.IndividualPlaceOfBirthFormProvider
import models.SubmissionsConstants.FATCA
import models.manual.cpso.IndividualPlaceOfBirth
import models.{Countries, NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.manual.cpso.{CurrentCPSOIdPage, IndividualNamePage, IndividualOrOrganisationPage, IndividualPlaceOfBirthPage}
import models.manual.cpso.{IndividualName, IndividualOrOrganisation}
import models.viewModels.manual.cpso.CPSOId
import pages.ReportIdPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.IndividualPlaceOfBirthView

import scala.concurrent.Future

class IndividualPlaceOfBirthControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new IndividualPlaceOfBirthFormProvider()
  val form         = formProvider()

  lazy val individualPlaceOfBirthRoute = controllers.manual.cpso.routes.IndividualPlaceOfBirthController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      IndividualPlaceOfBirthPage.toString -> Json.obj(
        "City"   -> "value 1",
        "Region" -> "value 2"
      )
    )
  )

  "IndividualPlaceOfBirth Controller" - {
    val reportId  = ReportId(FATCA, 2025, None, "TestfiID")
    val countries = Countries.allCountries(FATCA)
    val cpsoId    = CPSOId("TestId")
    val name      = IndividualName("Test", "Last")
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
      .withPage(IndividualOrOrganisationPage(cpsoId)(reportId), IndividualOrOrganisation.Individual)
      .withPage(IndividualNamePage(cpsoId)(reportId), name)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, individualPlaceOfBirthRoute)

        val view = application.injector.instanceOf[IndividualPlaceOfBirthView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, false, name.fullName, countries)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val validAnswer = IndividualPlaceOfBirth(Some("value 1"), Some("value 2"), "FX")
      val userAnswers = ua.set(IndividualPlaceOfBirthPage(cpsoId, reportId), validAnswer).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualPlaceOfBirthRoute)

        val view = application.injector.instanceOf[IndividualPlaceOfBirthView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(IndividualPlaceOfBirth(Some("value 1"), Some("value 2"), "FX")),
                                               NormalMode,
                                               false,
                                               name.fullName,
                                               countries
        )(
          request,
          messages(application)
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
          FakeRequest(POST, individualPlaceOfBirthRoute)
            .withFormUrlEncodedBody(("city", "value 1"), ("region", "value 2"), ("country", "FR"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualPlaceOfBirthRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[IndividualPlaceOfBirthView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, false, name.fullName, countries)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, individualPlaceOfBirthRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, individualPlaceOfBirthRoute)
            .withFormUrlEncodedBody(("City", "value 1"), ("Region", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
