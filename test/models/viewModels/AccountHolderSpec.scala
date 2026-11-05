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

package models.viewModels

import models.{AddressNonUk, UkAddress}
import models.SubmissionsConstants.{CRFA, CRS, FATCA, RegimeType}
import models.manual.TaxResidentInformation
import models.manual.accountHolders.AccountHolderTypes
import models.manual.accountHolders.AccountHolderTypes.*
import models.manual.accountHolders.IndividualOrOrganisation.Individual
import models.manual.accountHolders.IndividualOrOrganisation.Organisation
import models.response.{Address, Country}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class AccountHolderSpec extends AnyFreeSpec with Matchers {

  "AccountHolder.cpsoIsMandatory" - {
    Seq(
      Some(PassiveNonFinancialWithOneOrMoreControllingPerson) -> true,
      Some(CrsReportablePerson)                               -> false,
      Some(PassiveNonFinancialEntityThatIsCRSReportable)      -> false,
      None                                                    -> false
    ).foreach {
      case (accountHolderType, expected) =>
        s"must return $expected when the CRS organisation type is ${accountHolderType.getOrElse("not set")}" in {
          AccountHolder(individualOrOrganisation = Some(Organisation), accountHolderType = accountHolderType)
            .cpsoIsMandatory(CRS) mustBe expected
        }
    }

    Seq(
      Some(OwnerDocumentedForeignFinancialInstitution) -> false,
      Some(PassiveNFFEWithSubstantial)                 -> false,
      Some(NonParticipatingForeignFI)                  -> false,
      Some(SpecifiedUSPerson)                          -> false,
      None                                             -> false
    ).foreach {
      case (accountHolderType, expected) =>
        s"must return $expected when the FATCA organisation type is ${accountHolderType.getOrElse("not set")}" in {
          AccountHolder(individualOrOrganisation = Some(Organisation), accountHolderType = accountHolderType)
            .cpsoIsMandatory(CRS) mustBe expected
        }
    }

    "must return false for an individual account holder" in {
      AccountHolder(individualOrOrganisation = Some(Individual))
        .cpsoIsMandatory(CRS) mustBe false
    }

  }

  "AccountHolder.canHaveCPSO" - {

    "must be false for an individual account holder" - {

      Seq[RegimeType](CRS, FATCA, CRFA).foreach {
        regime =>
          s"for the ${regime.value} regime, even when an eligible organisation type is present" in {
            AccountHolder(
              individualOrOrganisation = Some(Individual)
            ).canHaveCPSO() mustBe false
          }
      }
    }

    "must be false when the individual or organisation question has not been answered" - {

      "Even when an eligible organisation type is present" in {
        AccountHolder(
          individualOrOrganisation = None,
          accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution)
        ).canHaveCPSO() mustBe false
      }

      "when no account holder type type is set" in {
        AccountHolder().canHaveCPSO() mustBe false
        AccountHolder().canHaveCPSO() mustBe false
      }
    }

    "for a CRS report" - {

      "must read the CRS account holder type" - {

        Seq(
          Some(PassiveNonFinancialWithOneOrMoreControllingPerson) -> true,
          Some(CrsReportablePerson)                               -> false,
          Some(PassiveNonFinancialEntityThatIsCRSReportable)      -> false,
          None                                                    -> false
        ).foreach {
          case (accountHolderType, expected) =>
            s"must return $expected when the CRS organisation type is ${accountHolderType.getOrElse("not set")}" in {
              AccountHolder(individualOrOrganisation = Some(Organisation), accountHolderType = accountHolderType)
                .canHaveCPSO() mustBe expected
            }
        }
      }
    }

    "for a FATCA report" - {

      "must read the FATCA accountHolder Type" - {

        Seq(
          Some(OwnerDocumentedForeignFinancialInstitution) -> true,
          Some(PassiveNFFEWithSubstantial)                 -> true,
          Some(NonParticipatingForeignFI)                  -> false,
          Some(SpecifiedUSPerson)                          -> false,
          None                                             -> false
        ).foreach {
          case (accountHolderType, expected) =>
            s"must return $expected when the FATCA account holder type is ${accountHolderType.getOrElse("not set")}" in {
              AccountHolder(individualOrOrganisation = Some(Organisation), accountHolderType = accountHolderType)
                .canHaveCPSO() mustBe expected
            }
        }
      }
    }

  }

  "IsComplete" - {
    "For an organization" - {
      val ukAddressSelect        = Address(None, "addressLine1", None, None, None, "town", None, Country.GB)
      val ukAddressManual        = UkAddress("Some Address", Some("Some Address"), "City", Some("Some Address"), "postcode: ", "coutry")
      val addressNonUk           = AddressNonUk(addressLine1 = "some address", None, "", None, None, "GB")
      val taxResidentInformation = TaxResidentInformation(Country.GB, hasTaxIdentificationNumber = None, taxIdentifierNumbers = Some("12333"))

      "is incomplete if missing individualOrOrganisation" in {
        val accountHolder = AccountHolder(
          individualOrOrganisation = None,
          organisationName = Some("Test Company"),
          accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
          areTheyBasedInTheUk = Some(false),
          taxResidentCountries = Some(Seq(taxResidentInformation)),
          addressNonUk = Some(addressNonUk)
        )

        accountHolder.isComplete(CRS) mustBe false
        accountHolder.isComplete(FATCA) mustBe false
      }

      "fatca" - {
        "returns isCompleted for a non-uk account holder that has all the required properties populated" in {
          val accountHolder = AccountHolder(
            individualOrOrganisation = Some(Organisation),
            organisationName = Some("Test Company"),
            accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
            areTheyBasedInTheUk = Some(false),
            taxResidentCountries = Some(Seq(taxResidentInformation)),
            addressNonUk = Some(addressNonUk)
          )

          accountHolder.isComplete(FATCA) mustBe true
        }

        "returns false for non-uk account holder if any of expected fields are missing" in {
          Seq(
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(false),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              addressNonUk = None
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(false),
              taxResidentCountries = None,
              addressNonUk = Some(addressNonUk)
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = None,
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              addressNonUk = Some(addressNonUk)
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = None,
              areTheyBasedInTheUk = Some(false),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              addressNonUk = Some(addressNonUk)
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = None,
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(false),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              addressNonUk = Some(addressNonUk)
            ),
            AccountHolder(
              individualOrOrganisation = None,
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(false),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              addressNonUk = Some(addressNonUk)
            )
          )
            .foreach(
              accountHolder => accountHolder.isComplete(FATCA) mustBe false
            )
        }

        "returns isCompleted for a uk account holder that has all the required properties populated" in {
          val accountHolder = AccountHolder(
            individualOrOrganisation = Some(Organisation),
            organisationName = Some("Test Company"),
            accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
            areTheyBasedInTheUk = Some(true),
            taxResidentCountries = Some(Seq(taxResidentInformation)),
            ukAddressManual = Some(ukAddressManual)
          )

          accountHolder.isComplete(FATCA) mustBe true

          AccountHolder(
            individualOrOrganisation = Some(Organisation),
            organisationName = Some("Test Company"),
            accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
            areTheyBasedInTheUk = Some(true),
            taxResidentCountries = Some(Seq(taxResidentInformation)),
            ukAddressFromSelect = Some(ukAddressSelect)
          ).isComplete(FATCA) mustBe true
        }

        "returns false for uk account holder if any of expected fields are missing" in {
          Seq(
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              ukAddressManual = None
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = None,
              ukAddressManual = Some(ukAddressManual)
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = None,
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              ukAddressManual = Some(ukAddressManual)
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = None,
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              addressNonUk = Some(addressNonUk)
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = None,
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              ukAddressManual = Some(ukAddressManual)
            ),
            AccountHolder(
              individualOrOrganisation = None,
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              ukAddressManual = Some(ukAddressManual)
            )
          )
            .foreach(
              accountHolder => accountHolder.isComplete(FATCA) mustBe false
            )

          Seq(
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              ukAddressFromSelect = None
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = None,
              ukAddressFromSelect = Some(ukAddressSelect)
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = None,
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              ukAddressFromSelect = Some(ukAddressSelect)
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = Some("Test Company"),
              accountHolderType = None,
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              ukAddressFromSelect = Some(ukAddressSelect)
            ),
            AccountHolder(
              individualOrOrganisation = Some(Organisation),
              organisationName = None,
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              ukAddressFromSelect = Some(ukAddressSelect)
            ),
            AccountHolder(
              individualOrOrganisation = None,
              organisationName = Some("Test Company"),
              accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
              areTheyBasedInTheUk = Some(true),
              taxResidentCountries = Some(Seq(taxResidentInformation)),
              ukAddressFromSelect = Some(ukAddressSelect)
            )
          )
            .foreach(
              accountHolder => accountHolder.isComplete(FATCA) mustBe false
            )
        }
      }
    }

    "For an individual" - {}
  }
}
