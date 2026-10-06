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
import forms.manual.cpso.SelectAddressFormProvider
import models.SubmissionsConstants.CRS
import models.manual.cpso.IndividualName
import models.response.{Address, AddressLookup, Country}
import models.viewModels.manual.cpso.CPSOId
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.{AddressLookupPage, CurrentCPSOIdPage, IndividualNamePage, SelectAddressPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.manual.cpso.SelectAddressView

import scala.concurrent.Future

class SelectAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val selectAddressRoute = controllers.manual.cpso.routes.SelectAddressController.onPageLoad(NormalMode).url

  val formProvider                = new SelectAddressFormProvider()
  val form                        = formProvider(CRS)
  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
  val controllingName             = "test last"
  val currentId                   = CPSOId("cpso-id")

  val addressLookup =
    AddressLookup(990091234514L, Some("2 Other place"), None, Some("Some District"), None, "Town", Some("County"), "postcode", Some(Country.GB))
  val addresses: Seq[AddressLookup] = Seq(addressLookup)

  val options: Seq[RadioItem] = addresses.map(
    a => RadioItem(content = Text(s"${a.formatRadios}"), value = Some(s"${a.format}"))
  )
  val name                  = IndividualName("test", "last")
  val savedAddress: Address = addressLookup.toAddress.value

  "SelectAddress Controller" - {
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), currentId)
      .withPage(IndividualNamePage(currentId), name)
      .withPage(AddressLookupPage(currentId, reportId), addresses)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, selectAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, options, name.fullName, "crs")(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery if address is not present for a GET" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage(), currentId)
        .withPage(IndividualNamePage(currentId), name)

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request = FakeRequest(GET, selectAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")
      val userAnswers       = ua.set(SelectAddressPage(currentId, reportId), savedAddress).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, selectAddressRoute)

        val view = application.injector.instanceOf[SelectAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(addressLookup.format), NormalMode, options, name.fullName, "crs")(request,
                                                                                                                           messages(application)
        ).toString
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
          FakeRequest(POST, selectAddressRoute)
            .withFormUrlEncodedBody(("value", addressLookup.format))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to journey recovery when address is not present for a submit" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentCPSOIdPage(), currentId)
        .withPage(IndividualNamePage(currentId), name)

      val application =
        applicationBuilder(maybeUserAnswers = Some(answer))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, selectAddressRoute)
            .withFormUrlEncodedBody(("value", addressLookup.format))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to journey recovery when address submitted is not same as one in the database" in {
      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, selectAddressRoute)
            .withFormUrlEncodedBody(("value", "some value"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, selectAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SelectAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, options, name.fullName, "crs")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, selectAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, selectAddressRoute)
            .withFormUrlEncodedBody(("value", "some value"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
