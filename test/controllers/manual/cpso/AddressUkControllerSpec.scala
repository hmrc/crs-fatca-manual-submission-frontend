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
import forms.manual.cpso.AddressUkFormProvider
import models.SubmissionsConstants.CRS
import models.manual.cpso.IndividualName
import models.viewModels.manual.cpso.CPSOId
import models.{Countries, NormalMode, ReportId, UkAddress, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.{AddressUkPage, CurrentCPSOIdPage, IndividualNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.cpso.AddressUkView

import scala.concurrent.Future

class AddressUkControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                = new AddressUkFormProvider()
  val form                        = formProvider()
  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
  val testName                    = "Some Name"
  val countries                   = Countries.crsUkTerritories

  lazy val addressUkRoute = controllers.manual.cpso.routes.AddressUkController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      AddressUkPage.toString -> Json.obj(
        "address1" -> "value 1",
        "address2" -> "value 2"
      )
    )
  )

  "AddressUk Controller" - {
    val cpsoId = CPSOId("some-id")
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), cpsoId)
      .withPage(IndividualNamePage(cpsoId), IndividualName("Some", "Name"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, addressUkRoute)

        val view = application.injector.instanceOf[AddressUkView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "crs", testName, countries)(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery when name is not present for a GET" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage(), cpsoId)

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request = FakeRequest(GET, addressUkRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val validAnswer       = UkAddress("value 1", Some("value 2"), "Some City", Some("Some County"), "AA1 1AA", "GB")
      implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")
      val userAnswers       = ua.set(AddressUkPage(cpsoId, reportId), validAnswer).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addressUkRoute)

        val view = application.injector.instanceOf[AddressUkView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, "crs", testName, countries)(request, messages(application)).toString
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
          FakeRequest(POST, addressUkRoute)
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

    "must redirect to journey recovery when name is not present for a submit" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage(), cpsoId)

      val application =
        applicationBuilder(maybeUserAnswers = Some(answer))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addressUkRoute)
            .withFormUrlEncodedBody(("addressLine1", "value 1"),
                                    ("addressLine2", "value 2"),
                                    ("city", "Some City"),
                                    ("county", "Some County"),
                                    ("postCode", "AA1 1AA"),
                                    ("country", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, addressUkRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddressUkView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "crs", testName, countries)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addressUkRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addressUkRoute)
            .withFormUrlEncodedBody(("address1", "value 1"), ("address2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
