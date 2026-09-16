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

package controllers.manual.accountHolders

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.manual.accountHolders.IsThisTheAddressForAccountHoldersFormProvider
import models.SubmissionsConstants.CRS
import models.manual.accountHolders.IndividualName
import models.response.{AddressLookup, Country}
import models.viewModels.AccountHolderId
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.{AddressLookupForAccountHolderPage, CurrentAccountHolderIdPage, IndividualNamePage, IsThisTheAddressForAccountHoldersPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.accountHolders.IsThisTheAddressForAccountHoldersView

import scala.concurrent.Future

class IsThisTheAddressForAccountHoldersControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new IsThisTheAddressForAccountHoldersFormProvider()
  val form         = formProvider()

  implicit val reportId: ReportId      = ReportId(CRS, 2025, None, "TestfiID")
  val accountHolderId: AccountHolderId = AccountHolderId("some-id")
  val individualName                   = IndividualName("Some-name", "Some-last-name")
  val accountHolderName                = s"${individualName.FirstName} ${individualName.LastName}".trim

  val addressLookup =
    AddressLookup(990091234514L, Some("2 Other place"), None, Some("Some District"), None, "Town", Some("County"), "postcode", Some(Country.GB))
  val address = addressLookup.toAddress.get

  lazy val isThisTheAddressForAccountHoldersRoute =
    controllers.manual.accountHolders.routes.IsThisTheAddressForAccountHoldersController.onPageLoad(NormalMode).url

  "IsThisTheAddressForAccountHolders Controller" - {
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
      .withPage(CurrentAccountHolderIdPage(), accountHolderId)
      .withPage(IndividualNamePage(accountHolderId), individualName)
      .withPage(AddressLookupForAccountHolderPage(accountHolderId, reportId), Seq(addressLookup))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressForAccountHoldersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsThisTheAddressForAccountHoldersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, address, accountHolderName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua
        .withPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), true)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressForAccountHoldersRoute)

        val view = application.injector.instanceOf[IsThisTheAddressForAccountHoldersView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, address, accountHolderName)(request, messages(application)).toString
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
          FakeRequest(POST, isThisTheAddressForAccountHoldersRoute)
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
          FakeRequest(POST, isThisTheAddressForAccountHoldersRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsThisTheAddressForAccountHoldersView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, address, accountHolderName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressForAccountHoldersRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if individual name is not provided" in {
      val answers = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), accountHolderId)
        .withPage(AddressLookupForAccountHolderPage(accountHolderId, reportId), Seq(addressLookup))

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressForAccountHoldersRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if address is not present" in {
      val answers = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), accountHolderId)
        .withPage(IndividualNamePage(accountHolderId), individualName)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, isThisTheAddressForAccountHoldersRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisTheAddressForAccountHoldersRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if individual name is not provided" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), accountHolderId)
        .withPage(AddressLookupForAccountHolderPage(accountHolderId, reportId), Seq(addressLookup))

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisTheAddressForAccountHoldersRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if address is not provided" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), accountHolderId)
        .withPage(IndividualNamePage(accountHolderId), individualName)

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisTheAddressForAccountHoldersRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
