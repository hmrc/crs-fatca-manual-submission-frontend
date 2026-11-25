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

import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.TaxResidentInformation
import models.manual.account.WhatAccountType
import models.manual.accountHolders.AccountHolderTypes
import models.manual.accountHolders.AccountHolderTypes.PassiveNonFinancialWithOneOrMoreControllingPerson
import models.manual.accountHolders.EquityInterestType
import models.manual.accountHolders.EquityInterestTypes
import models.manual.accountHolders.IndividualDateOfBirth
import models.manual.accountHolders.IndividualName
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.manual.accountHolders.IndividualPlaceOfBirth
import models.manual.accountHolders.SelfCertification
import models.response.Country
import models.viewModels.{Account, AccountHolder, AccountHolderId, AccountHolders, AccountId, Accounts}
import models.{AddressNonUk, ReportId, UkAddress, UserAnswers}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.manual.account.AccountsPage
import pages.manual.accountHolders.AccountHoldersPage

import java.time.Instant

class AccountHolderServiceSpec extends AnyFreeSpec with Matchers with OptionValues {

  private val service = new AccountHolderService()

  private val reportId      = ReportId(CRS, 2025, None, "TestfiID")
  private val fatcaReportId = ReportId(FATCA, 2025, None, "TestfiID")

  private val oldestCreated = Instant.parse("2025-03-01T09:00:00Z")
  private val middleCreated = Instant.parse("2025-08-15T12:30:00Z")
  private val newestCreated = Instant.parse("2026-01-20T16:45:00Z")

  private def organisationHolder(
    name: String,
    account: Option[Seq[String]] = None,
    creationDateTime: Option[Instant] = None
  ): AccountHolder =
    AccountHolder(
      individualOrOrganisation = Some(Organisation),
      organisationName = Some(name),
      accountHolderType = Some(PassiveNonFinancialWithOneOrMoreControllingPerson),
      account = account,
      creationDateTime = creationDateTime
    )

  private val unnamedHolder: AccountHolder =
    AccountHolder(individualOrOrganisation = Some(Individual))

  private def account(id: String, accountType: Option[WhatAccountType]): Account =
    Account(
      whatIsAccountNumber = Some(s"ACC-$id"),
      whatAccountType = accountType,
      accountId = AccountId(id)
    )

  private def userAnswersWith(
    holders: Map[String, AccountHolder] = Map.empty,
    accounts: Seq[Account] = Seq.empty,
    reportId: ReportId = reportId
  ): UserAnswers = {
    val withHolders = UserAnswers("id").set(AccountHoldersPage()(reportId), AccountHolders(None, holders)).get

    if accounts.isEmpty then withHolders
    else
      withHolders
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
  }

  private val ukAddress =
    UkAddress(
      addressLine1 = "1 High Street",
      addressLine2 = None,
      city = "London",
      postcode = "AA1 1AA",
      country = "GB"
    )

  private val nonUkAddress =
    AddressNonUk(
      addressLine1 = "1 Rue de Rivoli",
      addressLine2 = None,
      addressLine3 = "Paris",
      addressLine4 = None,
      postcode = None,
      country = "FR"
    )

  private val taxResident =
    TaxResidentInformation(
      country = Country.GB,
      hasTaxIdentificationNumber = Some(true),
      taxIdentifierNumber = Some("TIN-123")
    )

  private def completeOrganisationHolder(
    name: String = "Alpha Holdings",
    accountHolderType: Option[AccountHolderTypes] = Some(PassiveNonFinancialWithOneOrMoreControllingPerson),
    account: Option[Seq[String]] = Some(Seq("80")),
    areTheyBasedInTheUk: Option[Boolean] = Some(true),
    ukAddressManual: Option[UkAddress] = Some(ukAddress),
    addressNonUk: Option[AddressNonUk] = None,
    taxResidentCountries: Option[Seq[TaxResidentInformation]] = Some(Seq(taxResident)),
    selfCertification: Option[SelfCertification] = Some(SelfCertification.Yes),
    provideEquityInterestType: Option[Boolean] = None,
    equityInterestType: Option[EquityInterestTypes] = None
  ): AccountHolder =
    AccountHolder(
      individualOrOrganisation = Some(Organisation),
      organisationName = Some(name),
      accountHolderType = accountHolderType,
      account = account,
      areTheyBasedInTheUk = areTheyBasedInTheUk,
      ukAddressManual = ukAddressManual,
      addressNonUk = addressNonUk,
      taxResidentCountries = taxResidentCountries,
      selfCertification = selfCertification,
      provideEquityInterestType = provideEquityInterestType,
      equityInterestType = equityInterestType
    )

  private def completeIndividualHolder(
    firstName: String = "First Name",
    lastName: String = "Last Name",
    account: Option[Seq[String]] = Some(Seq("80")),
    areTheyBasedInTheUk: Option[Boolean] = Some(true),
    ukAddressManual: Option[UkAddress] = Some(ukAddress),
    addressNonUk: Option[AddressNonUk] = None,
    taxResidentCountries: Option[Seq[TaxResidentInformation]] = Some(Seq(taxResident)),
    selfCertification: Option[SelfCertification] = Some(SelfCertification.Yes),
    individualHavePlaceOfBirth: Option[Boolean] = Some(false),
    individualPlaceOfBirth: Option[IndividualPlaceOfBirth] = None,
    individualHaveDateOfBirth: Option[Boolean] = Some(false),
    individualDateOfBirth: Option[IndividualDateOfBirth] = None
  ): AccountHolder =
    AccountHolder(
      individualOrOrganisation = Some(Individual),
      individualName = Some(IndividualName(firstName, lastName)),
      account = account,
      areTheyBasedInTheUk = areTheyBasedInTheUk,
      ukAddressManual = ukAddressManual,
      addressNonUk = addressNonUk,
      taxResidentCountries = taxResidentCountries,
      selfCertification = selfCertification,
      individualHavePlaceOfBirth = individualHavePlaceOfBirth,
      individualPlaceOfBirth = individualPlaceOfBirth,
      individualHaveDateOfBirth = individualHaveDateOfBirth,
      individualDateOfBirth = individualDateOfBirth
    )

  "AccountHolderService.accountHolders" - {

    "must return only account holders that have a name" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> organisationHolder("Alpha Holdings"),
          "22" -> unnamedHolder
        )
      )

      val result = service.accountHolders(answers)(using reportId)

      result.map(_._1) mustBe Seq(AccountHolderId("21"))
      result.map(_._2.getName()) mustBe Seq("Alpha Holdings")
    }

    "must order account holders by creationDateTime, oldest first" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> organisationHolder("Newest Holdings", creationDateTime = Some(newestCreated)),
          "22" -> organisationHolder("Oldest Holdings", creationDateTime = Some(oldestCreated)),
          "23" -> organisationHolder("Middle Holdings", creationDateTime = Some(middleCreated))
        )
      )

      val result = service.accountHolders(answers)(using reportId)

      result.map(_._2.getName()) mustBe Seq("Oldest Holdings", "Middle Holdings", "Newest Holdings")
    }

    "must list account holders without a creationDateTime before those that have one" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> organisationHolder("Newest Holdings", creationDateTime = Some(newestCreated)),
          "22" -> organisationHolder("Undated Holdings"),
          "23" -> organisationHolder("Oldest Holdings", creationDateTime = Some(oldestCreated))
        )
      )

      val result = service.accountHolders(answers)(using reportId)

      result.map(_._2.getName()) mustBe Seq("Undated Holdings", "Oldest Holdings", "Newest Holdings")
    }

    "must require an equity interest type when a linked account is an investment entity" in {

      val answers = userAnswersWith(
        holders = Map("21" -> organisationHolder("Alpha Holdings", account = Some(Seq("80")))),
        accounts = Seq(account("80", Some(WhatAccountType.InvestmentEntity)))
      )

      val result = service.accountHolders(answers)(using reportId)

      result.map(_._2.checkEquityInterestTypeRequired).headOption mustBe Some(Some(true))
    }

    "must not require an equity interest type when no linked account is an investment entity" in {

      val answers = userAnswersWith(
        holders = Map("21" -> organisationHolder("Alpha Holdings", account = Some(Seq("80")))),
        accounts = Seq(account("80", Some(WhatAccountType.Custodial)))
      )

      val result = service.accountHolders(answers)(using reportId)

      result.map(_._2.checkEquityInterestTypeRequired).headOption mustBe Some(Some(false))
    }

    "must check every linked account, not just the first" in {

      val answers = userAnswersWith(
        holders = Map("21" -> organisationHolder("Alpha Holdings", account = Some(Seq("80", "81")))),
        accounts = Seq(
          account("80", Some(WhatAccountType.Custodial)),
          account("81", Some(WhatAccountType.InvestmentEntity))
        )
      )

      val result = service.accountHolders(answers)(using reportId)

      result.map(_._2.checkEquityInterestTypeRequired).headOption mustBe Some(Some(true))
    }

    "must leave the equity interest requirement unset when no accounts are linked" in {

      val answers = userAnswersWith(
        holders = Map("21" -> organisationHolder("Alpha Holdings")),
        accounts = Seq(account("80", Some(WhatAccountType.InvestmentEntity)))
      )

      val result = service.accountHolders(answers)(using reportId)

      result.map(_._2.checkEquityInterestTypeRequired).headOption mustBe Some(None)
    }

    "must not apply the equity interest requirement to a FATCA report" in {

      val answers = userAnswersWith(
        holders = Map("21" -> organisationHolder("Alpha Holdings", account = Some(Seq("80")))),
        accounts = Seq(account("80", Some(WhatAccountType.InvestmentEntity))),
        reportId = fatcaReportId
      )

      val result = service.accountHolders(answers)(using fatcaReportId)

      result.map(_._2.checkEquityInterestTypeRequired).headOption mustBe Some(None)
    }

    "must return no account holders when none have been added" in {
      service.accountHolders(UserAnswers("id"))(using reportId) mustBe empty
    }
  }

  "AccountHolderService.areAllAccountHoldersComplete" - {

    "must return false when there are no account holders" in {

      service.areAllAccountHoldersComplete(UserAnswers("id"), reportId) mustBe false
    }

    "must return true when every account holder is complete" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> completeOrganisationHolder(),
          "22" -> completeIndividualHolder()
        )
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe true
    }

    "must return false when any account holder is incomplete" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> completeOrganisationHolder(),
          "22" -> completeOrganisationHolder(name = "Incomplete Holdings", ukAddressManual = None)
        )
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe false
    }

    "must ignore account holders without a name" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> completeOrganisationHolder(),
          "22" -> unnamedHolder
        )
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe true
    }

    "must return false when an account holder has no linked account" in {

      val answers = userAnswersWith(
        holders = Map("21" -> completeOrganisationHolder(account = Some(Seq.empty)))
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe false
    }

    "must return false when an account holder has no address" in {

      val answers = userAnswersWith(
        holders = Map("21" -> completeOrganisationHolder(ukAddressManual = None))
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe false
    }

    "must return false when an account holder has no tax resident countries" in {

      val answers = userAnswersWith(
        holders = Map("21" -> completeOrganisationHolder(taxResidentCountries = Some(Seq.empty)))
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe false
    }

    "must return true for a complete individual account holder" in {

      val answers = userAnswersWith(holders = Map("21" -> completeIndividualHolder()))

      service.areAllAccountHoldersComplete(answers, reportId) mustBe true
    }

    "must return true for an individual based outside the UK with a non-UK address" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> completeIndividualHolder(
            areTheyBasedInTheUk = Some(false),
            ukAddressManual = None,
            addressNonUk = Some(nonUkAddress)
          )
        )
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe true
    }

    "must return false when an individual is missing their place of birth details" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> completeIndividualHolder(
            individualHavePlaceOfBirth = Some(true),
            individualPlaceOfBirth = None
          )
        )
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe false
    }

    "must return false when an individual is missing their date of birth details" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> completeIndividualHolder(
            individualHaveDateOfBirth = Some(true),
            individualDateOfBirth = None
          )
        )
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe false
    }

    "must require self-certification for a CRS report" in {

      val answers = userAnswersWith(
        holders = Map("21" -> completeOrganisationHolder(selfCertification = None))
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe false
    }

    "must require an equity interest type when a linked account is an investment entity" in {

      val answers = userAnswersWith(
        holders = Map("21" -> completeOrganisationHolder(account = Some(Seq("80")))),
        accounts = Seq(account("80", Some(WhatAccountType.InvestmentEntity)))
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe false
    }

    "must return true when the equity interest type is provided" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> completeOrganisationHolder(
            account = Some(Seq("80")),
            provideEquityInterestType = Some(true),
            equityInterestType = Some(EquityInterestTypes(Seq(EquityInterestType.BeneficiaryOfTrust)))
          )
        ),
        accounts = Seq(account("80", Some(WhatAccountType.InvestmentEntity)))
      )

      service.areAllAccountHoldersComplete(answers, reportId) mustBe true
    }

    "must not require self-certification or equity interest details for a FATCA report" in {

      val answers = userAnswersWith(
        holders = Map(
          "21" -> completeOrganisationHolder(
            selfCertification = None,
            account = Some(Seq("80"))
          )
        ),
        accounts = Seq(account("80", Some(WhatAccountType.InvestmentEntity))),
        reportId = fatcaReportId
      )

      service.areAllAccountHoldersComplete(answers, fatcaReportId) mustBe true
    }
  }
}
