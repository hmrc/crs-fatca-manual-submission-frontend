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
import models.viewModels.{AccountHolderId, AccountId}
import pages.manual.account.*
import pages.manual.accountHolders.AccountPage as HolderAccountsPage
import play.api.libs.json.Json
import services.manual.account.AccountsService

class AccountsServiceSpec extends SpecBase {

  private val service = new AccountsService()

  private val firstId  = AccountId("80")
  private val secondId = AccountId("12")

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

    "removeAccount" - {

      "must remove the last account and its selection when no account order was recorded" in {
        val answers = addAccount(emptyUserAnswers, firstId, "FIRST")
          .withPage(CurrentAccountIdPage(), firstId)

        val result = service.removeAccount(answers, reportId, firstId).success.value

        result.get(AccountDataPage(firstId)) mustBe None
        result.get(CurrentAccountIdPage()) mustBe None
        result.get(AccountOrderPage(reportId)) mustBe None
        service.orderedAccounts(result, reportId) mustBe empty
      }

      "must unlink the removed account and preserve the holders, other accounts and other reports" in {
        val sharedHolder = AccountHolderId("1")
        val soleHolder   = AccountHolderId("2")
        val otherReport  = reportId.copy(fiId = "another-fi")

        val sharedLinks = HolderAccountsPage(reportId, sharedHolder)
        val soleLinks   = HolderAccountsPage(reportId, soleHolder)

        val survivingAnswers = addAccount(
          addAccount(emptyUserAnswers, secondId, "KEEP"),
          firstId,
          "OTHER-REPORT",
          otherReport
        )
          .withPage(AccountClosedPage(secondId), false)
          .withPage(CurrentAccountIdPage(), secondId)
          .withPage(AccountOrderPage(reportId), Seq(secondId))
          .withPage(sharedLinks, Set(secondId))

        val holderRecord = Json.obj("name" -> "Holder Two")
        val holderPath   = Json.obj(soleHolder.value -> holderRecord)
        val reportData   = (survivingAnswers.data \ reportId.mongoKey).as[play.api.libs.json.JsObject]
        val holders      = (reportData \ "accountHolder").as[play.api.libs.json.JsObject]
        val expected = survivingAnswers.copy(
          data = survivingAnswers.data + (
            reportId.mongoKey -> (reportData + ("accountHolder" -> (holders ++ holderPath)))
          )
        )

        val before = addAccount(expected, firstId, "REMOVE")
          .withPage(AccountOrderPage(reportId), Seq(firstId, secondId))
          .withPage(sharedLinks, Set(firstId, secondId))
          .withPage(soleLinks, Set(firstId))

        val result = service.removeAccount(before, reportId, firstId).success.value

        result.data mustBe expected.data
      }

      "must reject removal when the account exists only in a different report" in {
        val otherReport = reportId.copy(fiId = "another-fi")
        val answers     = addAccount(emptyUserAnswers, firstId, "OTHER-REPORT", otherReport)

        service.removeAccount(answers, reportId, firstId).failure.exception mustBe
          a[IllegalArgumentException]
      }
    }

    "recordAccount and orderedAccounts" - {

      "must keep accounts in creation order when an earlier account is revisited" in {
        val first         = addAccount(emptyUserAnswers, firstId, "FIRST")
        val recordedFirst = service.recordAccount(first, reportId, firstId).success.value

        val both         = addAccount(recordedFirst, secondId, "SECOND")
        val recordedBoth = service.recordAccount(both, reportId, secondId).success.value
        val revisited    = service.recordAccount(recordedBoth, reportId, firstId).success.value

        service.orderedAccounts(revisited, reportId) mustBe Seq(
          firstId  -> "FIRST",
          secondId -> "SECOND"
        )
      }
    }

    "selectCurrentAccount and clearCurrentAccount" - {

      "must change the selection and then clear it while keeping both accounts" in {
        val answers = addAccount(
          addAccount(emptyUserAnswers, firstId, "FIRST"),
          secondId,
          "SECOND"
        )

        val selected = service.selectCurrentAccount(answers, reportId, secondId).success.value
        selected.get(CurrentAccountIdPage()) mustBe Some(secondId)

        val cleared = service.clearCurrentAccount(selected, reportId).success.value
        cleared.data mustBe answers.data
      }
    }

    "isComplete" - {

      "must mark a FATCA account incomplete when payments are declared but none have been entered" in {
        val fatcaReport = reportId.copy(regime = FATCA)
        val complete = addAccount(emptyUserAnswers, firstId, "FIRST", fatcaReport)
          .withPage(AccountClosedPage(firstId)(fatcaReport), false)
          .withPage(
            WhatWasTheAccountBalancePage(firstId)(fatcaReport),
            AccountBalance(Currency.GBP, "123")
          )
          .withPage(HavePaymentsPage(firstId)(fatcaReport), false)

        service.isComplete(complete, fatcaReport, firstId) mustBe true

        val awaitingPayment = complete.withPage(HavePaymentsPage(firstId)(fatcaReport), true)
        service.isComplete(awaitingPayment, fatcaReport, firstId) mustBe false
      }

      "for a CRS account" - {
        import models.manual.account.{WasAccountOpen, WhatAccountType}

        val baseAnswers = addAccount(emptyUserAnswers, firstId, "CRS-ACCOUNT")
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

          val withBalance = open.withPage(WhatWasTheAccountBalancePage(firstId), balance)
          service.isComplete(withBalance, reportId, firstId) mustBe true
        }

        "must require a currency for a closed account even when a balance is saved" in {
          val closed = baseAnswers
            .withPage(AccountClosedPage(firstId), true)
            .withPage(WhatWasTheAccountBalancePage(firstId), balance)

          service.isComplete(closed, reportId, firstId) mustBe false

          val withCurrency = closed.withPage(WhatWasTheAccountCurrencyPage(firstId), Currency.GBP)
          service.isComplete(withCurrency, reportId, firstId) mustBe true
        }

        "must require the holder count when an account changes from sole to joint" in {
          val sole = baseAnswers
            .withPage(AccountClosedPage(firstId), false)
            .withPage(WhatWasTheAccountBalancePage(firstId), balance)

          service.isComplete(sole, reportId, firstId) mustBe true

          val joint = sole.withPage(IsJointAccountPage(firstId), true)
          service.isComplete(joint, reportId, firstId) mustBe false

          val withHolderCount = joint.withPage(HowManyJointAccountHoldersPage(firstId), 2)
          service.isComplete(withHolderCount, reportId, firstId) mustBe true
        }
      }
    }
  }
}
