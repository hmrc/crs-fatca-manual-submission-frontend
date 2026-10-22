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
import models.manual.account.PaymentType
import models.manual.account.PaymentType.*
import models.manual.accountHolders.*
import models.manual.cpso.CpsoSelfCertification.Yes
import models.manual.cpso.IndividualOrOrganisation
import models.response.{Address, AddressLookup, Country}
import models.viewModels.manual.cpso.CPSOId
import pages.*
import pages.manual.cpso.{CPSOAddressNonUkPage, CPSOWhereAreTheyBasedPage, CpsoOrganisationNamePage, CpsoSelfCertificationPage, HaveTaxIdentificationNumberPage, ResidentForTaxPage, AddressLookupPage as AddressLookupPageForCpso, SelectAddressPage as SelectAddressPageForCPSO, UkPostCodePage as UkPostCodePageForCpso}
import pages.manual.sponsor.*

import java.time.LocalDate

class CPSONavigationSpec extends SpecBase {

  val nonUkAddress = AddressNonUk(
    addressLine1 = "string",
    addressLine2 = None,
    addressLine3 = "string",
    addressLine4 = None,
    postcode = None,
    country = "Sweden"
  )

  val navigator = new ManualSubmissionNavigator
  
  "cpso" - {
    val currentCPSOId = CPSOId("testid")
    
    "IndividualOrOrganisationPage" - {
      "must go to cpo IndividualName page when submitted" in {
        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.IndividualOrOrganisationPage(currentCPSOId), models.manual.cpso.IndividualOrOrganisation.Individual)
        navigator.nextPage(pages.manual.cpso.IndividualOrOrganisationPage(currentCPSOId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.IndividualNameController.onPageLoad(NormalMode)
      }

      "must go to cpo Individual organisation name page when submitted" in {
        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.IndividualOrOrganisationPage(currentCPSOId), models.manual.cpso.IndividualOrOrganisation.Organisation)
        navigator.nextPage(pages.manual.cpso.IndividualOrOrganisationPage(currentCPSOId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.CpsoOrganisationNameController.onPageLoad(NormalMode)
      }
    }

    "IndividualNamePage" - {
      "must go to individual have date of birth page when submitted" in {
        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.IndividualNamePage(currentCPSOId), models.manual.cpso.IndividualName("first-name", "last-name"))
        navigator.nextPage(pages.manual.cpso.IndividualNamePage(currentCPSOId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.IndividualHaveDateOfBirthController.onPageLoad(NormalMode)
      }
    }

    "IndividualDateOfBirthPage" - {
      "must go to IndividualHavePlaceOfBirth page when submitted" in {
        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.CPSOIndividualDateOfBirthPage(currentCPSOId, reportId), IndividualDateOfBirth(LocalDate.of(1980, 3, 31)))
        navigator.nextPage(pages.manual.cpso.CPSOIndividualDateOfBirthPage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.IndividualHavePlaceOfBirthController.onPageLoad(NormalMode)
      }
    }

    "IndividualHavePlaceOfBirthPage" - {
      "must go to IndividualPlaceOfBirth page when answer is yes" in {
        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.IndividualHavePlaceOfBirthPage(currentCPSOId, reportId), true)
        navigator.nextPage(pages.manual.cpso.IndividualHavePlaceOfBirthPage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.IndividualPlaceOfBirthController.onPageLoad(NormalMode)
      }
      "when regime is CRS" - {
        implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")

        "must go to ControllingPersonType page when answer is no" in {
          val ua = UserAnswers("id")
            .withPage(pages.manual.cpso.IndividualHavePlaceOfBirthPage(currentCPSOId, reportId), false)

          navigator.nextPage(pages.manual.cpso.IndividualHavePlaceOfBirthPage(currentCPSOId, reportId), NormalMode, ua) mustBe
            controllers.manual.cpso.routes.ControllingPersonTypeController.onPageLoad(NormalMode)
        }
      }
      "when regime is FATCA" - {
        implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")

        "must go to CPSOWhereAreTheyBased page when answer is no" in {
          val ua = UserAnswers("id")
            .withPage(pages.manual.cpso.IndividualHavePlaceOfBirthPage(currentCPSOId, reportId), false)
          navigator.nextPage(pages.manual.cpso.IndividualHavePlaceOfBirthPage(currentCPSOId, reportId), NormalMode, ua) mustBe
            controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(NormalMode)
        }

      }

      "must go to JourneyRecovery page when page doesnt have value" in {
        val ua = UserAnswers("id")
        navigator.nextPage(pages.manual.cpso.IndividualHavePlaceOfBirthPage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "IndividualHaveDateOfBirthPage" - {
      "must go to IndividualDateOfBirth page when answer is yes" in {
        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.IndividualHaveDateOfBirthPage(reportId, currentCPSOId), true)
        navigator.nextPage(pages.manual.cpso.IndividualHaveDateOfBirthPage(reportId, currentCPSOId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.CPSOIndividualDateOfBirthController.onPageLoad(NormalMode)
      }

      "must go to IndividualDateOfBirth page when answer is no" in {
        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.IndividualHaveDateOfBirthPage(reportId, currentCPSOId), false)
        navigator.nextPage(pages.manual.cpso.IndividualHaveDateOfBirthPage(reportId, currentCPSOId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.IndividualHavePlaceOfBirthController.onPageLoad(NormalMode)
      }

      "must go to JourneyRecovery page when page doesnt have value" in {
        val ua = UserAnswers("id")
        navigator.nextPage(pages.manual.cpso.IndividualHaveDateOfBirthPage(reportId, currentCPSOId), NormalMode, ua) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "IndividualPlaceOfBirthPage" - {
      "when regime is CRS" - {
        implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")

        "must go to controlling-person-type page when submitted" in {
          val ua = UserAnswers("id")
            .withPage(pages.manual.cpso.IndividualPlaceOfBirthPage(currentCPSOId, reportId),
              models.manual.cpso.IndividualPlaceOfBirth(Some("city"), Some("region"), "FR")
            )
          navigator.nextPage(pages.manual.cpso.IndividualPlaceOfBirthPage(currentCPSOId, reportId), NormalMode, ua) mustBe
            controllers.manual.cpso.routes.ControllingPersonTypeController.onPageLoad(NormalMode)
        }

      }
      "when regime is FATCA" - {
        implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")

        "must go to CPSOWhereAreTheyBased page when submitted" in {
          val ua = UserAnswers("id")
            .withPage(pages.manual.cpso.IndividualPlaceOfBirthPage(currentCPSOId, reportId),
              models.manual.cpso.IndividualPlaceOfBirth(Some("city"), Some("region"), "FR")
            )
          navigator.nextPage(pages.manual.cpso.IndividualPlaceOfBirthPage(currentCPSOId, reportId), NormalMode, ua) mustBe
            controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(NormalMode)
        }

      }
    }

    "CpsoOrganisationNamePage" - {
      "must go to CPSOWhereAreTheyBased page when submitted" in {
        val ua = UserAnswers("id")
          .withPage(CpsoOrganisationNamePage(currentCPSOId, reportId), "organisation-name")
        navigator.nextPage(CpsoOrganisationNamePage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(NormalMode)
      }
    }

    "CpsoSelfCertificationPage" - {
      "must go to CPSOWhereAreTheyBased page when submitted" in {
        val ua = UserAnswers("id")
          .withPage(CpsoSelfCertificationPage(currentCPSOId, reportId), Yes)
        navigator.nextPage(CpsoSelfCertificationPage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(NormalMode)
      }
    }

    "CPSOWhereAreTheyBased page" - {
      "must go to CPSOAddressNonUk page when No is submitted" in {
        val ua = UserAnswers("id")
          .withPage(CPSOWhereAreTheyBasedPage(currentCPSOId, reportId), false)
        navigator.nextPage(CPSOWhereAreTheyBasedPage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.CPSOAddressNonUkController.onPageLoad(NormalMode)
      }
      "must go to CPSO-Postcode uk post page when Yes is submitted" in {
        val ua = UserAnswers("id")
          .withPage(CPSOWhereAreTheyBasedPage(currentCPSOId, reportId), true)
        navigator.nextPage(CPSOWhereAreTheyBasedPage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.UkPostCodeController.onPageLoad(NormalMode)
      }
    }

    "UkPostcodePage  for cp-so" - {
      val currentCPSOId = CPSOId("testid")
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

      "must go to IsThisAddressForCPSO when one address is found" in {
        val address: Seq[AddressLookup] = Seq(addressLookup)

        val ua = UserAnswers("id")
          .withPage(UkPostCodePageForCpso(currentCPSOId, reportId), "ZZ1 1ZZ")
          .withPage(AddressLookupPageForCpso(currentCPSOId, reportId), address)
        navigator.nextPage(UkPostCodePageForCpso(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.IsThisTheAddressController.onPageLoad(NormalMode)
      }

      "must go to WhatIsAddressForCPSO when multiple addresses are found" in {
        val addresses: Seq[AddressLookup] = Seq(addressLookup, addressLookup)

        val ua = UserAnswers("id")
          .withPage(UkPostCodePageForCpso(currentCPSOId, reportId), "ZZ1 1ZZ")
          .withPage(AddressLookupPageForCpso(currentCPSOId, reportId), addresses)
        navigator.nextPage(UkPostCodePageForCpso(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.SelectAddressController.onPageLoad(NormalMode)
      }

      "must go to ProblemPage when no addresses are found" in {
        val ua = UserAnswers("id")
          .withPage(UkPostCodePageForCpso(currentCPSOId, reportId), "ZZ1 1ZZ")
          .withPage(AddressLookupPageForCpso(currentCPSOId, reportId), Seq.empty)

        navigator.nextPage(UkPostCodePageForCpso(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

      "must go to JourneyRecovery Page when AddressLookupPage is missing" in {
        val userData = UserAnswers("id")
        navigator.nextPage(HaveSponsorPage(), NormalMode, userData) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "CPSOAddressNonUk page" - {
      val country = Country("GB", "UK")
      val taxResidentInformation = TaxResidentInformation(country)

      "must go to resident-for-tax  page when submitted and there a no resident tax countries" in {
        val ua = UserAnswers("id")
          .withPage(CPSOAddressNonUkPage(currentCPSOId, reportId), nonUkAddress)
        navigator.nextPage(CPSOAddressNonUkPage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(NormalMode)
      }

      "must go to resident-for-tax  page when submitted and the resident tax countries list is empty" in {
        val ua = UserAnswers("id")
          .withPage(CPSOAddressNonUkPage(currentCPSOId, reportId), nonUkAddress)
          .withPage(pages.manual.cpso.TaxResidentCountriesListPage(currentCPSOId, reportId), Seq())

        navigator.nextPage(CPSOAddressNonUkPage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(NormalMode)
      }

      "must go to under construction page when submitted and the resident tax countries list is populated" in {
        val ua = UserAnswers("id")
          .withPage(CPSOAddressNonUkPage(currentCPSOId, reportId), nonUkAddress)
          .withPage(pages.manual.cpso.TaxResidentCountriesListPage(currentCPSOId, reportId), Seq(taxResidentInformation))

        navigator.nextPage(CPSOAddressNonUkPage(currentCPSOId, reportId), NormalMode, ua) mustBe
          controllers.routes.UnderConstructionController.onPageLoad()
      }
    }

    "SelectAddressPage for cpso" - {
      val country = Country("GB", "UK")
      val taxResidentInformation = TaxResidentInformation(country)

      implicit val reportId: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
      val address = Address(uprn = None,
        addressLine1 = "string",
        addressLine2 = None,
        addressLine3 = Some("string"),
        addressLine4 = None,
        town = "town",
        postCode = None,
        country = Country.GB
      )
      val currentId = CPSOId("holder-id")

      "must go to Tax resident page when there are no countries selected" in {
        val ua = UserAnswers("id")
          .withPage(SelectAddressPageForCPSO(currentId, reportId), address)

        navigator.nextPage(SelectAddressPageForCPSO(currentId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(NormalMode)
      }

      "must go to Tax resident page when the country list is empty" in {
        val ua = UserAnswers("id")
          .withPage(SelectAddressPageForCPSO(currentId, reportId), address)
          .withPage(pages.manual.cpso.TaxResidentCountriesListPage(currentId, reportId), Seq())

        navigator.nextPage(SelectAddressPageForCPSO(currentId, reportId), NormalMode, ua) mustBe
          controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(NormalMode)
      }

      "must go to under construction page when the country list is populated" in {
        val ua = UserAnswers("id")
          .withPage(SelectAddressPageForCPSO(currentId, reportId), address)
          .withPage(pages.manual.cpso.TaxResidentCountriesListPage(currentId, reportId), Seq(taxResidentInformation))

        navigator.nextPage(SelectAddressPageForCPSO(currentId, reportId), NormalMode, ua) mustBe
          controllers.routes.UnderConstructionController.onPageLoad()
      }
    }

    "ResidentForTaxPage cpso " - {
      implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")
      val currentCPSOId = CPSOId("testid")

      "must go to have tax identification page when regime is crs" in {

        val ua = UserAnswers("id")

        navigator.nextPage(
          ResidentForTaxPage(0, currentCPSOId, reportId),
          NormalMode,
          ua
        ) mustBe
          controllers.manual.cpso.routes.HaveTaxIdentificationNumberController.onPageLoad(NormalMode)
      }

      "must go to tax identification  page page when regime is fatca" in {
        implicit val reportId2: ReportId = ReportId(FATCA, 2024, None, "TestFIID")
        val ua = UserAnswers("id")

        navigator.nextPage(
          ResidentForTaxPage(0, currentCPSOId, reportId2),
          NormalMode,
          ua
        )(reportId2) mustBe
          controllers.manual.cpso.routes.TaxIdentificationNumberController.onPageLoad(NormalMode)
      }
    }

    "HaveTaxIdentificationNumberPage cpso" - {
      implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")
      val currentCPSOId = CPSOId("testid")

      "must go to have tax identification page when answer is yes" in {

        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.HaveTaxIdentificationNumberPage(0, currentCPSOId, reportId), true)

        navigator.nextPage(
          HaveTaxIdentificationNumberPage(0, currentCPSOId, reportId),
          NormalMode,
          ua
        ) mustBe
          controllers.manual.cpso.routes.TaxIdentificationNumberController.onPageLoad(NormalMode)
      }

      "must go to under construction page when answer is no" in {

        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.HaveTaxIdentificationNumberPage(0, currentCPSOId, reportId), false)

        navigator.nextPage(
          HaveTaxIdentificationNumberPage(0, currentCPSOId, reportId),
          NormalMode,
          ua
        ) mustBe
          controllers.routes.UnderConstructionController.onPageLoad()
      }

      "must go to journey recovery page when an answer is not provided" in {

        val ua = UserAnswers("id")

        navigator.nextPage(
          HaveTaxIdentificationNumberPage(0, currentCPSOId, reportId),
          NormalMode,
          ua
        ) mustBe
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

    }

    "TaxIdentificationNumberPage" - {
      implicit val reportId: ReportId = ReportId(CRS, 2024, None, "TestFIID")
      val currentCPSOId = CPSOId("testid")

      "must go to under construction page when answe" in {

        val ua = UserAnswers("id")
          .withPage(pages.manual.cpso.TaxIdentificationNumberPage(0, currentCPSOId, reportId), "some-number")

        navigator.nextPage(
          pages.manual.cpso.TaxIdentificationNumberPage(0, currentCPSOId, reportId),
          NormalMode,
          ua
        ) mustBe
          controllers.routes.UnderConstructionController.onPageLoad()
      }
    }
  }
}
