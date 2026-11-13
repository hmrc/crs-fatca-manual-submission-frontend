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

package models.viewModels

import models.SubmissionsConstants.CRS
import models.{ReportId, UserAnswers}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.manual.account.AccountsPage
import pages.manual.accountHolders.AccountPage

class UnlinkedAccountsSpec extends AnyFreeSpec with Matchers with OptionValues {

  private given reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private def account(id: String, number: String): Account =
    Account(haveNumber = Some(true), whatIsAccountNumber = Some(number), accountId = AccountId(id))

  private def answersWith(
    accounts: Seq[Account],
    links: Map[String, Set[AccountId]] = Map.empty
  ): UserAnswers = {
    val base = UserAnswers("id")
      .set(
        AccountsPage(reportId),
        Accounts(
          currentAccountId = accounts.headOption.map(_.accountId),
          accounts = accounts
            .map(
              account => account.accountId.value -> account
            )
            .toMap
        )
      )
      .get

    links.foldLeft(base) {
      case (answers, (accountHolderId, accountIds)) =>
        answers.set(AccountPage(reportId, AccountHolderId(accountHolderId)), accountIds).get
    }
  }

  "UnlinkedAccounts.accountsWithoutAnAccountHolder" - {

    "must return the accounts that no account holder is linked to" in {

      val answers = answersWith(
        accounts = Seq(account("80", "GB33BUKB20201555555555"), account("82", "GB11AAAA11111111111111")),
        links = Map("21" -> Set(AccountId("80")))
      )

      UnlinkedAccounts.accountsWithoutAnAccountHolder(answers) mustBe Seq("GB11AAAA11111111111111")
    }

    "must return every account when no account holders have been added" in {

      val answers = answersWith(
        accounts = Seq(account("80", "GB33BUKB20201555555555"), account("82", "GB11AAAA11111111111111"))
      )

      UnlinkedAccounts.accountsWithoutAnAccountHolder(answers).sorted mustBe
        Seq("GB11AAAA11111111111111", "GB33BUKB20201555555555")
    }

    "must return no accounts when every account is linked to an account holder" in {

      val answers = answersWith(
        accounts = Seq(account("80", "GB33BUKB20201555555555"), account("82", "GB11AAAA11111111111111")),
        links = Map("21" -> Set(AccountId("80"), AccountId("82")))
      )

      UnlinkedAccounts.accountsWithoutAnAccountHolder(answers) mustBe empty
    }

    "must treat accounts linked by different account holders as linked" in {

      val answers = answersWith(
        accounts = Seq(account("80", "GB33BUKB20201555555555"), account("82", "GB11AAAA11111111111111")),
        links = Map(
          "21" -> Set(AccountId("80")),
          "22" -> Set(AccountId("82"))
        )
      )

      UnlinkedAccounts.accountsWithoutAnAccountHolder(answers) mustBe empty
    }

    "must fall back to the account identifier when no account number has been entered" in {

      val identifierOnly = Account(
        haveNumber = Some(false),
        identifier = Some("IDENTIFIER-1"),
        whatIsAccountNumber = None,
        accountId = AccountId("83")
      )

      val answers = answersWith(accounts = Seq(identifierOnly))

      UnlinkedAccounts.accountsWithoutAnAccountHolder(answers) mustBe Seq("IDENTIFIER-1")
    }

    "must return an empty string for an account with neither a number nor an identifier" in {

      val blankAccount = Account(accountId = AccountId("84"))

      val answers = answersWith(accounts = Seq(blankAccount))

      UnlinkedAccounts.accountsWithoutAnAccountHolder(answers) mustBe Seq("")
    }

    "must return no accounts when the report has no accounts at all" in {

      UnlinkedAccounts.accountsWithoutAnAccountHolder(UserAnswers("id")) mustBe empty
    }
  }
}
