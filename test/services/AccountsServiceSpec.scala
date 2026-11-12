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

package services

import base.SpecBase
import models.{AccountBalance, Currency, ReportId, UserAnswers}
import models.SubmissionsConstants.FATCA
import models.viewModels.AccountId
import pages.manual.account.*
import services.manual.account.AccountsService

import java.time.Instant

class AccountsServiceSpec extends SpecBase {

  private val service = new AccountsService()

  private val firstId  = AccountId("80")
  private val secondId = AccountId("12")

  private val firstCreated =
    Instant.parse("2026-11-09T10:00:00.000000001Z")

  private val secondCreated =
    Instant.parse("2026-11-09T10:00:00.000000002Z")

  private def addAccount(
    answers: UserAnswers,
    id: AccountId,
    identifier: String,
    report: ReportId = reportId
  ): UserAnswers =
    answers
      .withPage(AccountIdPage(id)(report), id)
      .withPage(HaveNumberPage(id)(report), false)
      .withPage(IdentifierPage(id)(report), identifier)

  "AccountsService" - {

    "orderedAccounts" - {

      "must list accounts by creation timestamp rather than account ID" in {
        val answers = addAccount(
          addAccount(emptyUserAnswers, secondId, "SECOND"),
          firstId,
          "FIRST"
        )
          .withPage(AccountCreationPage(firstId), firstCreated)
          .withPage(AccountCreationPage(secondId), secondCreated)

        service.orderedAccounts(answers, reportId) mustBe Seq(
          firstId  -> "FIRST",
          secondId -> "SECOND"
        )
      }

      "must preserve the order when an account's identifying answer is edited" in {
        val answers = addAccount(
          addAccount(emptyUserAnswers, firstId, "FIRST"),
          secondId,
          "SECOND"
        )
          .withPage(AccountCreationPage(firstId), firstCreated)
          .withPage(AccountCreationPage(secondId), secondCreated)

        val edited =
          answers.withPage(IdentifierPage(firstId), "UPDATED-FIRST")

        service.orderedAccounts(edited, reportId) mustBe Seq(
          firstId  -> "UPDATED-FIRST",
          secondId -> "SECOND"
        )
      }

      "must display accounts without a timestamp before timestamped accounts" in {
        val answers = addAccount(
          addAccount(emptyUserAnswers, firstId, "NO-TIMESTAMP"),
          secondId,
          "TIMESTAMPED"
        )
          .withPage(AccountCreationPage(secondId), secondCreated)

        service.orderedAccounts(answers, reportId) mustBe Seq(
          firstId  -> "NO-TIMESTAMP",
          secondId -> "TIMESTAMPED"
        )
      }

      "must use account ID as a tie-breaker when creation timestamps match" in {
        val answers = addAccount(
          addAccount(emptyUserAnswers, firstId, "FIRST"),
          secondId,
          "SECOND"
        )
          .withPage(AccountCreationPage(firstId), firstCreated)
          .withPage(AccountCreationPage(secondId), firstCreated)

        service.orderedAccounts(answers, reportId) mustBe Seq(
          secondId -> "SECOND",
          firstId  -> "FIRST"
        )
      }

      "must exclude accounts without a saved number or identifier" in {
        val answers = addAccount(emptyUserAnswers, firstId, "FIRST")
          .withPage(AccountCreationPage(firstId), firstCreated)
          .withPage(AccountIdPage(secondId), secondId)
          .withPage(HaveNumberPage(secondId), false)
          .withPage(AccountCreationPage(secondId), secondCreated)

        service.orderedAccounts(answers, reportId) mustBe Seq(
          firstId -> "FIRST"
        )
      }
    }

    "clearCurrentAccount" - {

      "must clear the current selection while preserving both accounts" in {
        val answers = addAccount(
          addAccount(emptyUserAnswers, firstId, "FIRST"),
          secondId,
          "SECOND"
        )

        val selected =
          answers.withPage(CurrentAccountIdPage(), secondId)

        val cleared =
          service.clearCurrentAccount(selected, reportId).success.value

        cleared.get(CurrentAccountIdPage()) mustBe None
        cleared.data mustBe answers.data
      }
    }

    "isComplete" - {

      "must mark a FATCA account incomplete when payments are declared but none have been entered" in {
        val fatcaReport = reportId.copy(regime = FATCA)

        val complete = addAccount(
          emptyUserAnswers,
          firstId,
          "FIRST",
          fatcaReport
        )
          .withPage(AccountClosedPage(firstId)(fatcaReport), false)
          .withPage(
            WhatWasTheAccountBalancePage(firstId)(fatcaReport),
            AccountBalance(Currency.GBP, "123")
          )
          .withPage(HavePaymentsPage(firstId)(fatcaReport), false)

        service.isComplete(complete, fatcaReport, firstId) mustBe true

        val awaitingPayment =
          complete.withPage(HavePaymentsPage(firstId)(fatcaReport), true)

        service.isComplete(awaitingPayment, fatcaReport, firstId) mustBe false
      }

      "for a CRS account" - {
        import models.manual.account.{WasAccountOpen, WhatAccountType}

        val baseAnswers = addAccount(
          emptyUserAnswers,
          firstId,
          "CRS-ACCOUNT"
        )
          .withPage(IsUndocumentedAccountPage(firstId), false)
          .withPage(IsDormantAccountPage(firstId), false)
          .withPage(WasAccountOpenPage(firstId), WasAccountOpen.Yes)
          .withPage(IsJointAccountPage(firstId), false)
          .withPage(WhatAccountTypePage(firstId), WhatAccountType.Depository)
          .withPage(HavePaymentsPage(firstId), false)

        val balance = AccountBalance(Currency.GBP, "100")

        "must require a balance for an open account even when a currency is saved" in {
          val open = baseAnswers
            .withPage(AccountClosedPage(firstId), false)
            .withPage(WhatWasTheAccountCurrencyPage(firstId), Currency.GBP)

          service.isComplete(open, reportId, firstId) mustBe false

          val withBalance =
            open.withPage(WhatWasTheAccountBalancePage(firstId), balance)

          service.isComplete(withBalance, reportId, firstId) mustBe true
        }

        "must require a currency for a closed account even when a balance is saved" in {
          val closed = baseAnswers
            .withPage(AccountClosedPage(firstId), true)
            .withPage(WhatWasTheAccountBalancePage(firstId), balance)

          service.isComplete(closed, reportId, firstId) mustBe false

          val withCurrency =
            closed.withPage(WhatWasTheAccountCurrencyPage(firstId), Currency.GBP)

          service.isComplete(withCurrency, reportId, firstId) mustBe true
        }

        "must require the holder count when an account changes from sole to joint" in {
          val sole = baseAnswers
            .withPage(AccountClosedPage(firstId), false)
            .withPage(WhatWasTheAccountBalancePage(firstId), balance)

          service.isComplete(sole, reportId, firstId) mustBe true

          val joint =
            sole.withPage(IsJointAccountPage(firstId), true)

          service.isComplete(joint, reportId, firstId) mustBe false

          val withHolderCount =
            joint.withPage(HowManyJointAccountHoldersPage(firstId), 2)

          service.isComplete(withHolderCount, reportId, firstId) mustBe true
        }
      }
    }
  }
}
