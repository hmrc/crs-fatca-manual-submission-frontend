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

package viewmodels.manual.accountHolders

import base.SpecBase
import models.SubmissionsConstants.FATCA
import models.manual.TaxResidentInformation
import models.manual.accountHolders.AccountHolderTypes.PassiveNonFinancialWithOneOrMoreControllingPerson
import models.manual.accountHolders.IndividualOrOrganisation.Organisation
import models.manual.accountHolders.SelfCertification
import models.response.Country
import models.viewModels.{AccountHolder, AccountHolderId}
import models.{NormalMode, UkAddress}

class AccountHolderRowSpec extends SpecBase {

  private val accountHolderId = AccountHolderId("acc1")

  private val completeAccountHolder = AccountHolder(
    individualOrOrganisation = Some(Organisation),
    organisationName = Some("someOrg"),
    accountHolderType = Some(PassiveNonFinancialWithOneOrMoreControllingPerson),
    areTheyBasedInTheUk = Some(true),
    selfCertification = Some(SelfCertification.Yes),
    taxResidentCountries = Some(
      Seq(TaxResidentInformation(Country.GB, Some(true), Some("someTin")))
    ),
    ukAddressManual = Some(UkAddress("Address", None, "Town", None, "AA1 1AA", "GB")),
    account = Some(Seq("account-id")),
    checkEquityInterestTypeRequired = Some(false)
  )
  private val incompleteAccountHolder = AccountHolder(individualOrOrganisation = Some(Organisation), organisationName = Some("someOrg"))

  "rows" - {

    "must create a list of AccountHolderRows for each given accountHolder" in {
      val accountHolderId2 = accountHolderId.copy(value = "acc2")
      val rows = AccountHolderRow.rows(Seq(accountHolderId -> completeAccountHolder, accountHolderId.copy(value = "acc2") -> completeAccountHolder), FATCA)

      rows mustBe Seq(
        AccountHolderRow(
          accountHolderName = "someOrg",
          changeUrl = controllers.manual.accountHolders.routes.CurrentAccountHolderIdController.onChangeRedirect(accountHolderId.value).url,
          removeUrl = controllers.manual.accountHolders.routes.CurrentAccountHolderIdController.onRemoveRedirect(accountHolderId.value).url
        ),
        AccountHolderRow(
          accountHolderName = "someOrg",
          changeUrl = controllers.manual.accountHolders.routes.CurrentAccountHolderIdController.onChangeRedirect(accountHolderId2.value).url,
          removeUrl = controllers.manual.accountHolders.routes.CurrentAccountHolderIdController.onRemoveRedirect(accountHolderId2.value).url
        )
      )
    }

    "must mark an incomplete account holder as incomplete with a different changeUrl" in {
      val rows = AccountHolderRow.rows(Seq(accountHolderId -> incompleteAccountHolder), FATCA)

      rows mustBe Seq(
        AccountHolderRow(
          accountHolderName = "someOrg",
          changeUrl = controllers.manual.accountHolders.routes.AccountController.onPageLoad(NormalMode).url,
          removeUrl = controllers.manual.accountHolders.routes.CurrentAccountHolderIdController.onRemoveRedirect(accountHolderId.value).url,
          isIncomplete = true
        )
      )
    }
  }
}
