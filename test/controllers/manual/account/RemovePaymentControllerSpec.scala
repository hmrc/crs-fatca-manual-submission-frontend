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
import forms.manual.account.RemovePaymentFormProvider
import models.SubmissionsConstants.CRS
import models.manual.account.PaymentType.CRSInterest
import models.manual.account.{AccountPayment, AccountPaymentsAmount}
import models.viewModels.AccountId
import models.{CheckMode, Currency, NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.should
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.account.{AccountPaymentListPage, CurrentAccountIdPage, CurrentAccountPaymentIndexPage, RemovePaymentPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.account.RemovePaymentView

import scala.concurrent.Future

class RemovePaymentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
  val accountId: AccountId = AccountId("TestAccountId")
  val formProvider = new RemovePaymentFormProvider()
  val form         = formProvider()
  val currency = Currency(code = "VED", displayName = "Venezuelan Bolivar (VED)")
  val accountPayment = AccountPayment(CRSInterest, Some(AccountPaymentsAmount(currency, "1000")))
  val accountPaymentList = Seq(AccountPayment(CRSInterest, Some(AccountPaymentsAmount(currency, "1000"))))
  lazy val removePaymentRoute = controllers.manual.account.routes.RemovePaymentController.onPageLoad(CheckMode).url

  "RemovePayment Controller" - {

    val ua = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
      .withPage(CurrentAccountIdPage(), accountId)

    "must return OK and the correct view for a GET" in {
      val useranswers = ua.withPage(CurrentAccountPaymentIndexPage(accountId), 0)
      .withPage(AccountPaymentListPage(accountId), accountPaymentList)

      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request = FakeRequest(GET, removePaymentRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemovePaymentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, accountPayment)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val useranswers = ua.withPage(CurrentAccountPaymentIndexPage(accountId), 0)
        .withPage(AccountPaymentListPage(accountId), accountPaymentList)
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(useranswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removePaymentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        flash(result).data should contain("account-payment-removed" -> "1,000 VED interest")
      }
    }

    "must redirect to the next page when no is selected and flash message is not populated" in {
      val useranswers = ua.withPage(CurrentAccountPaymentIndexPage(accountId), 0)
        .withPage(AccountPaymentListPage(accountId), accountPaymentList)
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(useranswers))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removePaymentRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        flash(result).data should not contain key("account-payment-removed")
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val useranswers = ua.withPage(CurrentAccountPaymentIndexPage(accountId), 0)
        .withPage(AccountPaymentListPage(accountId), accountPaymentList)

      val application = applicationBuilder(maybeUserAnswers = Some(useranswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removePaymentRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemovePaymentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, accountPayment)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removePaymentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removePaymentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
