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
import forms.manual.accountHolders.IndividualDateOfBirthFormProvider
import models.SubmissionsConstants.CRS
import models.manual.accountHolders.{IndividualDateOfBirth, IndividualName}
import models.viewModels.AccountHolderId
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.{CurrentAccountHolderIdPage, IndividualDateOfBirthPage, IndividualNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.accountHolders.IndividualDateOfBirthView

import java.time.LocalDate
import scala.concurrent.Future

class IndividualDateOfBirthControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new IndividualDateOfBirthFormProvider()
  val form         = formProvider()

  lazy val individualDateOfBirthRoute =
    controllers.manual.accountHolders.routes.IndividualDateOfBirthController
      .onPageLoad(NormalMode)
      .url

  "IndividualDateOfBirth Controller" - {

    implicit val reportId: ReportId =
      ReportId(CRS, 2025, None, "TestfiID")

    val currentAccountHolderId =
      AccountHolderId("testid")

    val individualName =
      IndividualName(
        FirstName = "Account Holdings",
        LastName = "plc"
      )

    val accountHolderName =
      "Account Holdings plc"

    val ua =
      emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(
          CurrentAccountHolderIdPage()(reportId),
          currentAccountHolderId
        )
        .withPage(
          IndividualNamePage(currentAccountHolderId)(reportId),
          individualName
        )

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(GET, individualDateOfBirthRoute)

        val view =
          application.injector
            .instanceOf[IndividualDateOfBirthView]

        val result =
          route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            accountHolderName
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val validAnswer =
        IndividualDateOfBirth(
          LocalDate.of(1980, 3, 31)
        )

      val userAnswers =
        ua.withPage(
          IndividualDateOfBirthPage(currentAccountHolderId)(reportId),
          validAnswer
        )

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, individualDateOfBirthRoute)

        val view =
          application.injector
            .instanceOf[IndividualDateOfBirthView]

        val result =
          route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(validAnswer),
            NormalMode,
            accountHolderName
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository =
        mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator]
              .toInstance(
                new FakeManualSubmissionNavigator(onwardRoute)
              ),
            bind[DatabaseConnector]
              .toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, individualDateOfBirthRoute)
            .withFormUrlEncodedBody(
              "value.day"   -> "31",
              "value.month" -> "3",
              "value.year"  -> "1980"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualDateOfBirthRoute)
            .withFormUrlEncodedBody(
              "value.day"   -> "",
              "value.month" -> "",
              "value.year"  -> ""
            )

        val boundForm =
          form.bind(
            Map(
              "value.day"   -> "",
              "value.month" -> "",
              "value.year"  -> ""
            )
          )

        val view =
          application.injector
            .instanceOf[IndividualDateOfBirthView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            accountHolderName
          )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application =
        applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, individualDateOfBirthRoute)

        val result =
          route(application, request).value

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
          FakeRequest(POST, individualDateOfBirthRoute)
            .withFormUrlEncodedBody(
              "value.day"   -> "31",
              "value.month" -> "3",
              "value.year"  -> "1980"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if the individual name is missing" in {

      val userAnswers =
        emptyUserAnswers
          .withPage(ReportIdPage, reportId)
          .withPage(
            CurrentAccountHolderIdPage()(reportId),
            currentAccountHolderId
          )

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, individualDateOfBirthRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if the individual name is missing" in {

      val userAnswers =
        emptyUserAnswers
          .withPage(ReportIdPage, reportId)
          .withPage(
            CurrentAccountHolderIdPage()(reportId),
            currentAccountHolderId
          )

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualDateOfBirthRoute)
            .withFormUrlEncodedBody(
              "value.day"   -> "31",
              "value.month" -> "3",
              "value.year"  -> "1980"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
