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
import forms.manual.cpso.ControllingPersonTypeFormProvider
import models.SubmissionsConstants.FATCA
import models.manual.cpso.{ControllingPersonType, ControllingPersonTypes, IndividualName, IndividualOrOrganisation}
import models.viewModels.manual.cpso.CPSOId
import models.{NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.{ControllingPersonTypePage, CurrentCPSOIdPage, IndividualNamePage, IndividualOrOrganisationPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.ControllingPersonTypeView

import scala.concurrent.Future

class ControllingPersonTypeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ControllingPersonTypeFormProvider()
  val form         = formProvider()

  lazy val controllingPersonTypeRoute = controllers.manual.cpso.routes.ControllingPersonTypeController.onPageLoad(NormalMode).url

  val cpsoId = CPSOId("TestId")
  val name   = IndividualName("Test", "Last")

  def userAnswersFor(reportId: ReportId): UserAnswers = emptyUserAnswers
    .withPage(ReportIdPage, reportId)
    .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
    .withPage(IndividualOrOrganisationPage(cpsoId)(reportId), IndividualOrOrganisation.Individual)
    .withPage(IndividualNamePage(cpsoId)(reportId), name)

  val ua = userAnswersFor(reportId)

  "ControllingPersonType Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllingPersonTypeRoute)
        val view    = application.injector.instanceOf[ControllingPersonTypeView]
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, 2025, name.fullName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val validAnswer = ControllingPersonTypes(
        Seq(
          ControllingPersonType.OwnershipOfLegalPerson,
          ControllingPersonType.TrusteeOfTrust
        )
      )

      val userAnswers = ua.set(ControllingPersonTypePage(cpsoId, reportId), validAnswer).success.value
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllingPersonTypeRoute)
        val view    = application.injector.instanceOf[ControllingPersonTypeView]
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, 2025, name.fullName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must show 14 checkboxes when the reporting year is 2025" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllingPersonTypeRoute)
        val result  = route(application, request).value
        val html    = contentAsString(result)

        status(result) mustEqual OK
        "govuk-checkboxes__item".r.findAllIn(html).length mustEqual 14
        html must include("Not reported")
      }
    }

    "must show 13 checkboxes when the reporting year is after 2025" in {

      val newReportId = ReportId(FATCA, 2026, None, "TestfiID")
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswersFor(newReportId))).build()
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers.withPage(ReportIdPage, newReportId))))

      running(application) {
        val request = FakeRequest(GET, controllingPersonTypeRoute)
        val result  = route(application, request).value
        val html    = contentAsString(result)

        status(result) mustEqual OK
        "govuk-checkboxes__item".r.findAllIn(html).length mustEqual 13
        html must not include "Not reported"
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
          FakeRequest(POST, controllingPersonTypeRoute)
            .withFormUrlEncodedBody(
              ("types[]", "ownershipOfLegalPerson"),
              ("types[]", "trusteeOfTrust")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when no checkbox is selected" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingPersonTypeRoute)
            .withFormUrlEncodedBody(("unused", "value"))

        val boundForm = form.bind(Map("unused" -> "value"))
        val view      = application.injector.instanceOf[ControllingPersonTypeView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, 2025, name.fullName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllingPersonTypeRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingPersonTypeRoute)
            .withFormUrlEncodedBody(("types[]", "ownershipOfLegalPerson"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
