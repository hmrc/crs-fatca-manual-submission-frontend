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
import connectors.{AddressLookupConnector, DatabaseConnector}
import forms.manual.accountHolders.UkPostCodeForAccountHolderFormProvider
import models.SubmissionsConstants.CRS
import models.manual.accountHolders.IndividualName
import models.response.{AddressLookup, Country}
import models.viewModels.AccountHolderId
import models.{NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.{CurrentAccountHolderIdPage, IndividualNamePage, UkPostCodeForAccountHolderPage}
import play.api.data.FormError
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.accountHolders.UkPostCodeForAccountHolderView

import scala.concurrent.Future

class UkPostCodeForAccountHolderControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                     = new UkPostCodeForAccountHolderFormProvider()
  implicit val reportId: ReportId      = ReportId(CRS, 2025, None, "TestfiID")
  val accountHolderId: AccountHolderId = AccountHolderId("some-id")
  val individualName                   = IndividualName("Some-name", "Some-last-name")
  val accountHolderName                = s"${individualName.FirstName} ${individualName.LastName}".trim
  val form                             = formProvider()

  val addressLookup =
    AddressLookup(990091234514L, Some("2 Other place"), None, Some("Some District"), None, "Town", Some("County"), "postcode", Some(Country.GB))

  lazy val ukPostCodeForAccountHolderRoute = controllers.manual.accountHolders.routes.UkPostCodeForAccountHolderController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      UkPostCodeForAccountHolderPage.toString -> Json.obj(
        "postcode" -> "value 1",
        "some"     -> "value 2"
      )
    )
  )

  "UkPostCodeForAccountHolder Controller" - {
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
      .withPage(CurrentAccountHolderIdPage(), accountHolderId)
      .withPage(IndividualNamePage(accountHolderId), individualName)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, ukPostCodeForAccountHolderRoute)

        val view = application.injector.instanceOf[UkPostCodeForAccountHolderView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, accountHolderName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val validAnswer = "some-post-code"
      val userAnswers = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), accountHolderId)
        .withPage(IndividualNamePage(accountHolderId), individualName)
        .withPage(UkPostCodeForAccountHolderPage(accountHolderId, reportId), validAnswer)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ukPostCodeForAccountHolderRoute)

        val view = application.injector.instanceOf[UkPostCodeForAccountHolderView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, accountHolderName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if individual name is not present" in {
      val userAnswers = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), accountHolderId)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ukPostCodeForAccountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val postcode = "SOME-POST-CODE"
      val userAnswers = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), accountHolderId)
        .withPage(IndividualNamePage(accountHolderId), individualName)

      val mockSessionRepository = mock[DatabaseConnector]
      val mockAddressLookup     = mock[AddressLookupConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())
      when(mockAddressLookup.findByPostCode(any())(any(), any())).thenReturn(Future.successful(Seq(addressLookup)))

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(mockAddressLookup)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, ukPostCodeForAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "ls23 5ed"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a bad request when addressLookup does not return an address" in {
      val userAnswers = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), accountHolderId)
        .withPage(IndividualNamePage(accountHolderId), individualName)

      val mockSessionRepository = mock[DatabaseConnector]
      val mockAddressLookup     = mock[AddressLookupConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())
      when(mockAddressLookup.findByPostCode(any())(any(), any())).thenReturn(Future.successful(Seq()))

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository),
            bind[AddressLookupConnector].toInstance(mockAddressLookup)
          )
          .build()

      val view = application.injector.instanceOf[UkPostCodeForAccountHolderView]
      val boundForm = form
        .bind(Map("value" -> "ls23 5ed"))
        .withError(FormError("value", List("uKPostcode.error.notfound")))

      running(application) {
        val request =
          FakeRequest(POST, ukPostCodeForAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "ls23 5ed"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, accountHolderName)(request, messages(application)).toString

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, ukPostCodeForAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UkPostCodeForAccountHolderView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, accountHolderName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, ukPostCodeForAccountHolderRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, ukPostCodeForAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "value 1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if individual name is not provided" in {
      val userAnswers = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), accountHolderId)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, ukPostCodeForAccountHolderRoute)
            .withFormUrlEncodedBody(("value", "value 1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
