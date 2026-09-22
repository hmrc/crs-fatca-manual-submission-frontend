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
import models.SubmissionsConstants.CRS
import models.manual.account.PaymentType.CRSInterest
import models.manual.account.{AccountPayment, AccountPaymentsAmount}
import models.viewModels.AccountId
import models.{CheckMode, Currency, ReportId}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.account.{AccountPaymentListPage, CurrentAccountIdPage}
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, route, running, status, writeableOf_AnyContentAsEmpty, GET}

import scala.concurrent.Future

class CurrentAccountPaymentIndexControllerSpec extends SpecBase with MockitoSugar {

  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
  val currency                    = Currency(code = "VED", displayName = "Venezuelan Bolivar (VED)")
  val accountPaymentList          = Seq(AccountPayment(CRSInterest, Some(AccountPaymentsAmount(currency, "1000"))))

  val accountId = AccountId("TestAccountId")

  val ua = emptyUserAnswers
    .withPage(ReportIdPage, reportId)
    .withPage(CurrentAccountIdPage()(reportId), accountId)
    .withPage(AccountPaymentListPage(accountId), accountPaymentList)

  def onChangeRedirectRoute(index: Int) = controllers.manual.account.routes.CurrentAccountPaymentIndexController.onChangeRedirect(index).url
  def onRemoveRedirectRoute(index: Int) = controllers.manual.account.routes.CurrentAccountPaymentIndexController.onRemoveRedirect(index).url

  "CurrentAccountPaymentIndexController" - {

    "should redirect to check account type is depository controller when change action is called" in {
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[DatabaseConnector].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, onChangeRedirectRoute(0))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.account.routes.CheckAccountTypeIsDepositoryController.onChangeRedirect(CheckMode).url
      }

    }

    "should redirect to remove a payment when remove action is called" in {
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(
          bind[DatabaseConnector].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, onRemoveRedirectRoute(0))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.account.routes.RemovePaymentController.onPageLoad(CheckMode).url
      }

    }
  }

}
