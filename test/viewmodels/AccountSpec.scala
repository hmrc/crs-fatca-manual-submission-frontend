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
import models.viewModels.{Account, AccountId}

class AccountSpec extends SpecBase {

  ".accountNumber" - {

    "must return None when HaveNumber is None" in {

      val account = Account(
        haveNumber = None,
        identifier = None,
        numberType = None,
        whatIsAccountNumber = None,
        accountId = AccountId("TestAccountId")
      )

      account.accountNumber mustBe None

    }

    "must return Identifier Value  when HaveNumber is false & Identifier value present" in {

      val account = Account(
        haveNumber = Some(false),
        identifier = Some("testIdentifier"),
        numberType = None,
        whatIsAccountNumber = None,
        accountId = AccountId("TestAccountId")
      )

      account.accountNumber mustBe Some("testIdentifier")

    }

    "must return Identifier Value  when HaveNumber is true & AccountNumber value present" in {

      val account = Account(
        haveNumber = Some(true),
        identifier = None,
        numberType = None,
        whatIsAccountNumber = Some("testNumber"),
        accountId = AccountId("TestAccountId")
      )

      account.accountNumber mustBe Some("testNumber")

    }

    "must return None when missing answers - WhatIsAccountNumber" in {

      val account = Account(
        haveNumber = Some(true),
        identifier = None,
        numberType = None,
        whatIsAccountNumber = None,
        accountId = AccountId("TestAccountId")
      )

      account.accountNumber mustBe None

    }

    "must return None when missing answers - identifier" in {

      val account = Account(
        haveNumber = Some(false),
        identifier = None,
        numberType = None,
        whatIsAccountNumber = None,
        accountId = AccountId("TestAccountId")
      )

      account.accountNumber mustBe None

    }

  }
}
