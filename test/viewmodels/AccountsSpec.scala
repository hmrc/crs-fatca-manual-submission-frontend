/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels

import base.SpecBase
import models.ReportId
import models.SubmissionsConstants.CRS
import models.viewModels.{Account, AccountId, Accounts}
import pages.manual.account.AccountsPage

class AccountsSpec extends SpecBase {

  ".existingAccounts" - {

    "must return empty" in {

      val reportId = ReportId(regime = CRS, reportingYear = 2025, uploadedTime = None, fiId = "testFIID")

      val accounts = Accounts.existingAccounts(emptyUserAnswers, reportId)

      accounts mustBe Seq.empty

    }

    "must return AccountId & Account Number" in {

      val reportId = ReportId(regime = CRS, reportingYear = 2025, uploadedTime = None, fiId = "testFIID")

      val accountId1 = AccountId("testId1")
      val accountId2 = AccountId("testId2")
      val ua = emptyUserAnswers
        .withPage(
          AccountsPage(reportId),
          Accounts(
            currentAccountId = None,
            accounts = Map.apply(
              "testId1" -> Account(
                haveNumber = Some(false),
                identifier = Some("TestIdentifier"),
                accountId = accountId1
              ),
              "testId2" -> Account(
                haveNumber = Some(true),
                whatIsAccountNumber = Some("TestNumber"),
                accountId = accountId2
              )
            )
          )
        )

      val accounts = Accounts.existingAccounts(ua, reportId)

      accounts mustBe Seq(
        (
          accountId1,
          "TestIdentifier"
        ),
        (
          accountId2,
          "TestNumber"
        )
      )

    }

  }
}
