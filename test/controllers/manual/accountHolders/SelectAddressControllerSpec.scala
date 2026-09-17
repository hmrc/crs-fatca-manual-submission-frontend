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
import forms.manual.accountHolders.SelectAddressFormProvider
import models.SubmissionsConstants.CRS
import models.manual.accountHolders.IndividualName
import models.viewModels.AccountHolderId
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import models.response.{Address, AddressLookup, Country}
import pages.manual.accountHolders.{AddressLookupForAccountHolderPage, CurrentAccountHolderIdPage, IndividualNamePage, SelectAddressPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.manual.accountHolders.SelectAddressView

import scala.concurrent.Future

class SelectAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val selectAddressRoute = controllers.manual.accountHolders.routes.SelectAddressController.onPageLoad(NormalMode).url

  val formProvider = new SelectAddressFormProvider()
  val form         = formProvider()

  "SelectAddress Controller" - {

    val reportId        = ReportId(CRS, 2025, None, "testFiID")
    val accountHolderId = AccountHolderId("testId")
    val addressLookup =
      AddressLookup(990091234514L, Some("2 Other place"), None, Some("Some District"), None, "Town", Some("County"), "postcode", Some(Country.GB))
    val addresses: Seq[AddressLookup] = Seq(addressLookup)
    val options: Seq[RadioItem] = addresses.map(
      a => RadioItem(content = Text(s"${a.formatRadios}"), value = Some(s"${a.format}"))
    )
    val name                  = IndividualName("test", "last")
    val savedAddress: Address = addressLookup.toAddress.value
    val baseAnswer = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentAccountHolderIdPage()(reportId), accountHolderId)
      .withPage(IndividualNamePage(accountHolderId)(reportId), name)
    val ua = baseAnswer
      .withPage(AddressLookupForAccountHolderPage(accountHolderId, reportId), addresses)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, selectAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectAddressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, name.fullName, options)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.set(SelectAddressPage(accountHolderId, reportId), savedAddress).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, selectAddressRoute)

        val view = application.injector.instanceOf[SelectAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(addressLookup.format), NormalMode, name.fullName, options)(request, messages(application)).toString
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

    "must redirect to Journey Recovery when the selected value matches no address lookup" in {

      val application = applicationBuilder(maybeUserAnswers = Some(baseAnswer)).build()

      running(application) {
        val request =
          FakeRequest(POST, selectAddressRoute)
            .withFormUrlEncodedBody(("value", "some-format-not-in-addresses"))

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
        contentAsString(result) mustEqual view(boundForm, NormalMode, name.fullName, options)(request, messages(application)).toString
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

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, selectAddressRoute)
            .withFormUrlEncodedBody(("value[0]", savedAddress.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
