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
import models.*
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.TaxResidentInformation
import models.manual.account.WhatAccountType
import models.manual.accountHolders.*
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.response.{Address, AddressLookup, Country}
import models.viewModels.{AccountHolderId, AccountId}
import pages.*
import pages.manual.account.WhatAccountTypePage
import pages.manual.accountHolders.*

import java.time.LocalDate

class AccountHolderNavigatorSpec extends SpecBase {

  val navigator = new ManualSubmissionNavigator

  val nonUkAddress = AddressNonUk(
    addressLine1 = "string",
    addressLine2 = None,
    addressLine3 = "string",
    addressLine4 = None,
    postcode = None,
    country = "Sweden"
  )

  "ManualSubmissionNavigator in NormalMode" - {
    "with reportId" - {
      implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
      "accountHolderPages" - {

        val currentAccountHolderId = AccountHolderId("01")

        "AccountPage" - {

          "must go to Individual Or Organisation Page " in {
            val ua = UserAnswers("id")
              .withPage(
                AccountPage(reportId, currentAccountHolderId),
                Set(AccountId("testId"))
              )

            navigator.nextPage(
              AccountPage(reportId, currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.IndividualOrOrganisationController.onPageLoad(NormalMode)
          }

          "must go to Problem Page " in {
            val ua = UserAnswers("id")

            navigator.nextPage(
              AccountPage(reportId, currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "ResidentTaxPage" - {
          implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")

          "must go to have tax identification page when regime is crs" in {

            val ua = UserAnswers("id")

            navigator.nextPage(
              ResidentTaxPage(0, currentAccountHolderId, reportId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.HaveTaxIdenticationNumberController.onPageLoad(NormalMode)
          }

          "must go to tax identification page page when regime is fatca" in {
            implicit val reportId2: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
            val ua                           = UserAnswers("id")

            navigator.nextPage(
              ResidentTaxPage(0, currentAccountHolderId, reportId2),
              NormalMode,
              ua
            )(reportId2) mustBe
              controllers.manual.accountHolders.routes.TaxIdentificationNumberController.onPageLoad(NormalMode)
          }
        }

        "HaveTaxIdenticationNumberPage" - {
          implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")

          "must go to tax identification page if they have a tax identification number" in {

            val ua = UserAnswers("id")
              .withPage(HaveTaxIdenticationNumberPage(0, currentAccountHolderId, reportId), true)

            navigator.nextPage(
              HaveTaxIdenticationNumberPage(0, currentAccountHolderId, reportId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.TaxIdentificationNumberController.onPageLoad(NormalMode)
          }

          "must go to TaxResidentCountries page if they do not have a tax identification number" in {

            val ua = UserAnswers("id")
              .withPage(HaveTaxIdenticationNumberPage(0, currentAccountHolderId, reportId), false)

            navigator.nextPage(
              HaveTaxIdenticationNumberPage(0, currentAccountHolderId, reportId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.TaxResidentCountriesController.onPageLoad(NormalMode)
          }

          "must go to journey recovery page if they do not have a tax identification number record" in {

            val ua = UserAnswers("id")

            navigator.nextPage(
              HaveTaxIdenticationNumberPage(0, currentAccountHolderId, reportId),
              NormalMode,
              ua
            ) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "TaxIdentificationNumberPage" - {
          val ua = UserAnswers("id")
            .withPage(TaxIdentificationNumberPage(0, currentAccountHolderId, reportId), "some-value")

          "must go to under construction page if successfully submitted" in {
            navigator.nextPage(
              TaxIdentificationNumberPage(0, currentAccountHolderId, reportId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.TaxResidentCountriesController.onPageLoad(NormalMode)
          }

        }

        "IndividualOrOrganisationPage" - {

          "must go to organisation name page when organisation is selected" in {
            val ua = UserAnswers("id")
              .withPage(
                IndividualOrOrganisationPage(currentAccountHolderId)(reportId),
                Organisation
              )

            navigator.nextPage(
              IndividualOrOrganisationPage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.OrganisationNameController.onPageLoad(NormalMode)
          }

          "must go to AccountHolder IndividualName page when individual is selected" in {
            val ua = UserAnswers("id")
              .withPage(
                IndividualOrOrganisationPage(currentAccountHolderId)(reportId),
                Individual
              )

            navigator.nextPage(
              IndividualOrOrganisationPage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.IndividualNameController
                .onPageLoad(NormalMode)
          }

          "must go to Journey Recovery when the answer is missing" in {
            val ua = UserAnswers("id")

            navigator.nextPage(
              IndividualOrOrganisationPage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "IndividualNamePage" - {

          "must go to IndividualHaveDateOfBirth page when the individual name exists" in {
            val ua = UserAnswers("id")
              .withPage(
                AccountHolderIndividualNamePage(currentAccountHolderId)(reportId),
                IndividualName("firstName", "lastName")
              )

            navigator.nextPage(
              AccountHolderIndividualNamePage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.IndividualHaveDateOfBirthController
                .onPageLoad(NormalMode)
          }

          "must go to Journey Recovery when the individual name is missing" in {
            val ua = UserAnswers("id")

            navigator.nextPage(
              AccountHolderIndividualNamePage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "AccountHolderOrganisationNamePage" - {

          "must go to OrganisationCrs page when the organisation name exists and regime is CRS" in {
            implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestFIID")

            val ua = UserAnswers("id")
              .withPage(
                AccountHolderOrganisationNamePage(currentAccountHolderId)(reportId),
                "Test Organisation"
              )

            navigator.nextPage(
              AccountHolderOrganisationNamePage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.OrganisationCrsController
                .onPageLoad(NormalMode)
          }

          "must go to OrganisationFatca page when the organisation name exists and regime is FATCA" in {
            implicit val reportId: ReportId = ReportId(FATCA, 2025, None, "TestFIID")

            val ua = UserAnswers("id")
              .withPage(
                AccountHolderOrganisationNamePage(currentAccountHolderId)(reportId),
                "Test Organisation"
              )

            navigator.nextPage(
              AccountHolderOrganisationNamePage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.OrganisationFatcaController
                .onPageLoad(NormalMode)
          }

          "must go to Journey Recovery when the organisation name is missing" in {
            implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestFIID")

            val ua = UserAnswers("id")

            navigator.nextPage(
              AccountHolderOrganisationNamePage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "IndividualHaveDateOfBirthPage" - {

          "must go to IndividualDateOfBirth page when the answer is yes" in {
            val ua = UserAnswers("id")
              .withPage(
                IndividualHaveDateOfBirthPage(currentAccountHolderId)(reportId),
                true
              )

            navigator.nextPage(
              IndividualHaveDateOfBirthPage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.IndividualDateOfBirthController
                .onPageLoad(NormalMode)
          }

          "must go to UnderConstruction page when the answer is no" in {
            val ua = UserAnswers("id")
              .withPage(
                IndividualHaveDateOfBirthPage(currentAccountHolderId)(reportId),
                false
              )

            navigator.nextPage(
              IndividualHaveDateOfBirthPage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.IndividualHavePlaceOfBirthController.onPageLoad(NormalMode)
          }

          "must go to Journey Recovery when the answer is missing" in {
            val ua = UserAnswers("id")

            navigator.nextPage(
              IndividualHaveDateOfBirthPage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "IndividualDateOfBirthPage" - {

          "must go to under construction page after entering DOB" in {
            val ua = UserAnswers("id")
              .withPage(
                IndividualDateOfBirthPage(currentAccountHolderId)(reportId),
                IndividualDateOfBirth(LocalDate.of(1996, 3, 8))
              )

            navigator.nextPage(
              IndividualDateOfBirthPage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.manual.accountHolders.routes.IndividualHavePlaceOfBirthController.onPageLoad(NormalMode)
          }

          "must go to Journey Recovery when the answer is missing" in {
            val ua = UserAnswers("id")

            navigator.nextPage(
              IndividualDateOfBirthPage(currentAccountHolderId),
              NormalMode,
              ua
            ) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "IndividualHavePlaceOfBirthPage" - {
          implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
          val accountId                   = AccountHolderId("TestAccountId")
          "must go to Individual place of birth page when user selected yes" in {
            val ua = UserAnswers("id")
              .withPage(IndividualHavePlaceOfBirthPage(accountId), true)

            navigator.nextPage(IndividualHavePlaceOfBirthPage(accountId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.IndividualPlaceOfBirthController.onPageLoad(NormalMode)
          }

          "must go to under construction page when user selected no" in {
            val ua = UserAnswers("id")
              .withPage(IndividualHavePlaceOfBirthPage(accountId), false)

            navigator.nextPage(IndividualHavePlaceOfBirthPage(accountId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.WhereAreTheyBasedController.onPageLoad(NormalMode)
          }
        }

        "IndividualPlaceOfBirthPage" - {
          implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
          val accountId                   = AccountHolderId("TestAccountId")
          val details                     = IndividualPlaceOfBirth(Some("value 1"), Some("value 2"), "FX")
          "must go to Where Are They Based Page page when user submits details" in {
            val ua = UserAnswers("id")
              .withPage(IndividualPlaceOfBirthPage(accountId, reportId), details)

            navigator.nextPage(IndividualPlaceOfBirthPage(accountId, reportId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.WhereAreTheyBasedController.onPageLoad(NormalMode)
          }
        }

        "WhereAreTheyBasedPage" - {
          implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
          val accountId                   = AccountHolderId("TestAccountId")
          "must go to under construction page when user selected yes" in {
            val ua = UserAnswers("id")
              .withPage(WhereAreTheyBasedPage(accountId), true)

            navigator.nextPage(WhereAreTheyBasedPage(accountId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.UkPostCodeForAccountHolderController.onPageLoad(NormalMode)
          }

          "must go to AccountHolderNonUkAddress page when user selected no" in {
            val ua = UserAnswers("id")
              .withPage(WhereAreTheyBasedPage(accountId), false)
            navigator.nextPage(WhereAreTheyBasedPage(accountId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.AccountHolderAddressNonUkController.onPageLoad(NormalMode)
          }

          "must go to journey recovery if WhereAreTheyBasedPage is not present" in {
            val ua = UserAnswers("id")

            navigator.nextPage(WhereAreTheyBasedPage(accountId), NormalMode, ua) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "IsThisTheAddressForAccountHoldersPage" - {
          val country                     = Country("GB", "UK")
          val taxResidentInformation      = TaxResidentInformation(country)
          implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
          val accountHolderId             = AccountHolderId("holder-id")

          "must go to resident tax page for a yes answer when regime is FATCA when there were not tax resident counttries" in {
            val ua = UserAnswers("id")
              .withPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), true)

            navigator.nextPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode)
          }

          "must go to resident tax page for a yes answer when regime is FATCA when tax resident counttries is empty" in {
            val ua = UserAnswers("id")
              .withPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), true)
              .withPage(pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, reportId), Seq())

            navigator.nextPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode)
          }

          "must go to under construction page for a yes answer when regime is FATCA when tax resident countries are present" in {
            val ua = UserAnswers("id")
              .withPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), true)
              .withPage(pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, reportId), Seq(taxResidentInformation))

            navigator.nextPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.routes.UnderConstructionController.onPageLoad()
          }

          "must go to self certification page for a yes answer when regime is CRS" in {
            val crsReportId     = ReportId(CRS, 2025, None, "TestFIID")
            val accountHolderId = AccountHolderId("holder-id")
            val accountId       = AccountId("account-1")

            val ua = UserAnswers("id")
              .withPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, crsReportId), true)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId))
              .withPage(WhatAccountTypePage(accountId)(crsReportId), WhatAccountType.Custodial)

            navigator.nextPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode)
          }
          "must go to address uk for a no answer" in {
            val ua = UserAnswers("id")
              .withPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), false)

            navigator.nextPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.UkAddressController.onPageLoad(NormalMode)
          }

          "must go to journey recovery when IsThisTheAddressForAccountHoldersPage is not present" in {
            val ua = UserAnswers("id")

            navigator.nextPage(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "SelectAddressPage" - {

          val crsReportId     = ReportId(CRS, 2025, None, "TestFIID")
          val fatcaReportId   = ReportId(FATCA, 2025, None, "TestFIID")
          val accountHolderId = AccountHolderId("holder-id")
          val accountId1      = AccountId("account-1")
          val accountId2      = AccountId("account-2")

          val address = Address(
            uprn = None,
            addressLine1 = "string",
            addressLine2 = None,
            addressLine3 = Some("string"),
            addressLine4 = None,
            town = "town",
            postCode = None,
            country = Country.GB
          )

          "must go to Resident Tax for FATCA when no tax resident countries are present" in {
            val ua = UserAnswers("id")
              .withPage(SelectAddressPage(accountHolderId, fatcaReportId), address)

            navigator.nextPage(SelectAddressPage(accountHolderId, fatcaReportId), NormalMode, ua)(fatcaReportId) mustBe
              controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode)
          }
          "must go to under construction for FATCA when tax resident countries are present" in {
            val ua = UserAnswers("id")
              .withPage(SelectAddressPage(accountHolderId, fatcaReportId), address)
              .withPage(
                pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, fatcaReportId),
                Seq(TaxResidentInformation(Country.GB))
              )

            navigator.nextPage(SelectAddressPage(accountHolderId, fatcaReportId), NormalMode, ua)(fatcaReportId) mustBe
              controllers.routes.UnderConstructionController.onPageLoad()
          }

          "must go to Provide Equity Interest Type for CRS with a CRS1104 account" in {
            val ua = UserAnswers("id")
              .withPage(SelectAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(SelectAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.ProvideEquityInterestTypeController.onPageLoad(NormalMode)
          }

          "must go to Provide Equity Interest Type when one of multiple accounts is CRS1104" in {
            val ua = UserAnswers("id")
              .withPage(SelectAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1, accountId2))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.Custodial)
              .withPage(WhatAccountTypePage(accountId2)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(SelectAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.ProvideEquityInterestTypeController.onPageLoad(NormalMode)
          }

          "must go to Self Certification for CRS without a CRS1104 account" in {
            val ua = UserAnswers("id")
              .withPage(SelectAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1, accountId2))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.Depository)
              .withPage(WhatAccountTypePage(accountId2)(crsReportId), WhatAccountType.Custodial)

            navigator.nextPage(SelectAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode)
          }

          "must only check accounts associated with the current account holder" in {
            val ua = UserAnswers("id")
              .withPage(SelectAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.Custodial)
              .withPage(WhatAccountTypePage(accountId2)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(SelectAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode)
          }

          "must go to Journey Recovery when associated accounts are missing" in {
            val ua = UserAnswers("id")
              .withPage(SelectAddressPage(accountHolderId, crsReportId), address)

            navigator.nextPage(SelectAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }

          "must go to Journey Recovery when associated accounts are empty" in {
            val ua = UserAnswers("id")
              .withPage(SelectAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set.empty[AccountId])

            navigator.nextPage(SelectAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }

          "must go to Journey Recovery when an associated account type is missing" in {
            val ua = UserAnswers("id")
              .withPage(SelectAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1, accountId2))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(SelectAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "UKAddressPage" - {

          val crsReportId     = ReportId(CRS, 2025, None, "TestFIID")
          val fatcaReportId   = ReportId(FATCA, 2025, None, "TestFIID")
          val accountHolderId = AccountHolderId("holder-id")
          val accountId1      = AccountId("account-1")
          val accountId2      = AccountId("account-2")

          val address = Address(
            uprn = None,
            addressLine1 = "string",
            addressLine2 = None,
            addressLine3 = Some("string"),
            addressLine4 = None,
            town = "town",
            postCode = None,
            country = Country.GB
          ).ukAddress

          "must go to Resident Tax for FATCA when no tax resident countries are present" in {
            val ua = UserAnswers("id")
              .withPage(UkAddressPage(accountHolderId, fatcaReportId), address)

            navigator.nextPage(UkAddressPage(accountHolderId, fatcaReportId), NormalMode, ua)(fatcaReportId) mustBe
              controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode)
          }

          "must go to Provide Equity Interest Type for CRS with a CRS1104 account" in {
            val ua = UserAnswers("id")
              .withPage(UkAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(UkAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.ProvideEquityInterestTypeController.onPageLoad(NormalMode)
          }

          "must go to Provide Equity Interest Type when one of multiple accounts is CRS1104" in {
            val ua = UserAnswers("id")
              .withPage(UkAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1, accountId2))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.Custodial)
              .withPage(WhatAccountTypePage(accountId2)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(UkAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.ProvideEquityInterestTypeController.onPageLoad(NormalMode)
          }

          "must go to Self Certification for CRS without a CRS1104 account" in {
            val ua = UserAnswers("id")
              .withPage(UkAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1, accountId2))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.Depository)
              .withPage(WhatAccountTypePage(accountId2)(crsReportId), WhatAccountType.Custodial)

            navigator.nextPage(UkAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode)
          }

          "must only check accounts associated with the current account holder" in {
            val ua = UserAnswers("id")
              .withPage(UkAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.Custodial)
              .withPage(WhatAccountTypePage(accountId2)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(UkAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode)
          }

          "must go to Journey Recovery when associated accounts are missing" in {
            val ua = UserAnswers("id")
              .withPage(UkAddressPage(accountHolderId, crsReportId), address)

            navigator.nextPage(UkAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }

          "must go to Journey Recovery when associated accounts are empty" in {
            val ua = UserAnswers("id")
              .withPage(UkAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set.empty[AccountId])

            navigator.nextPage(UkAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }

          "must go to Journey Recovery when an associated account type is missing" in {
            val ua = UserAnswers("id")
              .withPage(UkAddressPage(accountHolderId, crsReportId), address)
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1, accountId2))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(UkAddressPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "Account holder SelfCertificationPage" - {
          val country                = Country("GB", "UK")
          val taxResidentInformation = TaxResidentInformation(country)
          val accountHolderId        = AccountHolderId("holder-id")
          "must go to resident tax country when no countries are not present" in {
            val ua = UserAnswers("id")
              .withPage(SelfCertificationPage(accountHolderId, reportId), SelfCertification.allValidValues.head)

            navigator.nextPage(SelfCertificationPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode)
          }

          "must go to resident tax country when  countries are empty" in {
            val ua = UserAnswers("id")
              .withPage(SelfCertificationPage(accountHolderId, reportId), SelfCertification.allValidValues.head)
              .withPage(pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, reportId), Seq.empty)

            navigator.nextPage(SelfCertificationPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode)
          }

          "must go to under construction when countries are present" in {
            val ua = UserAnswers("id")
              .withPage(SelfCertificationPage(accountHolderId, reportId), SelfCertification.allValidValues.head)
              .withPage(pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, reportId), Seq(taxResidentInformation))

            navigator.nextPage(SelfCertificationPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.routes.UnderConstructionController.onPageLoad()
          }

        }

        "AccountHolderAddressNonUk Page" - {

          val accountHolderId = AccountHolderId("holder-id")
          val crsReportId     = ReportId(CRS, 2025, None, "TestFIID")
          val fatcaReportId   = ReportId(FATCA, 2025, None, "TestFIID")
          val accountId1      = AccountId("account-1")
          val accountId2      = AccountId("account-2")

          "must go to under construction for FATCA" in {
            val ua = UserAnswers("id")
              .withPage(AccountHolderAddressNonUkPage(accountHolderId, fatcaReportId), nonUkAddress)
              .withPage(
                pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, fatcaReportId),
                Seq(TaxResidentInformation(Country.GB))
              )

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, fatcaReportId), NormalMode, ua)(fatcaReportId) mustBe
              controllers.routes.UnderConstructionController.onPageLoad()
          }

          "must go to Resident Tax for FATCA when no tax resident countries are present" in {
            val ua = UserAnswers("id")
              .withPage(AccountHolderAddressNonUkPage(accountHolderId, fatcaReportId), nonUkAddress)

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, fatcaReportId), NormalMode, ua)(fatcaReportId) mustBe
              controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode)
          }

          "must go to Resident Tax for FATCA when tax resident countries are empty" in {
            val ua = UserAnswers("id")
              .withPage(AccountHolderAddressNonUkPage(accountHolderId, fatcaReportId), nonUkAddress)
              .withPage(
                pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, fatcaReportId),
                Seq.empty[TaxResidentInformation]
              )

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, fatcaReportId), NormalMode, ua)(fatcaReportId) mustBe
              controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode)
          }

          "must go to Provide Equity Interest Type for CRS with a CRS1104 account" in {
            val ua = UserAnswers("id")
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.ProvideEquityInterestTypeController.onPageLoad(NormalMode)
          }

          "must go to Provide Equity Interest Type when one of multiple accounts is CRS1104" in {
            val ua = UserAnswers("id")
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1, accountId2))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.Custodial)
              .withPage(WhatAccountTypePage(accountId2)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.ProvideEquityInterestTypeController.onPageLoad(NormalMode)
          }

          "must go to Self Certification for CRS without a CRS1104 account" in {
            val ua = UserAnswers("id")
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1, accountId2))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.Depository)
              .withPage(WhatAccountTypePage(accountId2)(crsReportId), WhatAccountType.Custodial)

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode)
          }

          "must only check accounts associated with the current account holder" in {
            val ua = UserAnswers("id")
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.Custodial)
              .withPage(WhatAccountTypePage(accountId2)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode)
          }

          "must go to Journey Recovery when associated accounts are missing" in {
            val ua = UserAnswers("id")

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }

          "must go to Journey Recovery when associated accounts are empty" in {
            val ua = UserAnswers("id")
              .withPage(AccountPage(crsReportId, accountHolderId), Set.empty[AccountId])

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }

          "must go to Journey Recovery when an associated account type is missing" in {
            val ua = UserAnswers("id")
              .withPage(AccountPage(crsReportId, accountHolderId), Set(accountId1, accountId2))
              .withPage(WhatAccountTypePage(accountId1)(crsReportId), WhatAccountType.InvestmentEntity)

            navigator.nextPage(AccountHolderAddressNonUkPage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "UKPostcodePageForAccountHolder" - {
          implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
          val accountHolderId             = AccountHolderId("holder-id")
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

            val ua = UserAnswers("id")
              .withPage(UkPostCodeForAccountHolderPage(accountHolderId, reportId), "ZZ1 1ZZ")
              .withPage(AddressLookupForAccountHolderPage(accountHolderId, reportId), address)
            navigator.nextPage(UkPostCodeForAccountHolderPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.IsThisTheAddressForAccountHoldersController.onPageLoad(NormalMode)
          }
          "must go to select address when multiple addresses are found" in {
            val addresses: Seq[AddressLookup] = Seq(addressLookup, addressLookup)

            val ua = UserAnswers("id")
              .withPage(UkPostCodeForAccountHolderPage(accountHolderId, reportId), "ZZ1 1ZZ")
              .withPage(AddressLookupForAccountHolderPage(accountHolderId, reportId), addresses)
            navigator.nextPage(UkPostCodeForAccountHolderPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.SelectAddressController.onPageLoad(NormalMode)
          }
          "must go to ProblemPage when no addresses are found" in {
            val ua = UserAnswers("id")
              .withPage(UkPostCodeForAccountHolderPage(accountHolderId, reportId), "ZZ1 1ZZ")
              .withPage(AddressLookupForAccountHolderPage(accountHolderId, reportId), Seq.empty)

            navigator.nextPage(UkPostCodeForAccountHolderPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
          "must go to JourneyRecovery Page when AddressLookupPage is missing" in {
            val userData = UserAnswers("id")
            navigator.nextPage(UkPostCodeForAccountHolderPage(accountHolderId, reportId), NormalMode, userData) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "DoYouWantToAddTaxResidentCountryPage" - {
          implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
          val accountHolderId             = AccountHolderId("holder-id")

          "must go to TaxResidentCountries page when answer is true" in {

            val ua = UserAnswers("id")
              .withPage(DoYouWantToAddTaxResidentCountryPage(accountHolderId, reportId), true)
            navigator.nextPage(DoYouWantToAddTaxResidentCountryPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode)
          }
          "must go to TaxResidentCountries page when answer is false" in {

            val ua = UserAnswers("id")
              .withPage(DoYouWantToAddTaxResidentCountryPage(accountHolderId, reportId), false)
            navigator.nextPage(DoYouWantToAddTaxResidentCountryPage(accountHolderId, reportId), NormalMode, ua) mustBe
              controllers.routes.UnderConstructionController.onPageLoad()
          }

          "must go to JourneyRecovery Page when AddressLookupPage is missing" in {
            val userData = UserAnswers("id")
            navigator.nextPage(DoYouWantToAddTaxResidentCountryPage(accountHolderId, reportId), NormalMode, userData) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }
        }

        "RemoveTaxResidentPage" - {
          implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
          val accountHolderId             = AccountHolderId("holder-id")

          "must go to TaxResidentCountries page when answer is true" in {

            val ua = UserAnswers("id")
              .withPage(RemoveTaxResidentCountryPage(reportId, accountHolderId), true)
            navigator.nextPage(RemoveTaxResidentCountryPage(reportId, accountHolderId), NormalMode, ua) mustBe
              controllers.manual.accountHolders.routes.TaxResidentCountriesController.onPageLoad(NormalMode)
          }

        }

        "Equity interest routing" - {

          val accountHolderId = AccountHolderId("equity-holder")
          val crsReportId     = ReportId(CRS, 2025, None, "TestFIID")

          "must go to Equity Interest Type when Provide Equity Interest Type is Yes" in {
            val ua = UserAnswers("id")
              .withPage(ProvideEquityInterestTypePage(accountHolderId, crsReportId), true)

            navigator.nextPage(ProvideEquityInterestTypePage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.EquityInterestTypeController.onPageLoad(NormalMode)
          }

          "must go to Self Certification when Provide Equity Interest Type is No" in {
            val ua = UserAnswers("id")
              .withPage(ProvideEquityInterestTypePage(accountHolderId, crsReportId), false)

            navigator.nextPage(ProvideEquityInterestTypePage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode)
          }

          "must go to Journey Recovery when Provide Equity Interest Type is unanswered" in {
            val ua = UserAnswers("id")

            navigator.nextPage(ProvideEquityInterestTypePage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.routes.JourneyRecoveryController.onPageLoad()
          }

          "must go to Self Certification after Equity Interest Type" in {
            val ua = UserAnswers("id")

            navigator.nextPage(EquityInterestTypePage(accountHolderId, crsReportId), NormalMode, ua)(crsReportId) mustBe
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode)
          }
        }

      }
    }
  }
}
