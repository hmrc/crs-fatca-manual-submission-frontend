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
import connectors.{AddressLookupConnector, DatabaseConnector}
import controllers.routes
import forms.manual.cpso.UkPostCodeFormProvider
import models.SubmissionsConstants.CRS
import models.manual.cpso.IndividualName
import models.response.{AddressLookup, Country}
import models.viewModels.manual.cpso.CPSOId
import models.{NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.{CurrentCPSOIdPage, IndividualNamePage, UkPostCodePage}
import play.api.data.FormError
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.UkPostCodeView

import scala.concurrent.Future

class UkPostCodeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                = new UkPostCodeFormProvider()
  val form                        = formProvider()
  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
  val controllingName             = "Some Name"
  val currentId                   = CPSOId("cpso-id")

  val addressLookup =
    AddressLookup(990091234514L, Some("2 Other place"), None, Some("Some District"), None, "Town", Some("County"), "postcode", Some(Country.GB))

  lazy val ukPostCodeRoute = controllers.manual.cpso.routes.UkPostCodeController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      UkPostCodePage.toString -> Json.obj(
        "value"  -> "value 1",
        "value2" -> "value 2"
      )
    )
  )

  "UkPostCode Controller" - {
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), currentId)
      .withPage(IndividualNamePage(currentId), IndividualName("Some", "Name"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, ukPostCodeRoute)

        val view = application.injector.instanceOf[UkPostCodeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "crs", controllingName)(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery when individual name is not present for a GET" in {
      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage(), currentId)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, ukPostCodeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val postcode    = "LS23 4ED"
      val userAnswers = ua.set(UkPostCodePage(currentId, reportId), postcode).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ukPostCodeRoute)

        val view = application.injector.instanceOf[UkPostCodeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(postcode), NormalMode, "crs", controllingName)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      val mockAddressLookup = mock[AddressLookupConnector]
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())
      when(mockAddressLookup.findByPostCode(any())(any(), any())).thenReturn(Future.successful(Seq(addressLookup)))

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(mockAddressLookup)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, ukPostCodeRoute)
            .withFormUrlEncodedBody(("value", "ls27 3ed"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a bad request when addressLookup does not return an address" in {
      val mockSessionRepository = mock[DatabaseConnector]
      val mockAddressLookup     = mock[AddressLookupConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())
      when(mockAddressLookup.findByPostCode(any())(any(), any())).thenReturn(Future.successful(Seq()))

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(mockAddressLookup)
          )
          .build()

      val view = application.injector.instanceOf[UkPostCodeView]
      val boundForm = form
        .bind(Map("value" -> "ls23 5ed"))
        .withError(FormError("value", List("uKPostcode.error.notfound")))

      running(application) {
        val request =
          FakeRequest(POST, ukPostCodeRoute)
            .withFormUrlEncodedBody(("value", "ls23 5ed"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "crs", controllingName)(request, messages(application)).toString

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, ukPostCodeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UkPostCodeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "crs", controllingName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, ukPostCodeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, ukPostCodeRoute)
            .withFormUrlEncodedBody(("value", "value 1"), ("value2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
