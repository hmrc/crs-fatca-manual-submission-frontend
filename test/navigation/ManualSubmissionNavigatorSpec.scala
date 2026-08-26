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

package navigation

import base.SpecBase
import controllers.manual.reportdetails.routes.{ReportDetailsCheckAnswersController, ReportingYearController, TypeOfReportController}
import controllers.routes
import models.*
import models.CrsOrFatca.Fatca
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.account.WasAccountOpen
import models.manual.cpso.IndividualOrOrganisation
import models.manual.accountHolders.IndividualName
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.response.{Address, AddressLookup, Country}
import models.viewModels.{AccountHolderId, AccountId}
import models.viewModels.manual.cpso.CPSOId
import pages.*
import pages.manual.account.*
import pages.manual.accountHolders.{IndividualNamePage, IndividualOrOrganisationPage}
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import pages.manual.sponsor.*

class ManualSubmissionNavigatorSpec extends SpecBase {

  val navigator = new ManualSubmissionNavigator

  "ManualSubmissionNavigator in NormalMode" - {
    "nextPageWithoutReportId" - {
      "CrsOrFatcaPage" - {
        "must go to Reporting Year Page when Normal Mode" in {
          navigator.nextPageWithoutReportId(CrsOrFatcaPage, NormalMode) mustBe
            ReportingYearController.onPageLoad(NormalMode)
        }

      }
      "ReportingYearPage" - {
        "must go to TypeOfReport Page when Normal Mode" in {
          navigator.nextPageWithoutReportId(ReportingYearPage, NormalMode) mustBe
            TypeOfReportController.onPageLoad(NormalMode)
        }

      }
      "TypeOfReportPage" - {
        "must go to ReportDetailsCheckAnswers" in {
          navigator.nextPageWithoutReportId(TypeOfReportPage, NormalMode) mustBe
            ReportDetailsCheckAnswersController.onPageLoad()
        }

      }
    }
    "with reportId" - {
      implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
      val accountId                   = AccountId("TestAccountId")

      "HaveSponsorPage" - {
        "must go to SponsorName Page when answer is Yes" in {
          val userData = UserAnswers("id").withPage(HaveSponsorPage(), true)
          navigator.nextPage(HaveSponsorPage(), NormalMode, userData) mustBe
            controllers.manual.sponsor.routes.SponsorNameController.onPageLoad(NormalMode)
        }

        "must go to CheckAnswers Page when when answer is No" in {
          val userData = UserAnswers("id").withPage(HaveSponsorPage(), false)
          navigator.nextPage(HaveSponsorPage(), NormalMode, userData) mustBe
            controllers.manual.sponsor.routes.CheckAnswersController.onPageLoad()
        }

        "must go to JourneyRecovery Page when Normal Mode when answer is missing" in {
          val userData = UserAnswers("id")
          navigator.nextPage(HaveSponsorPage(), NormalMode, userData) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "UkAddressPage" - {
        "must go to sponsor resident tax Page when sponsor tax resident country codes are not present" in {
          val userData = UserAnswers("id")
          navigator.nextPage(UkAddressPage(), NormalMode, userData) mustBe
            controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)
        }

        "must go to Tax Resident Countries Page when sponsor tax resident country codes are already present" in {
          val userData = UserAnswers("id").withPage(SponsorResidentForTaxPage(0), Country.GB)
          navigator.nextPage(UkAddressPage(), NormalMode, userData) mustBe
            controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(NormalMode)
        }
      }

      "SponsorNamePage" - {
        "must go to WhatIsGIINForSponsor Page" in {
          val userData = UserAnswers("id")
          navigator.nextPage(SponsorNamePage(), NormalMode, userData) mustBe
            controllers.manual.sponsor.routes.WhatIsGIINForSponsorController.onPageLoad(NormalMode)
        }
      }

      "WhatIsGIINForSponsorPage" - {
        "must go to IsSponsorBasedInUK" in {
          val ua = UserAnswers("id")
          navigator.nextPage(WhatIsGIINForSponsorPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.IsSponsorBasedInUKController.onPageLoad(NormalMode)
        }
      }

      "IsSponsorBasedInUKPage" - {
        "must go to Non UK Address page when user selects false" in {
          val ua = UserAnswers("id").withPage(IsSponsorBasedInUKPage(), false)
          navigator.nextPage(IsSponsorBasedInUKPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.AddressNonUkController.onPageLoad(NormalMode)
        }

        "must go to UK Postcode when user selects true" in {
          val ua = UserAnswers("id").withPage(IsSponsorBasedInUKPage(), true)
          navigator.nextPage(IsSponsorBasedInUKPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.UKPostcodeController.onPageLoad(NormalMode)
        }

        "must go to There is a problem page when no answer available" in {
          val ua = UserAnswers("id")
          navigator.nextPage(IsSponsorBasedInUKPage(), NormalMode, ua) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "UKPostcodePage" - {
        val addressLookup: AddressLookup =
          AddressLookup(200000706253L,
                        Some("1 Address line 1 Road"),
                        None,
                        Some("Address line 2 Road"),
                        None,
                        "Town",
                        Some("County"),
                        "zz11zz",
                        Some(Country.GB)
          )

        "must go to IsThisAddressForSponsor when one address is found" in {
          val address: Seq[AddressLookup] = Seq(addressLookup)

          val ua = UserAnswers("id").withPage(UKPostcodePage(), "ZZ1 1ZZ").withPage(AddressLookupPage(), address)
          navigator.nextPage(UKPostcodePage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.IsThisAddressForSponsorController.onPageLoad(NormalMode)
        }
        "must go to WhatIsAddressForSponsor when multiple addresses are found" in {
          val addresses: Seq[AddressLookup] = Seq(addressLookup, addressLookup)

          val ua = UserAnswers("id").withPage(UKPostcodePage(), "ZZ1 1ZZ").withPage(AddressLookupPage(), addresses)
          navigator.nextPage(UKPostcodePage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.WhatIsAddressForSponsorController.onPageLoad(NormalMode)
        }
        "must go to ProblemPage when no addresses are found" in {
          val ua = UserAnswers("id").withPage(UKPostcodePage(), "ZZ1 1ZZ").withPage(AddressLookupPage(), Seq.empty)

          navigator.nextPage(UKPostcodePage(), NormalMode, ua) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
        "must go to JourneyRecovery Page when AddressLookupPage is missing" in {
          val userData = UserAnswers("id")
          navigator.nextPage(HaveSponsorPage(), NormalMode, userData) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "WhatTypeOfFilerPage" - {
        "must go to FilerCategoryCheckAnswers" in {
          val ua = UserAnswers("id")
          navigator.nextPage(WhatTypeOfFilerPage(), NormalMode, ua) mustBe
            controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
        }
      }

      "WhatTypeOfFilerIsSponsorPage" - {
        "must go to FilerCategoryCheckAnswers" in {
          val ua = UserAnswers("id")
          navigator.nextPage(WhatTypeOfFilerIsSponsorPage(), NormalMode, ua) mustBe
            controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
        }
      }

      "HaveNumberPage" - {
        "must go to NumberType Page when answer is Yes" in {
          val userData = UserAnswers("id").withPage(HaveNumberPage(accountId), true)
          navigator.nextPage(HaveNumberPage(accountId), NormalMode, userData) mustBe
            controllers.manual.account.routes.NumberTypeController.onPageLoad(NormalMode)
        }

        "must go to UnderConstruction Page when when answer is No" in {
          val userData = UserAnswers("id").withPage(HaveNumberPage(accountId), false)
          navigator.nextPage(HaveNumberPage(accountId), NormalMode, userData) mustBe
            controllers.manual.account.routes.IdentifierController.onPageLoad(NormalMode)
        }

        "must go to JourneyRecovery Page when Normal Mode" in {
          val userData = UserAnswers("id")
          navigator.nextPage(HaveNumberPage(accountId), NormalMode, userData) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "NumberTypePage" - {
        "must go to UnderConstruction Page when when answer is No" in {
          val userData = UserAnswers("id").withPage(NumberTypePage(accountId), NumberType.Iban)
          navigator.nextPage(NumberTypePage(accountId), NormalMode, userData) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }
      }

      "IdentifierPage" - {
        "must go to AccountClosed Page" in {
          val userData = UserAnswers("id").withPage(IdentifierPage(accountId), "testId")
          navigator.nextPage(IdentifierPage(accountId), NormalMode, userData) mustBe
            controllers.manual.account.routes.AccountClosedController.onPageLoad(NormalMode)
        }

      }

      "AccountClosedPage" - {

        "must go to WhatWasTheAccountCurrency when AccountClosed is true and regime is Crs" in {
          implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")

          val userAnswers = UserAnswers("id")
            .withPage(AccountClosedPage(accountId), true)

          navigator.nextPage(AccountClosedPage(accountId), NormalMode, userAnswers) mustBe
            controllers.manual.account.routes.WhatWasTheAccountCurrencyController.onPageLoad(NormalMode)
        }

        "must go to WhatWasTheAccountBalance page when AccountClosed is true and regime is Fatca" in {
          implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")

          val userAnswers = UserAnswers("id")
            .withPage(AccountClosedPage(accountId), true)
            .withPage(CrsOrFatcaPage, Fatca)

          navigator.nextPage(AccountClosedPage(accountId), NormalMode, userAnswers) mustBe
            controllers.manual.account.routes.WhatWasTheAccountBalanceController.onPageLoad(NormalMode)
        }

        "must go to WhatWasTheAccountBalance page when AccountClosed is false" in {
          val userAnswers = UserAnswers("id")
            .withPage(AccountClosedPage(accountId), false)

          navigator.nextPage(AccountClosedPage(accountId), NormalMode, userAnswers) mustBe
            controllers.manual.account.routes.WhatWasTheAccountBalanceController.onPageLoad(NormalMode)
        }

        "must go to JourneyRecoveryController when AccountClosed is not answered" in {
          val userAnswers = UserAnswers("id")

          navigator.nextPage(AccountClosedPage(accountId), NormalMode, userAnswers) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "AddressNonUkPage" - {
        "must go to Tax resident page after user hits submit when there are no tax tax resident country codes" in {
          val ua = UserAnswers("id")
          navigator.nextPage(AddressNonUkPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)

          val userAnswers = UserAnswers("id")

          navigator.nextPage(AddressNonUkPage(), NormalMode, userAnswers) mustBe
            controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)
        }

        "must go to TaxResidentCountries page after user hits submit when there are tax resident country codes" in {
          val ua = UserAnswers("id")
            .withPage(SponsorResidentForTaxPage(0), Country.GB)

          navigator.nextPage(AddressNonUkPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(NormalMode)
        }
      }

      "WhatWasTheAccountBalancePage" - {
        "must go to UNDERCONSTRUCTION page when regime is FATCA after submission" in {
          implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
          val ua                          = UserAnswers("id")
          navigator.nextPage(WhatWasTheAccountBalancePage(accountId), NormalMode, ua) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }

        "must go to IsUndocumentedAccount page when regime is CRS after submission" in {
          implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")
          val ua                          = UserAnswers("id")
          navigator.nextPage(WhatWasTheAccountBalancePage(accountId), NormalMode, ua) mustBe
            controllers.manual.account.routes.IsUndocumentedAccountController.onPageLoad(NormalMode)
        }
      }

      "WhatIsAddressForSponsorPage" - {
        val address = Address(uprn = None,
                              addressLine1 = "string",
                              addressLine2 = None,
                              addressLine3 = Some("string"),
                              addressLine4 = None,
                              town = "town",
                              postCode = None,
                              country = Country.GB
        )
        "must go to Tax resident page after user hits submit when there are no tax resident country codes" in {
          Seq(true, false).foreach {
            isThisAddressForSponsor =>
              val ua = UserAnswers("id")
                .withPage(IsThisAddressForSponsorPage(), isThisAddressForSponsor)
                .withPage(WhatIsAddressForSponsorPage(), address)
              navigator.nextPage(WhatIsAddressForSponsorPage(), NormalMode, ua) mustBe
                controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)

              val userAnswers = UserAnswers("id")
                .withPage(IsThisAddressForSponsorPage(), isThisAddressForSponsor)
                .withPage(WhatIsAddressForSponsorPage(), address)

              navigator.nextPage(WhatIsAddressForSponsorPage(), NormalMode, userAnswers) mustBe
                controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)
          }

        }

        "must go to TaxResidentCountries page after user hits submit when there are tax resident country codes" in {
          Seq(true, false).foreach {
            isThisAddressForSponsor =>
              val ua = UserAnswers("id")
                .withPage(IsThisAddressForSponsorPage(), isThisAddressForSponsor)
                .withPage(WhatIsAddressForSponsorPage(), address)
                .withPage(TaxResidentCountriesListPage(), Seq(Country.GB))
              navigator.nextPage(WhatIsAddressForSponsorPage(), NormalMode, ua) mustBe
                controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(NormalMode)

          }

        }

      }

      "IsThisAddressForSponsorPage" - {
        val address = Address(uprn = None,
                              addressLine1 = "string",
                              addressLine2 = None,
                              addressLine3 = Some("String"),
                              addressLine4 = None,
                              town = "town",
                              postCode = None,
                              country = Country.GB
        )
        "must go to Tax resident page after user hits submit when there are no tax resident country codes and it is the address" in {

          val ua = UserAnswers("id")
            .withPage(IsThisAddressForSponsorPage(), true)
            .withPage(WhatIsAddressForSponsorPage(), address)
          navigator.nextPage(IsThisAddressForSponsorPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)

          val userAnswers = UserAnswers("id")
            .withPage(IsThisAddressForSponsorPage(), true)
            .withPage(WhatIsAddressForSponsorPage(), address)

          navigator.nextPage(IsThisAddressForSponsorPage(), NormalMode, userAnswers) mustBe
            controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)

        }

        "must go to TaxResidentCountries page after user hits submit when there are tax tax resident country codes" in {
          Seq(true, false).foreach {
            isThisAddressForSponsor =>
              val ua = UserAnswers("id")
                .withPage(IsThisAddressForSponsorPage(), isThisAddressForSponsor)
                .withPage(WhatIsAddressForSponsorPage(), address)
                .withPage(TaxResidentCountriesListPage(), Seq(Country.GB))
              navigator.nextPage(WhatIsAddressForSponsorPage(), NormalMode, ua) mustBe
                controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(NormalMode)

          }

        }

        "must go to UkAddress Page when answer is No" in {
          val address = Address(
            uprn = None,
            addressLine1 = "1 Address line 1 Road",
            addressLine2 = None,
            addressLine3 = Some("Address line 2 Road"),
            addressLine4 = None,
            town = "Town",
            postCode = Some("zz11zz"),
            country = Country.GB
          )
          val userData = UserAnswers("id")
            .withPage(IsThisAddressForSponsorPage(), false)
            .withPage(WhatIsAddressForSponsorPage(), address)
          navigator.nextPage(IsThisAddressForSponsorPage(), NormalMode, userData) mustBe
            controllers.manual.sponsor.routes.UkAddressController.onPageLoad(NormalMode)
        }

        "must go to JourneyRecovery Page when IsThisAddressForSponsorPage not present" in {
          val userData = UserAnswers("id")
          navigator.nextPage(IsThisAddressForSponsorPage(), NormalMode, userData) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }

      }
      "SponsorResidentForTaxPage" - {
        "must go to Tax Resident Country page" in {
          val ua = UserAnswers("id")
            .withPage(SponsorResidentForTaxPage(0), Country.GB)

          navigator.nextPage(SponsorResidentForTaxPage(0), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(NormalMode)
        }
      }

      "TaxResidentCountriesPage" - {
        "must go to SponsorResidentForTax page when user selected as yes" in {
          val ua = UserAnswers("id")
            .withPage(DoYouWantToAddTaxResidentCountryPage(), true)

          navigator.nextPage(DoYouWantToAddTaxResidentCountryPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)
        }

        "must go to CheckAnswers page when user selected as no" in {
          val ua = UserAnswers("id")
            .withPage(DoYouWantToAddTaxResidentCountryPage(), false)

          navigator.nextPage(DoYouWantToAddTaxResidentCountryPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.CheckAnswersController.onPageLoad()
        }
      }
      "WhatWasTheAccountCurrencyPage" - {
        "must go to IsUndocumentedAccount page after submission" in {
          implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")
          val ua                          = UserAnswers("id")
          navigator.nextPage(WhatWasTheAccountCurrencyPage(accountId), NormalMode, ua) mustBe
            controllers.manual.account.routes.IsUndocumentedAccountController.onPageLoad(NormalMode)
        }
        "IsUndocumentedAccountPage" - {
          "must go to IsDormantAccount page after submission" in {
            val ua = UserAnswers("id")
            navigator.nextPage(IsUndocumentedAccountPage(accountId), NormalMode, ua) mustBe
              controllers.manual.account.routes.IsDormantAccountController.onPageLoad(NormalMode)
          }
        }
        "IsDormantAccountPage" - {
          "must go to under WasYearOpen page after successful submission" in {
            val ua = UserAnswers("id")
            navigator.nextPage(IsDormantAccountPage(accountId), NormalMode, ua) mustBe
              controllers.manual.account.routes.WasAccountOpenController.onPageLoad(NormalMode)
          }
        }
      }
      "WasAccountOpenPage" - {
        "must go to IsJointAccountPage after a valid submission" in {
          val ua = emptyUserAnswers.withPage(WasAccountOpenPage(accountId), WasAccountOpen.Yes)
          navigator.nextPage(WasAccountOpenPage(accountId), NormalMode, ua) mustBe controllers.manual.account.routes.IsJointAccountController
            .onPageLoad(NormalMode)
        }
      }
      "IsJointAccountPage" - {
        "must go to HowManyJointAccountHolderPage if answered yes" in {
          val ua = emptyUserAnswers.withPage(IsJointAccountPage(accountId), true)
          navigator.nextPage(IsJointAccountPage(accountId), NormalMode, ua) mustBe controllers.manual.account.routes.HowManyJointAccountHoldersController
            .onPageLoad(NormalMode)
        }
        "must go to Under construction page if answered no" in {
          val ua = emptyUserAnswers.withPage(IsJointAccountPage(accountId), false)
          navigator.nextPage(IsJointAccountPage(accountId), NormalMode, ua) mustBe controllers.routes.UnderConstructionController.onPageLoad()
        }
      }
      "RemoveTaxResidentCountryPage" - {
        "must go to Tax Resident countries page when submitted" in {
          val ua = UserAnswers("id")
          navigator.nextPage(RemoveTaxResidentCountryPage(), NormalMode, ua) mustBe
            controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(NormalMode)
        }
      }

      "cpso" - {
        val currentCPSOId = CPSOId("testid")
        "IndividualOrOrganisationPage" - {
          "must go to cpo IndividualName page when submitted" in {
            val ua = UserAnswers("id")
              .withPage(pages.manual.cpso.IndividualOrOrganisationPage(currentCPSOId), models.manual.cpso.IndividualOrOrganisation.Individual)
            navigator.nextPage(pages.manual.cpso.IndividualOrOrganisationPage(currentCPSOId), NormalMode, ua) mustBe
              controllers.manual.cpso.routes.IndividualNameController.onPageLoad(NormalMode)
          }
        }

        "IndividualNamePage" - {
          "must go to underconstruction page when submitted" in {
            val ua = UserAnswers("id")
              .withPage(pages.manual.cpso.IndividualNamePage(currentCPSOId), models.manual.cpso.IndividualName("first-name", "last-name"))
            navigator.nextPage(pages.manual.cpso.IndividualNamePage(currentCPSOId), NormalMode, ua) mustBe
              controllers.routes.UnderConstructionController.onPageLoad()
          }
        }
      }

      "accountHolderPages" - {
        val currentAccountHolderId = AccountHolderId("01")
        "IndividualOrOrganisationPage" - {
          "must go to UnderConstruction page when organisation is selected" in {
            val ua = UserAnswers("id")
              .withPage(IndividualOrOrganisationPage(currentAccountHolderId)(reportId), Organisation)
            navigator.nextPage(IndividualOrOrganisationPage(currentAccountHolderId), NormalMode, ua) mustBe
              controllers.routes.UnderConstructionController.onPageLoad()
          }

          "must go to IndividualName page when individual is selected" in {
            val ua = UserAnswers("id")
              .withPage(IndividualOrOrganisationPage(currentAccountHolderId)(reportId), Individual)
            navigator.nextPage(IndividualOrOrganisationPage(currentAccountHolderId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.IndividualNameController.onPageLoad(NormalMode)
          }

          "must go to Journey Recovery if IndividualOrOrganisationPage is absent" in {
            val ua = UserAnswers("id")
            navigator.nextPage(IndividualOrOrganisationPage(currentAccountHolderId), NormalMode, ua) mustBe
              routes.JourneyRecoveryController.onPageLoad()
          }

          "must go to JourneyRecovery page when No value is selected" in {
            val ua = UserAnswers("id")
            navigator.nextPage(IndividualOrOrganisationPage(currentAccountHolderId), NormalMode, ua) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }
        "IndividualName" - {
          "must go to UnderConstruction page" in {
            val ua = UserAnswers("id")
              .withPage(IndividualNamePage(currentAccountHolderId)(reportId), IndividualName("firstName", "lastName"))
            navigator.nextPage(IndividualNamePage(currentAccountHolderId), NormalMode, ua) mustBe
              controllers.routes.UnderConstructionController.onPageLoad()
          }
        }
      }
    }
  }
}
