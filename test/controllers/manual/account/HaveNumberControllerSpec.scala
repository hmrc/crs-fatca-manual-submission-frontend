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

package controllers.manual.account

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.manual.account.HaveNumberFormProvider
import models.{NormalMode, UserAnswers}
import models.viewModels.AccountId
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.manual.account.{AccountCreationPage, AccountIdPage, CurrentAccountIdPage, HaveNumberPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.account.HaveNumberView

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.Future

class HaveNumberControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new HaveNumberFormProvider()
  val form         = formProvider()

  private val submissionTime =
    Instant.parse("2026-11-09T12:00:00Z")

  private val fixedClock =
    Clock.fixed(submissionTime, ZoneOffset.UTC)

  lazy val haveNumberRoute =
    controllers.manual.account.routes.HaveNumberController
      .onPageLoad(NormalMode)
      .url

  "HaveNumber Controller" - {

    val accountId: AccountId = AccountId("TestAccountId")

    val ua = emptyUserAnswers
      .withPage(CurrentAccountIdPage()(reportId), accountId)

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, haveNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HaveNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua
        .set(HaveNumberPage(accountId)(reportId), true)
        .success
        .value

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, haveNumberRoute)

        val view = application.injector.instanceOf[HaveNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(true), NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockDBConnector = mock[DatabaseConnector]

      when(mockDBConnector.set(any())(any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator]
              .toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockDBConnector),
            bind[Clock].toInstance(fixedClock)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, haveNumberRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must save the creation timestamp when a new account's first answer is submitted" in {

      val newAccountId = AccountId("12")

      val userAnswers = emptyUserAnswers
        .withPage(
          CurrentAccountIdPage()(reportId),
          newAccountId
        )

      val mockDBConnector = mock[DatabaseConnector]

      when(mockDBConnector.set(any())(any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers))
          .overrides(
            bind[ManualSubmissionNavigator]
              .toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockDBConnector),
            bind[Clock].toInstance(fixedClock)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, haveNumberRoute)
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val savedAnswers =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(mockDBConnector).set(savedAnswers.capture())(any())

        savedAnswers.getValue
          .get(HaveNumberPage(newAccountId)(reportId)) mustBe Some(false)

        savedAnswers.getValue
          .get(AccountIdPage(newAccountId)(reportId)) mustBe Some(newAccountId)

        savedAnswers.getValue
          .get(AccountCreationPage(newAccountId)(reportId)) mustBe
          Some(submissionTime)
      }
    }

    "must preserve an existing account's creation timestamp when its answer is submitted again" in {

      val existingAccountId = AccountId("80")
      val originalCreationTime =
        Instant.parse("2026-11-08T09:00:00Z")

      val userAnswers = emptyUserAnswers
        .withPage(
          AccountIdPage(existingAccountId)(reportId),
          existingAccountId
        )
        .withPage(
          HaveNumberPage(existingAccountId)(reportId),
          false
        )
        .withPage(
          AccountCreationPage(existingAccountId)(reportId),
          originalCreationTime
        )
        .withPage(
          CurrentAccountIdPage()(reportId),
          existingAccountId
        )

      val mockDBConnector = mock[DatabaseConnector]

      when(mockDBConnector.set(any())(any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers))
          .overrides(
            bind[ManualSubmissionNavigator]
              .toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockDBConnector),
            bind[Clock].toInstance(fixedClock)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, haveNumberRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val savedAnswers =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        verify(mockDBConnector).set(savedAnswers.capture())(any())

        savedAnswers.getValue
          .get(HaveNumberPage(existingAccountId)(reportId)) mustBe Some(true)

        savedAnswers.getValue
          .get(AccountCreationPage(existingAccountId)(reportId)) mustBe
          Some(originalCreationTime)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, haveNumberRoute)
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[HaveNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application =
        applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, haveNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application =
        applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, haveNumberRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
