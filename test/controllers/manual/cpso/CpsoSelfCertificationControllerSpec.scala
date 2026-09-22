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
import forms.manual.cpso.CpsoSelfCertificationFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import models.{NormalMode, ReportId}
import models.manual.cpso.{CpsoSelfCertification, IndividualName}
import models.viewModels.manual.cpso.CPSOId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.CpsoSelfCertificationView
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.ReportIdPage
import pages.manual.cpso.{CpsoSelfCertificationPage, CurrentCPSOIdPage, IndividualNamePage}

import scala.concurrent.Future

class CpsoSelfCertificationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val cpsoSelfCertificationRoute = controllers.manual.cpso.routes.CpsoSelfCertificationController.onPageLoad(NormalMode).url

  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
  val reportingPeriod             = 2025
  val controllingName             = "Some Name"
  val currentId                   = CPSOId("cpso-id")
  val formProvider                = new CpsoSelfCertificationFormProvider()
  val form                        = formProvider()

  "CpsoSelfCertification Controller" - {
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
      .withPage(CurrentCPSOIdPage(), currentId)
      .withPage(IndividualNamePage(currentId), IndividualName("Some", "Name"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, cpsoSelfCertificationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CpsoSelfCertificationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, reportingPeriod, controllingName)(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery when fatca is provided for a GET" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(FATCA, 2025, None, "TestfiID"))
        .withPage(CurrentCPSOIdPage(), currentId)
        .withPage(IndividualNamePage(currentId), IndividualName("Some", "Name"))

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request = FakeRequest(GET, cpsoSelfCertificationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.withPage(CpsoSelfCertificationPage(currentId, reportId), CpsoSelfCertification.allValidValues.head)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, cpsoSelfCertificationRoute)

        val view = application.injector.instanceOf[CpsoSelfCertificationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(CpsoSelfCertification.allValidValues.head), NormalMode, reportingPeriod, controllingName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to journey recovery on a GET when individual name is missing" in {

      val userAnswers = ua
        .withPage(CpsoSelfCertificationPage(currentId, reportId), CpsoSelfCertification.allValidValues.head)
        .remove(IndividualNamePage(currentId))
        .get

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, cpsoSelfCertificationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
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
          FakeRequest(POST, cpsoSelfCertificationRoute)
            .withFormUrlEncodedBody(("value", CpsoSelfCertification.allValidValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to journey recovery when FATCA is provided for a submission" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(FATCA, 2025, None, "TestfiID"))
        .withPage(CurrentCPSOIdPage(), currentId)
        .withPage(IndividualNamePage(currentId), IndividualName("Some", "Name"))

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(answer))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cpsoSelfCertificationRoute)
            .withFormUrlEncodedBody(("value", CpsoSelfCertification.allValidValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect journey recovery  when individual name is missing for a submit" in {
      val userAnswers = ua
        .withPage(CpsoSelfCertificationPage(currentId, reportId), CpsoSelfCertification.allValidValues.head)
        .remove(IndividualNamePage(currentId))
        .get

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cpsoSelfCertificationRoute)
            .withFormUrlEncodedBody(("value", CpsoSelfCertification.allValidValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, cpsoSelfCertificationRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CpsoSelfCertificationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, reportingPeriod, controllingName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, cpsoSelfCertificationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, cpsoSelfCertificationRoute)
            .withFormUrlEncodedBody(("value", CpsoSelfCertification.allValidValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
