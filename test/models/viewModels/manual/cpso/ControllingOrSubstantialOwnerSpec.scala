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

package models.viewModels.manual.cpso

import base.SpecBase
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.manual.TaxResidentInformation
import models.manual.accountHolders.IndividualDateOfBirth
import models.manual.cpso.IndividualOrOrganisation.{Individual, Organisation}
import models.manual.cpso.{ControllingPersonType, ControllingPersonTypes, CpsoSelfCertification, IndividualName}
import models.{AddressNonUk, UkAddress}
import models.response.{Address, Country}
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate

class ControllingOrSubstantialOwnerSpec extends SpecBase {

  "IsComplete" - {
    val controllingPersonTypes = ControllingPersonTypes(Seq(ControllingPersonType.OwnershipOfLegalPerson))
    val accountHolders         = Seq("ah-1")
    val individualDateOfBirth  = IndividualDateOfBirth(LocalDate.of(2000, 10, 12))
    val individualName         = IndividualName("First Name", "Last Name")
    val ukAddressSelect        = Address(None, "addressLine1", None, None, None, "town", None, Country.GB)
    val ukAddressManual        = UkAddress("Some Address", Some("Some Address"), "City", Some("Some Address"), "postcode: ", "coutry")
    val addressNonUk           = AddressNonUk(addressLine1 = "some address", None, "", None, None, "GB")
    val taxResidentInformation = TaxResidentInformation(Country.GB, hasTaxIdentificationNumber = Some(true), taxIdentifierNumber = Some("12333"))
    val taxResidentWithoutTin  = TaxResidentInformation(Country.GB, hasTaxIdentificationNumber = Some(true), taxIdentifierNumber = None)
    val taxResidentUnknownTin  = TaxResidentInformation(Country.GB, hasTaxIdentificationNumber = None, taxIdentifierNumber = None)
    val taxResidentNoTin       = TaxResidentInformation(Country.GB, hasTaxIdentificationNumber = Some(false), taxIdentifierNumber = None)
    val organisationName       = "Test Organisation Ltd"

    "CRS regime" - {

      val baseControllingPerson = ControllingOrSubstantialOwner(
        individualName = Some(individualName),
        accountHolders = Some(accountHolders),
        individualHaveDateOfBirth = Some(false),
        individualHavePlaceOfBirth = Some(false),
        areTheyBasedInTheUk = Some(false),
        taxResidentCountries = Some(Seq(taxResidentInformation)),
        addressNonUk = Some(addressNonUk),
        selfCertification = Some(CpsoSelfCertification.Yes),
        controllingPersonType = Some(controllingPersonTypes)
      )

      "is complete for a non-uk address controlling person that has all required fields completed" in {
        baseControllingPerson.isComplete(CRS) mustBe true
      }

      "is complete for uk address controlling person that has all required fields completed" in {
        baseControllingPerson.copy(areTheyBasedInTheUk = Some(true), ukAddressFromSelect = Some(ukAddressSelect)).isComplete(CRS) mustBe true
        baseControllingPerson.copy(areTheyBasedInTheUk = Some(true), ukAddressManual = Some(ukAddressManual)).isComplete(CRS) mustBe true
      }

      "returns false for non-uk address controlling person if any of expected fields are missing" - {
        mustBeIncomplete(CRS)(
          "the individual name is missing"         -> baseControllingPerson.copy(individualName = None),
          "the account holders are missing"        -> baseControllingPerson.copy(accountHolders = None),
          "the account holders are empty"          -> baseControllingPerson.copy(accountHolders = Some(Seq.empty)),
          "the date of birth question is missing"  -> baseControllingPerson.copy(individualHaveDateOfBirth = None),
          "the place of birth question is missing" -> baseControllingPerson.copy(individualHavePlaceOfBirth = None),
          "a date of birth is required but is missing" -> baseControllingPerson.copy(
            individualHaveDateOfBirth = Some(true),
            individualDateOfBirth = None
          ),
          "a place of birth is required but is missing" -> baseControllingPerson.copy(
            individualHavePlaceOfBirth = Some(true),
            individualPlaceOfBirth = None
          ),
          "the uk residency question is missing"              -> baseControllingPerson.copy(areTheyBasedInTheUk = None),
          "the non-uk address is missing"                     -> baseControllingPerson.copy(addressNonUk = None),
          "the tax resident countries are missing"            -> baseControllingPerson.copy(taxResidentCountries = None),
          "the tax resident countries are empty"              -> baseControllingPerson.copy(taxResidentCountries = Some(Seq.empty)),
          "a tax resident requires a TIN but it is missing"   -> baseControllingPerson.copy(taxResidentCountries = Some(Seq(taxResidentWithoutTin))),
          "the tax resident TIN status has not been answered" -> baseControllingPerson.copy(taxResidentCountries = Some(Seq(taxResidentUnknownTin))),
          "the self certification is missing"                 -> baseControllingPerson.copy(selfCertification = None),
          "the controlling person type is missing"            -> baseControllingPerson.copy(controllingPersonType = None)
        )
      }

      "returns false for non-uk address controlling person if any of expected fields are missing" - {
        mustBeIncomplete(CRS)(
          "the uk AddressFromSelect or ukAddressManual is missing" -> baseControllingPerson.copy(areTheyBasedInTheUk = Some(true))
        )
      }
    }

    "fatca regime" - {

      val baseFatcaIndividual = ControllingOrSubstantialOwner(
        individualOrOrganisation = Some(Individual),
        individualName = Some(individualName),
        accountHolders = Some(accountHolders),
        individualHaveDateOfBirth = Some(false),
        individualHavePlaceOfBirth = Some(false),
        areTheyBasedInTheUk = Some(false),
        taxResidentCountries = Some(Seq(taxResidentInformation)),
        addressNonUk = Some(addressNonUk)
      )

      val baseFatcaOrganisation = ControllingOrSubstantialOwner(
        individualOrOrganisation = Some(Organisation),
        organisationName = Some(organisationName),
        accountHolders = Some(accountHolders),
        areTheyBasedInTheUk = Some(false),
        taxResidentCountries = Some(Seq(taxResidentInformation)),
        addressNonUk = Some(addressNonUk)
      )

      "is complete for a non-uk address substantial owner that has all required fields completed" in {
        baseFatcaIndividual.isComplete(FATCA) mustBe true
      }

      "is complete for a uk address substantial owner that has all required fields completed" in {
        baseFatcaIndividual
          .copy(areTheyBasedInTheUk = Some(true), ukAddressFromSelect = Some(ukAddressSelect))
          .isComplete(FATCA) mustBe true

        baseFatcaIndividual
          .copy(areTheyBasedInTheUk = Some(true), ukAddressManual = Some(ukAddressManual))
          .isComplete(FATCA) mustBe true
      }

      "is complete for a non-uk address organisation substantial owner that has all required fields completed" in {
        baseFatcaOrganisation.isComplete(FATCA) mustBe true
      }

      "is complete for a uk address organisation substantial owner that has all required fields completed" in {
        baseFatcaOrganisation
          .copy(areTheyBasedInTheUk = Some(true), ukAddressFromSelect = Some(ukAddressSelect))
          .isComplete(FATCA) mustBe true

        baseFatcaOrganisation
          .copy(areTheyBasedInTheUk = Some(true), ukAddressManual = Some(ukAddressManual))
          .isComplete(FATCA) mustBe true
      }

      "returns false for non-uk address substantial owner if any of expected fields are missing" - {
        mustBeIncomplete(FATCA)(
          "the individual or organisation question is missing" -> baseFatcaIndividual.copy(individualOrOrganisation = None),
          "the individual name is missing"                     -> baseFatcaIndividual.copy(individualName = None),
          "the account holders are missing"                    -> baseFatcaIndividual.copy(accountHolders = None),
          "the account holders are empty"                      -> baseFatcaIndividual.copy(accountHolders = Some(Seq.empty)),
          "the date of birth question is missing"              -> baseFatcaIndividual.copy(individualHaveDateOfBirth = None),
          "the place of birth question is missing"             -> baseFatcaIndividual.copy(individualHavePlaceOfBirth = None),
          "a date of birth is required but is missing" -> baseFatcaIndividual.copy(
            individualHaveDateOfBirth = Some(true),
            individualDateOfBirth = None
          ),
          "a place of birth is required but is missing" -> baseFatcaIndividual.copy(
            individualHavePlaceOfBirth = Some(true),
            individualPlaceOfBirth = None
          ),
          "the uk residency question is missing"              -> baseFatcaIndividual.copy(areTheyBasedInTheUk = None),
          "the non-uk address is missing"                     -> baseFatcaIndividual.copy(addressNonUk = None),
          "the tax resident countries are missing"            -> baseFatcaIndividual.copy(taxResidentCountries = None),
          "the tax resident countries are empty"              -> baseFatcaIndividual.copy(taxResidentCountries = Some(Seq.empty)),
          "a tax resident requires a TIN but it is missing"   -> baseFatcaIndividual.copy(taxResidentCountries = Some(Seq(taxResidentWithoutTin))),
          "a tax resident does not have a TIN"                -> baseFatcaIndividual.copy(taxResidentCountries = Some(Seq(taxResidentNoTin))),
          "the tax resident TIN status has not been answered" -> baseFatcaIndividual.copy(taxResidentCountries = Some(Seq(taxResidentUnknownTin)))
        )
      }

      "returns false for non-uk address organisation substantial owner if any of expected fields are missing" - {
        mustBeIncomplete(FATCA)(
          "the organisation name is missing"                  -> baseFatcaOrganisation.copy(organisationName = None),
          "the account holders are missing"                   -> baseFatcaOrganisation.copy(accountHolders = None),
          "the account holders are empty"                     -> baseFatcaOrganisation.copy(accountHolders = Some(Seq.empty)),
          "the uk residency question is missing"              -> baseFatcaOrganisation.copy(areTheyBasedInTheUk = None),
          "the non-uk address is missing"                     -> baseFatcaOrganisation.copy(addressNonUk = None),
          "the tax resident countries are missing"            -> baseFatcaOrganisation.copy(taxResidentCountries = None),
          "the tax resident countries are empty"              -> baseFatcaOrganisation.copy(taxResidentCountries = Some(Seq.empty)),
          "a tax resident requires a TIN but it is missing"   -> baseFatcaOrganisation.copy(taxResidentCountries = Some(Seq(taxResidentWithoutTin))),
          "a tax resident does not have a TIN"                -> baseFatcaOrganisation.copy(taxResidentCountries = Some(Seq(taxResidentNoTin))),
          "the tax resident TIN status has not been answered" -> baseFatcaOrganisation.copy(taxResidentCountries = Some(Seq(taxResidentUnknownTin)))
        )
      }
    }
  }

  private def mustBeIncomplete(regime: RegimeType)(scenarios: (String, ControllingOrSubstantialOwner)*): Unit =
    scenarios.foreach {
      case (scenario, controllingOrSubstantialOwner) =>
        s"when $scenario" in {
          controllingOrSubstantialOwner.isComplete(regime) mustBe false
        }
    }
}
