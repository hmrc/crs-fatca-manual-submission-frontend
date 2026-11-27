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
import models.SubmissionsConstants.FATCA
import models.manual.filercategory.WhatTypeOfFiler.Foreign
import models.manual.filercategory.WhatTypeOfFilerIsSponsor.ForeignFI
import models.response.{Address, Country}
import models.viewModels.AccountId
import models.viewModels.TaskStatus.{CannotStartYet, Completed, Incomplete}
import models.{AccountBalance, AddressNonUk, Currency, ReportId, UkAddress}
import pages.ReportIdPage
import pages.manual.FINamePage
import pages.manual.account.*
import pages.manual.cpso.IsCpsoSectionCompletePage
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportingFI.HaveYouFinishedCheckingFIDetailsPage
import pages.manual.sponsor.*
import services.manual.account.AccountsService

class SendAReportServiceSpec extends SpecBase {

  private val service = new SendAReportService(new AccountsService())

  "SendAReportService" - {

    "reportingFIStatus" - {

      "should return incomplete when page is not answered" in {
        service.reportingFIStatus(emptyUserAnswers, reportId) mustBe Incomplete
      }

      "should return incomplete when page is answered as false" in {
        service.reportingFIStatus(
          emptyUserAnswers.withPage(HaveYouFinishedCheckingFIDetailsPage(reportId), false),
          reportId
        ) mustBe Incomplete
      }

      "should return completed when page is answered as true" in {
        service.reportingFIStatus(
          emptyUserAnswers.withPage(HaveYouFinishedCheckingFIDetailsPage(reportId), true),
          reportId
        ) mustBe Completed
      }
    }

    "reporting CPSO Complete Status" - {

      "should return incomplete when page is not answered" in {
        service.reportingCPSOCompleteStatus(emptyUserAnswers, reportId) mustBe Incomplete
      }

      "should return incomplete when page is answered as false" in {
        service.reportingCPSOCompleteStatus(
          emptyUserAnswers.withPage(IsCpsoSectionCompletePage(reportId), false),
          reportId
        ) mustBe Incomplete
      }

      "should return completed when page is answered as true" in {
        service.reportingCPSOCompleteStatus(
          emptyUserAnswers.withPage(IsCpsoSectionCompletePage(reportId), true),
          reportId
        ) mustBe Completed
      }
    }

    "fillerStatus" - {

      "should return CannotStartYet when haveSponsor page is not answered" in {
        service.fillerStatus(emptyUserAnswers, reportId) mustBe CannotStartYet
      }

      "should return incomplete when WhatTypeOfFilerIsSponsorPage page is not answered" in {
        val answers = emptyUserAnswers.withPage(HaveSponsorPage()(reportId), true)

        service.fillerStatus(answers, reportId) mustBe Incomplete
      }

      "should return incomplete when WhatTypeOfFilerPage page is not answered" in {
        val answers = emptyUserAnswers.withPage(HaveSponsorPage()(reportId), false)

        service.fillerStatus(answers, reportId) mustBe Incomplete
      }

      "should return completed when WhatTypeOfFilerIsSponsorPage page is answered" in {
        val answers = emptyUserAnswers
          .withPage(HaveSponsorPage()(reportId), true)
          .withPage(WhatTypeOfFilerIsSponsorPage()(reportId), ForeignFI)

        service.fillerStatus(answers, reportId) mustBe Completed
      }

      "should return completed when WhatTypeOfFilerPage page is answered" in {
        val answers = emptyUserAnswers
          .withPage(HaveSponsorPage()(reportId), false)
          .withPage(WhatTypeOfFilerPage()(reportId), Foreign)

        service.fillerStatus(answers, reportId) mustBe Completed
      }
    }

    "sponsorStatus" - {

      "should return Incomplete when haveSponsor page is not answered" in {
        service.sponsorStatus(emptyUserAnswers, reportId) mustBe Incomplete
      }

      "should return Completed when haveSponsor page is answered as no" in {
        service.sponsorStatus(
          emptyUserAnswers.withPage(HaveSponsorPage()(reportId), false),
          reportId
        ) mustBe Completed
      }

      "should return Incomplete when haveSponsor page is yes and SponsorName is missing" in {
        val userAnswers = emptyUserAnswers
          .withPage(HaveSponsorPage()(reportId), true)
          .withPage(WhatIsGIINForSponsorPage(), "alphvA.zDSJH.HV.255")
          .withPage(UkAddressPage(), UkAddress("address line 1", None, "city", None, "postcode", "GB"))
          .withPage(TaxResidentCountriesListPage(), Seq(Country("XX", "Test Country")))
          .withPage(FINamePage(), "TestFI")

        service.sponsorStatus(userAnswers, reportId) mustBe Incomplete
      }

      "should return Incomplete when haveSponsor page is yes and GIIN is missing" in {
        val userAnswers = emptyUserAnswers
          .withPage(HaveSponsorPage()(reportId), true)
          .withPage(SponsorNamePage(), "Test Sponsor")
          .withPage(UkAddressPage(), UkAddress("address line 1", None, "city", None, "postcode", "GB"))
          .withPage(TaxResidentCountriesListPage(), Seq(Country("XX", "Test Country")))
          .withPage(FINamePage(), "TestFI")

        service.sponsorStatus(userAnswers, reportId) mustBe Incomplete
      }

      "should return Incomplete when haveSponsor page is yes and Address is missing" in {
        val userAnswers = emptyUserAnswers
          .withPage(HaveSponsorPage()(reportId), true)
          .withPage(SponsorNamePage(), "Test Sponsor")
          .withPage(WhatIsGIINForSponsorPage(), "alphvA.zDSJH.HV.255")
          .withPage(TaxResidentCountriesListPage(), Seq(Country("XX", "Test Country")))
          .withPage(FINamePage(), "TestFI")

        service.sponsorStatus(userAnswers, reportId) mustBe Incomplete
      }

      "should return Incomplete when haveSponsor page is yes and TaxResidentCountriesListPage is missing" in {
        val userAnswers = emptyUserAnswers
          .withPage(HaveSponsorPage()(reportId), true)
          .withPage(SponsorNamePage(), "Test Sponsor")
          .withPage(WhatIsGIINForSponsorPage(), "alphvA.zDSJH.HV.255")
          .withPage(UkAddressPage(), UkAddress("address line 1", None, "city", None, "postcode", "GB"))
          .withPage(FINamePage(), "TestFI")

        service.sponsorStatus(userAnswers, reportId) mustBe Incomplete
      }

      "should return Completed when haveSponsor page is yes and all pages are answered - UKAddress" in {
        val userAnswers = emptyUserAnswers
          .withPage(HaveSponsorPage(), true)
          .withPage(SponsorNamePage(), "Test Sponsor")
          .withPage(WhatIsGIINForSponsorPage(), "alphvA.zDSJH.HV.255")
          .withPage(UkAddressPage(), UkAddress("address line 1", None, "city", None, "postcode", "GB"))
          .withPage(TaxResidentCountriesListPage(), Seq(Country("XX", "Test Country")))

        service.sponsorStatus(userAnswers, reportId) mustBe Completed
      }

      "should return Completed when haveSponsor page is yes and all pages are answered - NonUKAddress" in {
        val addressNonUk = AddressNonUk(addressLine1 = "some address", None, "", None, None, "GB")

        val userAnswers = emptyUserAnswers
          .withPage(HaveSponsorPage(), true)
          .withPage(SponsorNamePage(), "Test Sponsor")
          .withPage(WhatIsGIINForSponsorPage(), "alphvA.zDSJH.HV.255")
          .withPage(AddressNonUkPage(), addressNonUk)
          .withPage(TaxResidentCountriesListPage(), Seq(Country("XX", "Test Country")))

        service.sponsorStatus(userAnswers, reportId) mustBe Completed
      }

      "should return Completed when haveSponsor page is yes and all pages are answered - AddressSelect" in {
        val ukAddressSelect = Address(None, "addressLine1", None, None, None, "town", None, Country.GB)

        val userAnswers = emptyUserAnswers
          .withPage(HaveSponsorPage(), true)
          .withPage(SponsorNamePage(), "Test Sponsor")
          .withPage(WhatIsGIINForSponsorPage(), "alphvA.zDSJH.HV.255")
          .withPage(WhatIsAddressForSponsorPage(), ukAddressSelect)
          .withPage(TaxResidentCountriesListPage(), Seq(Country("XX", "Test Country")))

        service.sponsorStatus(userAnswers, reportId) mustBe Completed
      }
    }

    "accountStatus" - {
      val report   = ReportId(FATCA, 2025, None, "testFiID")
      val firstId  = AccountId("80")
      val secondId = AccountId("12")

      val finishedPage = IsAccountsSectionCompletedPage(report)
      val noAccounts   = emptyUserAnswers.withPage(ReportIdPage, report)

      val oneCompleteAccount = noAccounts
        .withPage(AccountIdPage(firstId)(report), firstId)
        .withPage(HaveNumberPage(firstId)(report), false)
        .withPage(IdentifierPage(firstId)(report), "FIRST-ACCOUNT")
        .withPage(AccountClosedPage(firstId)(report), false)
        .withPage(WhatWasTheAccountBalancePage(firstId)(report), AccountBalance(Currency.GBP, "100"))
        .withPage(HavePaymentsPage(firstId)(report), false)

      val twoCompleteAccounts = oneCompleteAccount
        .withPage(AccountIdPage(secondId)(report), secondId)
        .withPage(HaveNumberPage(secondId)(report), false)
        .withPage(IdentifierPage(secondId)(report), "SECOND-ACCOUNT")
        .withPage(AccountClosedPage(secondId)(report), false)
        .withPage(WhatWasTheAccountBalancePage(secondId)(report), AccountBalance(Currency.GBP, "200"))
        .withPage(HavePaymentsPage(secondId)(report), false)

      "should return Incomplete when there are no accounts" in {
        service.accountStatus(noAccounts, report) mustBe Incomplete
      }

      "should return Incomplete when there are no accounts even with a saved Yes" in {
        service.accountStatus(noAccounts.withPage(finishedPage, true), report) mustBe Incomplete
      }

      "should return Incomplete when all accounts are complete but confirmation is unanswered" in {
        service.accountStatus(oneCompleteAccount, report) mustBe Incomplete
      }

      "should return Incomplete when all accounts are complete but confirmation is No" in {
        service.accountStatus(oneCompleteAccount.withPage(finishedPage, false), report) mustBe Incomplete
      }

      "should return Completed when the account is complete and confirmation is Yes" in {
        service.accountStatus(oneCompleteAccount.withPage(finishedPage, true), report) mustBe Completed
      }

      "should return Completed when multiple accounts are complete and confirmation is Yes" in {
        service.accountStatus(twoCompleteAccounts.withPage(finishedPage, true), report) mustBe Completed
      }

      "should return Incomplete when a previously completed account requires a payment after a saved Yes" in {
        val confirmedAnswers = oneCompleteAccount.withPage(finishedPage, true)

        service.accountStatus(confirmedAnswers, report) mustBe Completed

        val updatedAnswers = confirmedAnswers
          .withPage(HavePaymentsPage(firstId)(report), true)

        service.accountStatus(updatedAnswers, report) mustBe Incomplete
      }

      "should return Incomplete when an unlabelled account is added after a saved Yes" in {
        val confirmedAnswers = oneCompleteAccount.withPage(finishedPage, true)

        val updatedAnswers = confirmedAnswers
          .withPage(AccountIdPage(secondId)(report), secondId)
          .withPage(HaveNumberPage(secondId)(report), false)

        service.accountStatus(updatedAnswers, report) mustBe Incomplete
      }

      "should return Incomplete when one of multiple accounts is incomplete despite a saved Yes" in {
        val answers = twoCompleteAccounts
          .withPage(finishedPage, true)
          .withPage(HavePaymentsPage(secondId)(report), true)

        service.accountStatus(answers, report) mustBe Incomplete
      }

      "should reuse the saved Yes after an incomplete account is completed again" in {
        val confirmed  = oneCompleteAccount.withPage(finishedPage, true)
        val incomplete = confirmed.withPage(HavePaymentsPage(firstId)(report), true)

        service.accountStatus(incomplete, report) mustBe Incomplete
        service.accountStatus(
          incomplete.withPage(HavePaymentsPage(firstId)(report), false),
          report
        ) mustBe Completed
      }

      "should use the confirmation answer for the selected report" in {
        val otherReport = report.copy(fiId = "anotherFiID")

        val answers = oneCompleteAccount
          .withPage(IsAccountsSectionCompletedPage(otherReport), true)

        service.accountStatus(answers, report) mustBe Incomplete
      }
    }

    "accountHolderStatus pending DAC6-4525" - {

      "must unlock only after accounts are completed" in {
        service.accountHolderStatus(Incomplete) mustBe CannotStartYet
        service.accountHolderStatus(Completed) mustBe Incomplete
      }
    }

    "cpsoStatus" - {
      import models.SubmissionsConstants.CRS
      import models.manual.accountHolders.{AccountHolderTypes, IndividualOrOrganisation}
      import models.viewModels.{AccountHolder, AccountHolders}
      import pages.manual.accountHolders.AccountHoldersPage

      "must hide the row without holders, even with a stale confirmation" in {
        service.cpsoStatus(
          emptyUserAnswers.withPage(IsCpsoSectionCompletePage(reportId), true),
          reportId
        ) mustBe None
      }

      "must hide the row when the holder type is unknown" in {
        val answers = emptyUserAnswers.withPage(
          AccountHoldersPage()(reportId),
          AccountHolders(None, Map("1" -> AccountHolder()))
        )

        service.cpsoStatus(answers, reportId) mustBe None
      }

      "must use the merged confirmation behaviour for an eligible holder" in {
        val holder = AccountHolder(
          individualOrOrganisation = Some(IndividualOrOrganisation.Organisation),
          accountHolderType = Some(
            AccountHolderTypes.PassiveNonFinancialWithOneOrMoreControllingPerson
          )
        )

        val report = reportId.copy(regime = CRS)

        val answers = emptyUserAnswers.withPage(
          AccountHoldersPage()(report),
          AccountHolders(None, Map("1" -> holder))
        )

        service.cpsoStatus(
          answers.withPage(IsCpsoSectionCompletePage(report), true),
          report
        ) mustBe Some(Completed)

        service.cpsoStatus(
          answers.withPage(IsCpsoSectionCompletePage(report), false),
          report
        ) mustBe Some(Incomplete)
      }

      Seq(
        (
          CRS,
          AccountHolder(individualOrOrganisation = Some(IndividualOrOrganisation.Individual)),
          None
        ),
        (
          CRS,
          AccountHolder(
            individualOrOrganisation = Some(IndividualOrOrganisation.Organisation),
            accountHolderType = Some(AccountHolderTypes.CrsReportablePerson)
          ),
          None
        ),
        (
          CRS,
          AccountHolder(
            individualOrOrganisation = Some(IndividualOrOrganisation.Organisation),
            accountHolderType = Some(AccountHolderTypes.PassiveNonFinancialEntityThatIsCRSReportable)
          ),
          None
        ),
        (
          FATCA,
          AccountHolder(
            individualOrOrganisation = Some(IndividualOrOrganisation.Organisation),
            accountHolderType = Some(AccountHolderTypes.NonParticipatingForeignFI)
          ),
          None
        ),
        (
          FATCA,
          AccountHolder(
            individualOrOrganisation = Some(IndividualOrOrganisation.Organisation),
            accountHolderType = Some(AccountHolderTypes.SpecifiedUSPerson)
          ),
          None
        ),
        (
          CRS,
          AccountHolder(
            individualOrOrganisation = Some(IndividualOrOrganisation.Organisation),
            accountHolderType = Some(AccountHolderTypes.PassiveNonFinancialWithOneOrMoreControllingPerson)
          ),
          Some(Incomplete)
        ),
        (
          FATCA,
          AccountHolder(
            individualOrOrganisation = Some(IndividualOrOrganisation.Organisation),
            accountHolderType = Some(AccountHolderTypes.OwnerDocumentedForeignFinancialInstitution)
          ),
          Some(Incomplete)
        ),
        (
          FATCA,
          AccountHolder(
            individualOrOrganisation = Some(IndividualOrOrganisation.Organisation),
            accountHolderType = Some(AccountHolderTypes.PassiveNFFEWithSubstantial)
          ),
          Some(Incomplete)
        )
      ).foreach {
        case (regime, holder, expected) =>
          s"must return $expected for $regime ${holder.individualOrOrganisation} ${holder.accountHolderType}" in {
            val report = reportId.copy(regime = regime)

            val answers = emptyUserAnswers.withPage(
              AccountHoldersPage()(report),
              AccountHolders(None, Map("1" -> holder))
            )

            service.cpsoStatus(answers, report) mustBe expected
          }
      }
    }
  }
}
