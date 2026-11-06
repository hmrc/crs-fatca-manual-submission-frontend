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
import models.manual.accountHolders.EquityInterestType.SettlorOfTrust
import models.manual.accountHolders.IndividualOrOrganisation.Individual
import models.manual.accountHolders.IndividualOrOrganisation.Organisation
import models.manual.accountHolders.{EquityInterestTypes, IndividualDateOfBirth, IndividualName, IndividualPlaceOfBirth, SelfCertification}
import models.response.{Address, Country}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDate

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
    val ukAddressSelect        = Address(None, "addressLine1", None, None, None, "town", None, Country.GB)
    val ukAddressManual        = UkAddress("Some Address", Some("Some Address"), "City", Some("Some Address"), "postcode: ", "coutry")
    val addressNonUk           = AddressNonUk(addressLine1 = "some address", None, "", None, None, "GB")
    val taxResidentInformation = TaxResidentInformation(Country.GB, hasTaxIdentificationNumber = None, taxIdentifierNumbers = Some("12333"))

    "fatca regime" - {
      "For an organization" - {
        val baseAccountHolder = AccountHolder(
          individualOrOrganisation = Some(Organisation),
          organisationName = Some("Test Company"),
          accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution),
          areTheyBasedInTheUk = Some(false),
          taxResidentCountries = Some(Seq(taxResidentInformation)),
          addressNonUk = Some(addressNonUk)
        )

        "is incomplete if missing individualOrOrganisation" in {
          val accountHolder = baseAccountHolder.copy(individualOrOrganisation = None)

          accountHolder.isComplete(CRS) mustBe false
          accountHolder.isComplete(FATCA) mustBe false
        }

        "returns isCompleted for a non-uk account holder that has all the required properties populated" in {
          baseAccountHolder.isComplete(FATCA) mustBe true
        }

        "returns false for non-uk account holder if any of expected fields are missing" - {
          mustBeIncomplete(FATCA)(
            "the non-uk address is missing"                         -> baseAccountHolder.copy(addressNonUk = None),
            "the tax resident countries are missing"                -> baseAccountHolder.copy(taxResidentCountries = None),
            "the uk residency question is unanswered"               -> baseAccountHolder.copy(areTheyBasedInTheUk = None),
            "the account holder type is missing"                    -> baseAccountHolder.copy(accountHolderType = None),
            "the organisation name is missing"                      -> baseAccountHolder.copy(organisationName = None),
            "the individual or organisation question is unanswered" -> baseAccountHolder.copy(individualOrOrganisation = None)
          )
        }

        "returns isCompleted for a uk account holder that has all the required properties populated" in {
          baseAccountHolder
            .copy(areTheyBasedInTheUk = Some(true), addressNonUk = None, ukAddressManual = Some(ukAddressManual))
            .isComplete(FATCA) mustBe true

          baseAccountHolder
            .copy(areTheyBasedInTheUk = Some(true), addressNonUk = None, ukAddressFromSelect = Some(ukAddressSelect))
            .isComplete(FATCA) mustBe true
        }

        "returns false for uk account holder if any of expected fields are missing" - {
          val manualAccountHolder =
            baseAccountHolder.copy(areTheyBasedInTheUk = Some(true), addressNonUk = None, ukAddressManual = Some(ukAddressManual))

          val selectAccountHolder =
            baseAccountHolder.copy(areTheyBasedInTheUk = Some(true), addressNonUk = None, ukAddressFromSelect = Some(ukAddressSelect))

          "when the uk address is entered manually" - {
            mustBeIncomplete(FATCA)(
              "the uk address is missing"                             -> manualAccountHolder.copy(ukAddressManual = None),
              "the tax resident countries are missing"                -> manualAccountHolder.copy(taxResidentCountries = None),
              "the uk residency question is unanswered"               -> manualAccountHolder.copy(areTheyBasedInTheUk = None),
              "the account holder type is missing"                    -> manualAccountHolder.copy(accountHolderType = None),
              "the organisation name is missing"                      -> manualAccountHolder.copy(organisationName = None),
              "the individual or organisation question is unanswered" -> manualAccountHolder.copy(individualOrOrganisation = None)
            )
          }

          "when the uk address is chosen from the lookup" - {
            mustBeIncomplete(FATCA)(
              "the uk address is missing"                             -> selectAccountHolder.copy(ukAddressFromSelect = None),
              "the tax resident countries are missing"                -> selectAccountHolder.copy(taxResidentCountries = None),
              "the uk residency question is unanswered"               -> selectAccountHolder.copy(areTheyBasedInTheUk = None),
              "the account holder type is missing"                    -> selectAccountHolder.copy(accountHolderType = None),
              "the organisation name is missing"                      -> selectAccountHolder.copy(organisationName = None),
              "the individual or organisation question is unanswered" -> selectAccountHolder.copy(individualOrOrganisation = None)
            )
          }
        }
      }

      "For an Individual" - {
        val individualName         = IndividualName("John", "Smith")
        val individualDateOfBirth  = IndividualDateOfBirth(LocalDate.of(1990, 1, 1))
        val individualPlaceOfBirth = IndividualPlaceOfBirth(Some("London"), None, "GB")

        val nonUkAccountHolder = AccountHolder(
          individualOrOrganisation = Some(Individual),
          individualName = Some(individualName),
          individualHavePlaceOfBirth = Some(true),
          individualHaveDateOfBirth = Some(true),
          individualDateOfBirth = Some(individualDateOfBirth),
          individualPlaceOfBirth = Some(individualPlaceOfBirth),
          areTheyBasedInTheUk = Some(false),
          taxResidentCountries = Some(Seq(taxResidentInformation)),
          addressNonUk = Some(addressNonUk)
        )

        val ukAccountHolder = AccountHolder(
          individualOrOrganisation = Some(Individual),
          individualName = Some(individualName),
          individualHavePlaceOfBirth = Some(true),
          individualHaveDateOfBirth = Some(true),
          individualDateOfBirth = Some(individualDateOfBirth),
          individualPlaceOfBirth = Some(individualPlaceOfBirth),
          areTheyBasedInTheUk = Some(true),
          taxResidentCountries = Some(Seq(taxResidentInformation)),
          ukAddressManual = Some(ukAddressManual)
        )

        "returns isCompleted for a non-uk account holder that has all the required properties populated" in {
          nonUkAccountHolder.isComplete(FATCA) mustBe true

          Seq(
            "a place of birth and a date of birth" -> nonUkAccountHolder,
            "a place of birth only" -> nonUkAccountHolder.copy(
              individualHaveDateOfBirth = Some(false),
              individualDateOfBirth = None
            ),
            "a date of birth only" -> nonUkAccountHolder.copy(
              individualHavePlaceOfBirth = Some(false),
              individualPlaceOfBirth = None
            ),
            "neither a place of birth nor a date of birth" -> nonUkAccountHolder.copy(
              individualHavePlaceOfBirth = Some(false),
              individualHaveDateOfBirth = Some(false),
              individualPlaceOfBirth = None,
              individualDateOfBirth = None
            )
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with $scenario: ") {
                accountHolder.isComplete(FATCA) mustBe true
              }
          }
        }

        "returns false for non-uk account holder if any of expected fields are missing" - {
          mustBeIncomplete(FATCA)(
            "the individual or organisation question is unanswered" -> nonUkAccountHolder.copy(individualOrOrganisation = None),
            "the individual name is missing"                        -> nonUkAccountHolder.copy(individualName = None),
            "the place of birth question is unanswered"             -> nonUkAccountHolder.copy(individualHavePlaceOfBirth = None),
            "the date of birth question is unanswered"              -> nonUkAccountHolder.copy(individualHaveDateOfBirth = None),
            "a place of birth is declared but missing"              -> nonUkAccountHolder.copy(individualPlaceOfBirth = None),
            "a date of birth is declared but missing"               -> nonUkAccountHolder.copy(individualDateOfBirth = None),
            "the uk residency question is unanswered"               -> nonUkAccountHolder.copy(areTheyBasedInTheUk = None),
            "the tax resident countries are missing"                -> nonUkAccountHolder.copy(taxResidentCountries = None),
            "the non-uk address is missing"                         -> nonUkAccountHolder.copy(addressNonUk = None),
            "a uk address is provided instead of a non-uk address" -> nonUkAccountHolder.copy(
              addressNonUk = None,
              ukAddressManual = Some(ukAddressManual)
            )
          )
        }

        "returns isCompleted for a uk account holder that has all the required properties populated" in {
          ukAccountHolder.isComplete(FATCA) mustBe true

          Seq(
            "a manually entered uk address" -> ukAccountHolder,
            "a uk address chosen from the address lookup" -> ukAccountHolder.copy(
              ukAddressManual = None,
              ukAddressFromSelect = Some(ukAddressSelect)
            ),
            "neither a place of birth nor a date of birth" -> ukAccountHolder.copy(
              individualHavePlaceOfBirth = Some(false),
              individualHaveDateOfBirth = Some(false),
              individualPlaceOfBirth = None,
              individualDateOfBirth = None
            )
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with $scenario: ") {
                accountHolder.isComplete(FATCA) mustBe true
              }
          }
        }

        "returns false for uk account holder if any of expected fields are missing" - {
          mustBeIncomplete(FATCA)(
            "the individual or organisation question is unanswered" -> ukAccountHolder.copy(individualOrOrganisation = None),
            "the individual name is missing"                        -> ukAccountHolder.copy(individualName = None),
            "the place of birth question is unanswered"             -> ukAccountHolder.copy(individualHavePlaceOfBirth = None),
            "the date of birth question is unanswered"              -> ukAccountHolder.copy(individualHaveDateOfBirth = None),
            "a place of birth is declared but missing"              -> ukAccountHolder.copy(individualPlaceOfBirth = None),
            "a date of birth is declared but missing"               -> ukAccountHolder.copy(individualDateOfBirth = None),
            "the uk residency question is unanswered"               -> ukAccountHolder.copy(areTheyBasedInTheUk = None),
            "the tax resident countries are missing"                -> ukAccountHolder.copy(taxResidentCountries = None),
            "the uk address is missing"                             -> ukAccountHolder.copy(ukAddressManual = None),
            "a non-uk address is provided instead of a uk address" -> ukAccountHolder.copy(
              ukAddressManual = None,
              addressNonUk = Some(addressNonUk)
            ),
            "a uk address is provided for an account holder based outside the uk" -> ukAccountHolder.copy(
              areTheyBasedInTheUk = Some(false)
            )
          )
        }
      }
    }

    "crs regime" - {
      "For an organisation" - {
        val baseAccountHolder = AccountHolder(
          individualOrOrganisation = Some(Organisation),
          organisationName = Some("Test Company"),
          accountHolderType = Some(PassiveNonFinancialEntityThatIsCRSReportable),
          selfCertification = Some(SelfCertification.Yes),
          taxResidentCountries = Some(Seq(taxResidentInformation))
        )

        val nonUkAccountHolder = baseAccountHolder.copy(
          areTheyBasedInTheUk = Some(false),
          addressNonUk = Some(addressNonUk)
        )

        val ukAccountHolder = baseAccountHolder.copy(
          areTheyBasedInTheUk = Some(true),
          ukAddressManual = Some(ukAddressManual)
        )

        val equityInterestTypes = EquityInterestTypes(Seq(SettlorOfTrust))

        "returns isCompleted for a non-uk account holder that has all the required properties populated" in {
          nonUkAccountHolder.isComplete(CRS) mustBe true

          Seq(
            "no equity interest type questions answered" -> nonUkAccountHolder,
            "the equity interest type is not required" -> nonUkAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(false)
            ),
            "the account holder does not provide an equity interest type" -> nonUkAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(true),
              provideEquityInterestType = Some(false)
            ),
            "the account holder provides an equity interest type" -> nonUkAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(true),
              provideEquityInterestType = Some(true),
              equityInterestType = Some(equityInterestTypes)
            )
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with $scenario: ") {
                accountHolder.isComplete(CRS) mustBe true
              }
          }
        }

        "returns false for non-uk account holder if any of expected fields are missing" - {
          mustBeIncomplete(CRS)(
            "the individual or organisation question is unanswered" -> nonUkAccountHolder.copy(individualOrOrganisation = None),
            "the organisation name is missing"                      -> nonUkAccountHolder.copy(organisationName = None),
            "the account holder type is missing"                    -> nonUkAccountHolder.copy(accountHolderType = None),
            "the self certification is missing"                     -> nonUkAccountHolder.copy(selfCertification = None),
            "the tax resident countries are missing"                -> nonUkAccountHolder.copy(taxResidentCountries = None),
            "the uk residency question is unanswered"               -> nonUkAccountHolder.copy(areTheyBasedInTheUk = None),
            "the non-uk address is missing"                         -> nonUkAccountHolder.copy(addressNonUk = None),
            "a uk address is provided instead of a non-uk address" -> nonUkAccountHolder.copy(
              addressNonUk = None,
              ukAddressManual = Some(ukAddressManual)
            )
          )
        }

        "returns isCompleted for a uk account holder that has all the required properties populated" in {
          ukAccountHolder.isComplete(CRS) mustBe true

          Seq(
            "a manually entered uk address" -> ukAccountHolder,
            "a uk address chosen from the address lookup" -> ukAccountHolder.copy(
              ukAddressManual = None,
              ukAddressFromSelect = Some(ukAddressSelect)
            ),
            "the equity interest type is not required" -> ukAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(false)
            ),
            "the account holder does not provide an equity interest type" -> ukAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(true),
              provideEquityInterestType = Some(false)
            ),
            "the account holder provides an equity interest type" -> ukAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(true),
              provideEquityInterestType = Some(true),
              equityInterestType = Some(equityInterestTypes)
            )
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with $scenario: ") {
                accountHolder.isComplete(CRS) mustBe true
              }
          }
        }

        "returns false for uk account holder if any of expected fields are missing" - {
          mustBeIncomplete(CRS)(
            "the individual or organisation question is unanswered" -> ukAccountHolder.copy(individualOrOrganisation = None),
            "the organisation name is missing"                      -> ukAccountHolder.copy(organisationName = None),
            "the account holder type is missing"                    -> ukAccountHolder.copy(accountHolderType = None),
            "the self certification is missing"                     -> ukAccountHolder.copy(selfCertification = None),
            "the tax resident countries are missing"                -> ukAccountHolder.copy(taxResidentCountries = None),
            "the uk residency question is unanswered"               -> ukAccountHolder.copy(areTheyBasedInTheUk = None),
            "the uk address is missing"                             -> ukAccountHolder.copy(ukAddressManual = None),
            "a non-uk address is provided instead of a uk address" -> ukAccountHolder.copy(
              ukAddressManual = None,
              addressNonUk = Some(addressNonUk)
            ),
            "a uk address is provided for an account holder based outside the uk" -> ukAccountHolder.copy(
              areTheyBasedInTheUk = Some(false)
            )
          )
        }

        "returns false when the equity interest type is required but the account holder has not said whether they provide one" in {
          Seq(
            "based in the uk"      -> ukAccountHolder,
            "based outside the uk" -> nonUkAccountHolder
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with an account holder $scenario: ") {
                accountHolder.copy(checkEquityInterestTypeRequired = Some(true)).isComplete(CRS) mustBe false
              }
          }
        }

        "returns false when the account holder provides an equity interest type but the type itself is missing" in {
          Seq(
            "based in the uk"      -> ukAccountHolder,
            "based outside the uk" -> nonUkAccountHolder
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with an account holder $scenario: ") {
                accountHolder.copy(provideEquityInterestType = Some(true)).isComplete(CRS) mustBe false
              }
          }
        }
      }

      "For an Individual" - {
        val individualName         = IndividualName("John", "Smith")
        val individualDateOfBirth  = IndividualDateOfBirth(LocalDate.of(1990, 1, 1))
        val individualPlaceOfBirth = IndividualPlaceOfBirth(Some("London"), None, "GB")

        val baseAccountHolder = AccountHolder(
          individualOrOrganisation = Some(Individual),
          individualName = Some(individualName),
          individualHavePlaceOfBirth = Some(true),
          individualHaveDateOfBirth = Some(true),
          individualDateOfBirth = Some(individualDateOfBirth),
          individualPlaceOfBirth = Some(individualPlaceOfBirth),
          selfCertification = Some(SelfCertification.Yes),
          taxResidentCountries = Some(Seq(taxResidentInformation))
        )

        val nonUkAccountHolder = baseAccountHolder.copy(
          areTheyBasedInTheUk = Some(false),
          addressNonUk = Some(addressNonUk)
        )

        val ukAccountHolder = baseAccountHolder.copy(
          areTheyBasedInTheUk = Some(true),
          ukAddressManual = Some(ukAddressManual)
        )

        val equityInterestTypes = EquityInterestTypes(Seq(SettlorOfTrust))

        "returns isCompleted for a non-uk account holder that has all the required properties populated" in {
          nonUkAccountHolder.isComplete(CRS) mustBe true

          Seq(
            "a place of birth and a date of birth" -> nonUkAccountHolder,
            "no place of birth" -> nonUkAccountHolder.copy(
              individualHavePlaceOfBirth = Some(false),
              individualPlaceOfBirth = None
            ),
            "no date of birth" -> nonUkAccountHolder.copy(
              individualHaveDateOfBirth = Some(false),
              individualDateOfBirth = None
            ),
            "neither a place of birth nor a date of birth" -> nonUkAccountHolder.copy(
              individualHavePlaceOfBirth = Some(false),
              individualHaveDateOfBirth = Some(false),
              individualPlaceOfBirth = None,
              individualDateOfBirth = None
            ),
            "the equity interest type is not required" -> nonUkAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(false)
            ),
            "the account holder does not provide an equity interest type" -> nonUkAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(true),
              provideEquityInterestType = Some(false)
            ),
            "the account holder provides an equity interest type" -> nonUkAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(true),
              provideEquityInterestType = Some(true),
              equityInterestType = Some(equityInterestTypes)
            )
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with $scenario: ") {
                accountHolder.isComplete(CRS) mustBe true
              }
          }
        }

        "returns false for non-uk account holder if any of expected fields are missing" - {
          mustBeIncomplete(CRS)(
            "the individual or organisation question is unanswered" -> nonUkAccountHolder.copy(individualOrOrganisation = None),
            "the individual name is missing"                        -> nonUkAccountHolder.copy(individualName = None),
            "the place of birth question is unanswered"             -> nonUkAccountHolder.copy(individualHavePlaceOfBirth = None),
            "the date of birth question is unanswered"              -> nonUkAccountHolder.copy(individualHaveDateOfBirth = None),
            "a place of birth is declared but missing"              -> nonUkAccountHolder.copy(individualPlaceOfBirth = None),
            "a date of birth is declared but missing"               -> nonUkAccountHolder.copy(individualDateOfBirth = None),
            "the self certification is missing"                     -> nonUkAccountHolder.copy(selfCertification = None),
            "the tax resident countries are missing"                -> nonUkAccountHolder.copy(taxResidentCountries = None),
            "the uk residency question is unanswered"               -> nonUkAccountHolder.copy(areTheyBasedInTheUk = None),
            "the non-uk address is missing"                         -> nonUkAccountHolder.copy(addressNonUk = None),
            "a uk address is provided instead of a non-uk address" -> nonUkAccountHolder.copy(
              addressNonUk = None,
              ukAddressManual = Some(ukAddressManual)
            )
          )
        }

        "returns isCompleted for a uk account holder that has all the required properties populated" in {
          ukAccountHolder.isComplete(CRS) mustBe true

          Seq(
            "a manually entered uk address" -> ukAccountHolder,
            "a uk address chosen from the address lookup" -> ukAccountHolder.copy(
              ukAddressManual = None,
              ukAddressFromSelect = Some(ukAddressSelect)
            ),
            "neither a place of birth nor a date of birth" -> ukAccountHolder.copy(
              individualHavePlaceOfBirth = Some(false),
              individualHaveDateOfBirth = Some(false),
              individualPlaceOfBirth = None,
              individualDateOfBirth = None
            ),
            "the equity interest type is not required" -> ukAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(false)
            ),
            "the account holder does not provide an equity interest type" -> ukAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(true),
              provideEquityInterestType = Some(false)
            ),
            "the account holder provides an equity interest type" -> ukAccountHolder.copy(
              checkEquityInterestTypeRequired = Some(true),
              provideEquityInterestType = Some(true),
              equityInterestType = Some(equityInterestTypes)
            )
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with $scenario: ") {
                accountHolder.isComplete(CRS) mustBe true
              }
          }
        }

        "returns false for uk account holder if any of expected fields are missing" - {
          mustBeIncomplete(CRS)(
            "the individual or organisation question is unanswered" -> ukAccountHolder.copy(individualOrOrganisation = None),
            "the individual name is missing"                        -> ukAccountHolder.copy(individualName = None),
            "the place of birth question is unanswered"             -> ukAccountHolder.copy(individualHavePlaceOfBirth = None),
            "the date of birth question is unanswered"              -> ukAccountHolder.copy(individualHaveDateOfBirth = None),
            "a place of birth is declared but missing"              -> ukAccountHolder.copy(individualPlaceOfBirth = None),
            "a date of birth is declared but missing"               -> ukAccountHolder.copy(individualDateOfBirth = None),
            "the self certification is missing"                     -> ukAccountHolder.copy(selfCertification = None),
            "the tax resident countries are missing"                -> ukAccountHolder.copy(taxResidentCountries = None),
            "the uk residency question is unanswered"               -> ukAccountHolder.copy(areTheyBasedInTheUk = None),
            "the uk address is missing"                             -> ukAccountHolder.copy(ukAddressManual = None),
            "a non-uk address is provided instead of a uk address" -> ukAccountHolder.copy(
              ukAddressManual = None,
              addressNonUk = Some(addressNonUk)
            ),
            "a uk address is provided for an account holder based outside the uk" -> ukAccountHolder.copy(
              areTheyBasedInTheUk = Some(false)
            )
          )
        }

        "returns false when the equity interest type is required but the account holder has not said whether they provide one" in {
          Seq(
            "based in the uk"      -> ukAccountHolder,
            "based outside the uk" -> nonUkAccountHolder
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with an account holder $scenario: ") {
                accountHolder.copy(checkEquityInterestTypeRequired = Some(true)).isComplete(CRS) mustBe false
              }
          }
        }

        "returns false when the account holder provides an equity interest type but the type itself is missing" in {
          Seq(
            "based in the uk"      -> ukAccountHolder,
            "based outside the uk" -> nonUkAccountHolder
          ).foreach {
            case (scenario, accountHolder) =>
              withClue(s"with an account holder $scenario: ") {
                accountHolder.copy(provideEquityInterestType = Some(true)).isComplete(CRS) mustBe false
              }
          }
        }
      }
    }
  }

  private def mustBeIncomplete(regime: RegimeType)(scenarios: (String, AccountHolder)*): Unit =
    scenarios.foreach {
      case (scenario, accountHolder) =>
        s"when $scenario" in {
          accountHolder.isComplete(regime) mustBe false
        }
    }

}
