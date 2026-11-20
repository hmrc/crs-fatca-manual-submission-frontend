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
import models.manual.filercategory.WhatTypeOfFilerIsSponsor
import models.manual.filercategory.WhatTypeOfFilerIsSponsor.ForeignFI
import models.response.{Address, Country}
import models.viewModels.AccountId
import models.viewModels.TaskStatus.{CannotStartYet, Completed, Incomplete, NotStarted}
import models.{AccountBalance, AddressNonUk, Currency, ReportId, UkAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.ReportIdPage
import pages.manual.FINamePage
import pages.manual.account.*
import pages.manual.cpso.IsCpsoSectionCompletePage
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportingFI.HaveYouFinishedCheckingFIDetailsPage
import pages.manual.sponsor.*
import services.manual.account.AccountsService

class SendAReportServiceSpec extends SpecBase {
  val mockCpsoService = mock[CpsoService]
  val mockAccountService = mock[AccountsService]
  private val service    = new SendAReportService(mockAccountService, mockCpsoService)

  "SendAReportService" - {

    "reportingFIStatus" - {
      "should return incomplete when page is not answered" in {
        service.reportingFIStatus(emptyUserAnswers, reportId) mustBe Incomplete
      }

      "should return incomplete when page is answered as false" in {
        service.reportingFIStatus(emptyUserAnswers
                                    .withPage(HaveYouFinishedCheckingFIDetailsPage(reportId), false),
                                  reportId
        ) mustBe Incomplete
      }

      "should return completed when page is answered as true" in {
        service.reportingFIStatus(emptyUserAnswers
                                    .withPage(HaveYouFinishedCheckingFIDetailsPage(reportId), true),
                                  reportId
        ) mustBe Completed
      }
    }

    "reporting CPSO Complete Status" - {
      "should return incomplete when page is not answered" in {

        service.reportingCPSOCompleteStatus(emptyUserAnswers, reportId) mustBe Incomplete
      }

      "should return incomplete when page is answered as false" in {
        Mockito.when(mockCpsoService.areAllCpsosComplete(any())(any())).thenReturn(true)
        service.reportingCPSOCompleteStatus(emptyUserAnswers
                                              .withPage(IsCpsoSectionCompletePage(reportId), false),
                                            reportId
        ) mustBe Incomplete
      }

      "should return completed when page is answered as true" in {
        Mockito.when(mockCpsoService.areAllCpsosComplete(any())(any())).thenReturn(true)
        service.reportingCPSOCompleteStatus(emptyUserAnswers
                                              .withPage(IsCpsoSectionCompletePage(reportId), true),
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

      "should return incomplete when WhatTypeOfFilerIsSponsorPage page is answered" in {
        val answers = emptyUserAnswers
          .withPage(HaveSponsorPage()(reportId), true)
          .withPage(WhatTypeOfFilerIsSponsorPage()(reportId), ForeignFI)
        service.fillerStatus(answers, reportId) mustBe Completed
      }

      "should return incomplete when WhatTypeOfFilerPage page is answered" in {
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
        service.sponsorStatus(emptyUserAnswers.withPage(HaveSponsorPage()(reportId), false), reportId) mustBe Completed
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

    "accountsStatus" - {
      val report  = ReportId(FATCA, 2025, None, "testFiID")
      val firstId = AccountId("80")

      val noAccounts = emptyUserAnswers.withPage(ReportIdPage, report)

      val oneCompleteAccount = noAccounts
        .withPage(AccountIdPage(firstId)(report), firstId)
        .withPage(HaveNumberPage(firstId)(report), false)
        .withPage(IdentifierPage(firstId)(report), "FIRST-ACCOUNT")
        .withPage(AccountClosedPage(firstId)(report), false)
        .withPage(WhatWasTheAccountBalancePage(firstId)(report), AccountBalance(Currency.GBP, "100"))
        .withPage(HavePaymentsPage(firstId)(report), false)

      "should return NotStarted when page is not answered" in {
        service.accountStatus(noAccounts, report) mustBe NotStarted
      }

      "should return Completed when page are answered" in {
        when(mockAccountService.isComplete(any(), any(), any())).thenReturn(true)
        service.accountStatus(oneCompleteAccount, report) mustBe Completed
      }

      "should return InComplete when page is incomplete" in {
        when(mockAccountService.isComplete(any(), any(), any())).thenReturn(false)
        service.accountStatus(oneCompleteAccount, report) mustBe Incomplete
      }
    }
  }

}
