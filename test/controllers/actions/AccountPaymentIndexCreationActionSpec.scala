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

package controllers.actions

import base.SpecBase
import models.SubmissionsConstants.FATCA
import models.manual.account.{AccountPayment, PaymentType}
import models.requests.{AccountIdRequest, AccountPaymentIdRequest}
import models.viewModels.AccountId
import models.{ReportId, UserAnswers}
import org.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.account.{AccountPaymentListPage, CurrentAccountPaymentIndexPage}
import play.api.test.FakeRequest

import scala.concurrent.Future

class AccountPaymentIndexCreationActionSpec extends SpecBase with MockitoSugar {

  class Harness extends AccountPaymentIndexCreationActionImpl() {
    def callTransform[A](request: AccountIdRequest[A]): Future[AccountPaymentIdRequest[A]] = transform(request)
  }

  private val userId    = "user-id"
  private val fatcaId   = "FATCAID"
  private val accountId = AccountId("TestAccountId")

  private val reportId = ReportId(
    regime = FATCA,
    reportingYear = 2025,
    uploadedTime = None,
    fiId = "FIID"
  )

  val userAnswers =
    emptyUserAnswers
      .set(ReportIdPage, reportId)
      .success
      .value

  private def accountIdRequest(userAnswers: UserAnswers): AccountIdRequest[_] =
    AccountIdRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId,
      accountId = accountId
    )

  "AccountPaymentIdCreationAction" - {

    "when there is no currentAccountPaymentIndex in the userAnswer" - {

      "must create new id from the current account payment list size and set in request" in {
        val action = new Harness()
        val updatedUA = userAnswers.withPage(
          AccountPaymentListPage(accountId)(reportId),
          Seq(
            AccountPayment(PaymentType.FATCAInterest),
            AccountPayment(PaymentType.FATCAOther)
          )
        )

        val result = action.callTransform(accountIdRequest(updatedUA)).futureValue

        result.userId mustBe userId
        result.userAnswers mustBe updatedUA
        result.fatcaId mustBe fatcaId
        result.reportId mustBe reportId
        result.accountId mustBe accountId
        result.currentIndex mustBe 2
      }
    }

    "when there is currentAccountPaymentIndex in the userAnswer" - {

      "must use existing id and set in request" in {
        val action = new Harness()
        val updatedUA = userAnswers
          .withPage(CurrentAccountPaymentIndexPage(accountId)(reportId), 1)
          .withPage(
            AccountPaymentListPage(accountId)(reportId),
            Seq(
              AccountPayment(PaymentType.FATCADividends),
              AccountPayment(PaymentType.FATCAInterest),
              AccountPayment(PaymentType.FATCAOther)
            )
          )

        val result = action.callTransform(accountIdRequest(updatedUA)).futureValue

        result.userId mustBe userId
        result.userAnswers mustBe updatedUA
        result.fatcaId mustBe fatcaId
        result.reportId mustBe reportId
        result.accountId mustBe accountId
        result.currentIndex mustBe 1
      }
    }
  }

}
