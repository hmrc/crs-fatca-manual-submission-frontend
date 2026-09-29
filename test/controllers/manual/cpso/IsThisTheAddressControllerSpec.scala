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
import forms.manual.cpso.IsThisTheAddressFormProvider
import models.SubmissionsConstants.CRS
import models.manual.cpso.{IndividualName, IndividualOrOrganisation}
import models.response.{AddressLookup, Country}
import models.viewModels.manual.cpso.CPSOId
import models.{NormalMode, ReportId}
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
import views.html.manual.cpso.IsThisTheAddressView

import scala.concurrent.Future

class IsThisTheAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new IsThisTheAddressFormProvider()
  val form         = formProvider(CRS)

  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
  val cpsoId: CPSOId              = CPSOId("some-id")

  val addressLookup =
    AddressLookup(990091234514L, Some("2 Other place"), None, Some("Some District"), None, "Town", Some("County"), "postcode", Some(Country.GB))
  val address = addressLookup.toAddress.get

  val name = IndividualName("Test", "Last")

  lazy val isThisTheAddressRoute = controllers.manual.cpso.routes.IsThisTheAddressController.onPageLoad(NormalMode).url

  "IsThisTheAddress Controller" - {

    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
      .withPage(IndividualOrOrganisationPage(cpsoId)(reportId), IndividualOrOrganisation.Individual)
      .withPage(IndividualNamePage(cpsoId)(reportId), name)
      .withPage(AddressLookupPage(cpsoId, reportId), Seq(addressLookup))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsThisTheAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, address, name.fullName, "crs")(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery if address is missing for a GET" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(IndividualOrOrganisationPage(cpsoId)(reportId), IndividualOrOrganisation.Individual)
        .withPage(IndividualNamePage(cpsoId)(reportId), name)

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsThisTheAddressView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to journey recovery if name is missing for a GET" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(IndividualOrOrganisationPage(cpsoId)(reportId), IndividualOrOrganisation.Individual)
        .withPage(AddressLookupPage(cpsoId, reportId), Seq(addressLookup))

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsThisTheAddressView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")

      val userAnswers = ua.set(IsThisTheAddressPage(cpsoId, reportId), true).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressRoute)

        val view = application.injector.instanceOf[IsThisTheAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, address, name.fullName, "crs")(request, messages(application)).toString
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
          FakeRequest(POST, isThisTheAddressRoute)
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
          FakeRequest(POST, isThisTheAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsThisTheAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, address, name.fullName, "crs")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisTheAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no name is present" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(IndividualOrOrganisationPage(cpsoId)(reportId), IndividualOrOrganisation.Individual)
        .withPage(AddressLookupPage(cpsoId, reportId), Seq(addressLookup))

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisTheAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no address is present" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(IndividualOrOrganisationPage(cpsoId)(reportId), IndividualOrOrganisation.Individual)
        .withPage(IndividualNamePage(cpsoId)(reportId), name)

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisTheAddressRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
