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
import connectors.DatabaseConnector
import forms.manual.sponsor.AddressNonUkFormProvider
import models.SubmissionsConstants.CRS
import models.{AddressNonUk, NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.manual.sponsor.{AddressNonUkPage, SponsorNamePage}
import pages.ReportIdPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.sponsor.AddressNonUkView
import scala.concurrent.Future

class AddressNonUkControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute = Call("GET", "/foo")

  private val formProvider = new AddressNonUkFormProvider()
  private val form         = formProvider()

  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private val sponsorName = "Test Sponsor"

  private val validAddress = AddressNonUk(
    addressLine1 = "1 Test Street",
    addressLine2 = Some("Suite 2"),
    addressLine3 = "Paris",
    addressLine4 = Some("Ile de France"),
    postcode = Some("75001"),
    country = "FR"
  )

  private val validFormData = Map(
    "addressLine1" -> "1 Test Street",
    "addressLine2" -> "Suite 2",
    "addressLine3" -> "Paris",
    "addressLine4" -> "Ile de France",
    "postcode"     -> "75001",
    "country"      -> "FR"
  )

  private val invalidFormData = Map(
    "addressLine1" -> "",
    "addressLine2" -> "",
    "addressLine3" -> "",
    "addressLine4" -> "",
    "postcode"     -> "",
    "country"      -> ""
  )

  private lazy val addressNonUkRoute =
    controllers.manual.sponsor.routes.AddressNonUkController.onPageLoad(NormalMode).url

  private val userAnswers =
    emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(SponsorNamePage(), sponsorName)

  "AddressNonUkController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addressNonUkRoute)

        val view =
          application.injector.instanceOf[AddressNonUkView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            sponsorName
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val answers =
        userAnswers
          .set(AddressNonUkPage(), validAddress)
          .success
          .value

      val application =
        applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, addressNonUkRoute)

        val view =
          application.injector.instanceOf[AddressNonUkView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(validAddress),
            NormalMode,
            sponsorName
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockDatabaseConnector = mock[DatabaseConnector]

      when(mockDatabaseConnector.set(any())(any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers))
          .overrides(
            bind[ManualSubmissionNavigator]
              .toInstance(
                new FakeManualSubmissionNavigator(onwardRoute)
              ),
            bind[DatabaseConnector]
              .toInstance(mockDatabaseConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addressNonUkRoute)
            .withFormUrlEncodedBody(validFormData.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addressNonUkRoute)
            .withFormUrlEncodedBody(invalidFormData.toSeq*)

        val boundForm =
          form.bind(invalidFormData)

        val view =
          application.injector.instanceOf[AddressNonUkView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            sponsorName
          )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application =
        applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addressNonUkRoute)

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
          FakeRequest(POST, addressNonUkRoute)
            .withFormUrlEncodedBody(validFormData.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
