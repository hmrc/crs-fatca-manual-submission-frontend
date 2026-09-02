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
import forms.manual.account.PaymentTypeFormProvider
import models.SubmissionsConstants.CRS
import models.{NormalMode, ReportId}
import models.manual.account.{AccountPayment, PaymentType}
import models.viewModels.AccountId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.account.PaymentTypeView
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.ReportIdPage
import pages.manual.account.{AccountPaymentListPage, AccountPaymentPage, CurrentAccountIdPage, CurrentAccountPaymentIndexPage, NumberTypePage}
import models.NumberType.Iban

import scala.concurrent.Future

class PaymentTypeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val paymentTypeRoute = controllers.manual.account.routes.PaymentTypeController.onPageLoad(NormalMode).url

  val formProvider                 = new PaymentTypeFormProvider()
  val form                         = formProvider()
  val crs                          = CRS
  implicit val reportId: ReportId  = ReportId(CRS, 2025, None, "TestfiID")
  private val accountId: AccountId = AccountId(value = "SomeId")

  "PaymentType Controller" - {
    val ua = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))

    "must return OK and the correct view for a GET" in {

      val userAnswers = ua.withPage(CurrentAccountIdPage(), accountId)
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, paymentTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PaymentTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, crs, true)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = ua
        .withPage(CurrentAccountIdPage(), accountId)
        .withPage(CurrentAccountPaymentIndexPage(accountId), 0)
        .withPage(AccountPaymentPage(0)(accountId = accountId), AccountPayment(PaymentType.values.head))

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, paymentTypeRoute)

        val view = application.injector.instanceOf[PaymentTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(PaymentType.values.head), NormalMode, crs, true)(request, messages(application)).toString
      }
    }

    "must update payment type to CRS interest when number type is Iban" in {
      val userAnswers = ua
        .withPage(CurrentAccountIdPage(), accountId)
        .withPage(NumberTypePage(accountId), Iban)
        .withPage(AccountPaymentListPage(accountId), Seq(AccountPayment(PaymentType.CRSDividends)))

      val mockSessionRepository = mock[DatabaseConnector]
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(userAnswers))
          .overrides(bind[DatabaseConnector].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(GET, paymentTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua.withPage(CurrentAccountIdPage(), accountId)))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, paymentTypeRoute)
            .withFormUrlEncodedBody(("value", PaymentType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua.withPage(CurrentAccountIdPage(), accountId))).build()

      running(application) {
        val request =
          FakeRequest(POST, paymentTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PaymentTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, crs, true)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, paymentTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, paymentTypeRoute)
            .withFormUrlEncodedBody(("value", PaymentType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
