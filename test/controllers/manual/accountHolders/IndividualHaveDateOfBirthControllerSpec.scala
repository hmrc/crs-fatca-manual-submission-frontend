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
import forms.manual.accountHolders.IndividualHaveDateOfBirthFormProvider
import models.SubmissionsConstants.CRS
import models.manual.accountHolders.IndividualName
import models.viewModels.AccountHolderId
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.FINamePage
import pages.manual.accountHolders.{CurrentAccountHolderIdPage, IndividualHaveDateOfBirthPage, IndividualNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.accountHolders.IndividualHaveDateOfBirthView

import scala.concurrent.Future

class IndividualHaveDateOfBirthControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new IndividualHaveDateOfBirthFormProvider()
  val form         = formProvider()

  lazy val individualHaveDateOfBirthRoute =
    controllers.manual.accountHolders.routes.IndividualHaveDateOfBirthController
      .onPageLoad(NormalMode)
      .url

  "IndividualHaveDateOfBirth Controller" - {

    implicit val reportId: ReportId =
      ReportId(CRS, 2025, None, "TestfiID")

    val currentAccountHolderId =
      AccountHolderId("testid")

    val fiName =
      "Test FI"

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
          FINamePage()(reportId),
          fiName
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
          FakeRequest(GET, individualHaveDateOfBirthRoute)

        val view =
          application.injector
            .instanceOf[IndividualHaveDateOfBirthView]

        val result =
          route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            fiName,
            accountHolderName,
            CRS
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        ua.withPage(
          IndividualHaveDateOfBirthPage(currentAccountHolderId)(reportId),
          true
        )

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, individualHaveDateOfBirthRoute)

        val view =
          application.injector
            .instanceOf[IndividualHaveDateOfBirthView]

        val result =
          route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(true),
            NormalMode,
            fiName,
            accountHolderName,
            CRS
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
          FakeRequest(POST, individualHaveDateOfBirthRoute)
            .withFormUrlEncodedBody(
              "value" -> "true"
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
          FakeRequest(POST, individualHaveDateOfBirthRoute)
            .withFormUrlEncodedBody(
              "value" -> ""
            )

        val boundForm =
          form.bind(
            Map(
              "value" -> ""
            )
          )

        val view =
          application.injector
            .instanceOf[IndividualHaveDateOfBirthView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            fiName,
            accountHolderName,
            CRS
          )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application =
        applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, individualHaveDateOfBirthRoute)

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
          FakeRequest(POST, individualHaveDateOfBirthRoute)
            .withFormUrlEncodedBody(
              "value" -> "true"
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
