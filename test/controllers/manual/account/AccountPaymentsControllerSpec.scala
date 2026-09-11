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
import forms.manual.account.AccountPaymentsFormProvider
import models.SubmissionsConstants.CRS
import models.manual.account.{AccountPayment, AccountPaymentsAmount}
import models.manual.account.PaymentType.CRSInterest
import models.*
import models.viewModels.AccountId
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.account.{AccountIdPage, AccountPaymentListPage, CurrentAccountIdPage, DoYouNeedToAddPaymentsPage, PaymentsAddedPreviouslyPage}
import play.api.inject.bind
import play.api.mvc.{Call, Flash}
import play.api.mvc.ControllerHelpers.request2flash
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.account.AccountPaymentsView

import scala.concurrent.Future

class AccountPaymentsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AccountPaymentsFormProvider()
  val form         = formProvider(0)

  lazy val accountPaymentsRoute = controllers.manual.account.routes.AccountPaymentsController.onPageLoad(NormalMode).url

  "AccountPayments Controller" - {
    implicit val reportId    = ReportId(CRS, 2025, None, "TestfiID")
    val currency             = Currency(code = "VED", displayName = "Venezuelan Bolivar (VED)")
    val accountPaymentList   = Seq(AccountPayment(CRSInterest, Some(AccountPaymentsAmount(currency, "1000"))))
    val regimeType           = "crs"
    val accountId: AccountId = AccountId("TestAccountId")
    val reportingPeriod      = "2025"
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
      .withPage(CurrentAccountIdPage(), accountId)

    "must return OK and the correct view for a GET" in {
      val userAnswers = ua
        .withPage(PaymentsAddedPreviouslyPage(accountId), true)
        .withPage(AccountPaymentListPage(accountId), accountPaymentList)
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, accountPaymentsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountPaymentsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, accountPaymentList, reportingPeriod, regimeType)(request,
                                                                                                                  messages(application),
                                                                                                                  Flash()
        ).toString
      }
    }

    "must redirect to PaymentTypeController when there are no previous added payments and accountPayment list is empty" in {
      val userAnswers = ua.withPage(AccountPaymentListPage(accountId), Seq.empty)
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, accountPaymentsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.account.routes.PaymentTypeController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to CurrentAccountPaymentIndexController when there are no previous added payments and the last account payment is not complete" in {
      val incompleteAccountPaymentList = Seq(AccountPayment(CRSInterest, None))
      val userAnswers                  = ua.withPage(AccountPaymentListPage(accountId), incompleteAccountPaymentList)
      val application                  = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, accountPaymentsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.account.routes.CurrentAccountPaymentIndexController
          .onChangeRedirect(incompleteAccountPaymentList.size - 1)
          .url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = ua
        .withPage(DoYouNeedToAddPaymentsPage(accountId), true)
        .withPage(PaymentsAddedPreviouslyPage(accountId), true)
        .withPage(AccountPaymentListPage(accountId), accountPaymentList)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, accountPaymentsRoute)

        val view = application.injector.instanceOf[AccountPaymentsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, accountPaymentList, reportingPeriod, regimeType)(request,
                                                                                                                             messages(application),
                                                                                                                             Flash()
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
          FakeRequest(POST, accountPaymentsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val contextForm = formProvider(accountPaymentList.size)
      val userAnswers = ua
        .withPage(DoYouNeedToAddPaymentsPage(accountId), true)
        .withPage(PaymentsAddedPreviouslyPage(accountId), true)
        .withPage(AccountPaymentListPage(accountId), accountPaymentList)
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, accountPaymentsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = contextForm.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AccountPaymentsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, accountPaymentList, reportingPeriod, regimeType)(request,
                                                                                                                       messages(application),
                                                                                                                       Flash()
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, accountPaymentsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, accountPaymentsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
