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
import forms.UkAddressFormProvider
import models.UkAddress
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import connectors.DatabaseConnector
import views.html.UkAddressView
import models.SubmissionsConstants.CRS
import models.{NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.manual.sponsor.SponsorNamePage
import pages.{ReportIdPage, UkAddressPage}

import scala.concurrent.Future

class UkAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                = new UkAddressFormProvider()
  val form                        = formProvider()
  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
  lazy val ukAddressRoute         = controllers.manual.sponsor.routes.UkAddressController.onPageLoad(NormalMode).url
  val sponsorName                 = "Test Sponsor Name"

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      UkAddressPage.toString -> Json.obj(
        "addressLine1" -> "value 1",
        "addressLine2" -> "value 2"
      )
    )
  )

  "UkAddress Controller" - {
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)

    "must return OK and the correct view for a GET" in {
      val userAnswers = ua.withPage(SponsorNamePage(), "Test Sponsor Name")
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)

        val view = application.injector.instanceOf[UkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, sponsorName)(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery when sponsor name is not present" in {
      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val validAnswer       = UkAddress("value 1", Some("value 2"), "Some City", Some("Some County"), "AA1 1AA", "GB")
      implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")
      val userAnswers       = ua.withPage(UkAddressPage(), validAnswer).withPage(SponsorNamePage(), "Test Sponsor Name")

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)

        val view = application.injector.instanceOf[UkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, sponsorName)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers           = ua.withPage(SponsorNamePage(), "Test Sponsor Name")
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
          FakeRequest(POST, ukAddressRoute)
            .withFormUrlEncodedBody(("addressLine1", "value 1"),
                                    ("addressLine2", "value 2"),
                                    ("city", "Some City"),
                                    ("county", "Some County"),
                                    ("postCode", "AA1 1AA"),
                                    ("country", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = ua.withPage(SponsorNamePage(), "Test Sponsor Name")
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, ukAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[UkAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, sponsorName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, ukAddressRoute)
            .withFormUrlEncodedBody(("addressLine1", "value 1"), ("addressLine2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to to Journey Recovery for a POST when Sponsor name is not provided" in {

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
          FakeRequest(POST, ukAddressRoute)
            .withFormUrlEncodedBody(("addressLine1", "value 1"), ("addressLine2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
