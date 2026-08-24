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
import forms.manual.cpso.IndividualOrOrganisationFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import models.{NormalMode, ReportId}
import models.manual.cpso.IndividualOrOrganisation
import models.viewModels.manual.cpso.CPSOId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.IndividualOrOrganisationView
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.ReportIdPage
import pages.manual.cpso.{CurrentCPSOIdPage, IndividualOrOrganisationPage}

import scala.concurrent.Future

class IndividualOrOrganisationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val individualOrOrganisationRoute = controllers.manual.cpso.routes.IndividualOrOrganisationController.onPageLoad(NormalMode).url

  val formProvider = new IndividualOrOrganisationFormProvider()
  val form         = formProvider()

  "IndividualOrOrganisation Controller" - {
    val reportId = ReportId(FATCA, 2025, None, "TestfiID")
    val ua       = emptyUserAnswers.withPage(ReportIdPage, reportId)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, individualOrOrganisationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualOrOrganisationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val currentId = CPSOId("testid")
      val userAnswers = ua
        .withPage(CurrentCPSOIdPage()(reportId), currentId)
        .withPage(IndividualOrOrganisationPage(currentId)(reportId), IndividualOrOrganisation.values.head)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualOrOrganisationRoute)

        val view = application.injector.instanceOf[IndividualOrOrganisationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(IndividualOrOrganisation.values.head), NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, individualOrOrganisationRoute)
            .withFormUrlEncodedBody(("value", IndividualOrOrganisation.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualOrOrganisationRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[IndividualOrOrganisationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, individualOrOrganisationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET when Regime is CRS" in {
      val reportId = ReportId(CRS, 2025, None, "TestfiID")
      val ua       = emptyUserAnswers.withPage(ReportIdPage, reportId)

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, individualOrOrganisationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, individualOrOrganisationRoute)
            .withFormUrlEncodedBody(("value", IndividualOrOrganisation.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
