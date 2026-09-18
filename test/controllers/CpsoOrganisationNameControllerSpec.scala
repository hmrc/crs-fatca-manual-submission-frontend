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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import connectors.DatabaseConnector
import forms.manual.cpso.CpsoOrganisationNameFormProvider
import views.html.CpsoOrganisationNameView
import models.SubmissionsConstants.{CRS, FATCA}
import models.viewModels.manual.cpso.CPSOId
import models.{NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.ReportIdPage
import pages.manual.cpso.{CpsoOrganisationNamePage, CurrentCPSOIdPage}

import scala.concurrent.Future

class CpsoOrganisationNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new CpsoOrganisationNameFormProvider()
  val form         = formProvider()
  val reportId     = ReportId(FATCA, 2025, None, "TestfiID")
  val currentId    = CPSOId("cpso-id")

  lazy val cpsoOrganisationNameRoute = controllers.manual.cpso.routes.CpsoOrganisationNameController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      CpsoOrganisationNamePage.toString -> Json.obj(
        "organizationName" -> "value 1",
        "some-name"        -> "value 2"
      )
    )
  )

  "CpsoOrganisationName Controller" - {
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage()(reportId), currentId)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, cpsoOrganisationNameRoute)

        val view = application.injector.instanceOf[CpsoOrganisationNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery when CRS is used in GET" in {
      val updatedUa   = ua.withPage(ReportIdPage, reportId.copy(regime = CRS))
      val application = applicationBuilder(maybeUserAnswers = Some(updatedUa)).build()

      running(application) {
        val request = FakeRequest(GET, cpsoOrganisationNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val validAnswer = "value 1"

      val userAnswers = ua.withPage(CpsoOrganisationNamePage(currentId, reportId), validAnswer)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, cpsoOrganisationNameRoute)

        val view = application.injector.instanceOf[CpsoOrganisationNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("value 1"), NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, cpsoOrganisationNameRoute)
            .withFormUrlEncodedBody(("value", "value 1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to journey recovery when valid data is submitted for crs regime" in {
      val updatedUa             = ua.withPage(ReportIdPage, reportId.copy(regime = CRS))
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(updatedUa))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cpsoOrganisationNameRoute)
            .withFormUrlEncodedBody(("value", "value 1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, cpsoOrganisationNameRoute)
            .withFormUrlEncodedBody(("value", "invalid--value"))

        val boundForm = form.bind(Map("value" -> "invalid--value"))

        val view = application.injector.instanceOf[CpsoOrganisationNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, cpsoOrganisationNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, cpsoOrganisationNameRoute)
            .withFormUrlEncodedBody(("organizationName", "value 1"), ("some-name", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
