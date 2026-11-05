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

import cats.syntax.apply.*
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.manual.TaxResidentInformation
import models.manual.accountHolders.IndividualOrOrganisation.*
import models.manual.accountHolders.OrganisationCrs.PassiveWithMultiplePersons
import models.manual.accountHolders.OrganisationFatca.{OwnerDocumented, Passive}
import models.manual.accountHolders.*
import models.response.Address
import models.{AddressNonUk, UkAddress}
import play.api.libs.json.{Json, OFormat}

case class AccountHolder(individualOrOrganisation: Option[IndividualOrOrganisation] = None,
                         organisationName: Option[String] = None,
                         accountHolderType: Option[AccountHolderTypes] = None,
                         individualName: Option[IndividualName] = None,
                         areTheyBasedInTheUk: Option[Boolean] = None,
                         addressNonUk: Option[AddressNonUk] = None,
                         selfCertification: Option[SelfCertification] = None,
                         taxResidentCountries: Option[Seq[TaxResidentInformation]] = None,
                         ukAddressFromSelect: Option[Address] = None,
                         ukAddressManual: Option[UkAddress] = None,
                         individualHavePlaceOfBirth: Option[Boolean] = None,
                         individualHaveDateOfBirth: Option[Boolean] = None,
                         individualDateOfBirth: Option[IndividualDateOfBirth] = None,
                         individualPlaceOfBirth: Option[IndividualPlaceOfBirth] = None,
                         account: Option[Seq[String]] = None,
                         provideEquityInterestType: Option[Boolean] = None,
                         equityInterestType: Option[EquityInterestTypes] = None,
                         checkEquityInterestTypeRequired: Option[Boolean] = None //crs only
) {

  def canHaveCPSO() =
    individualOrOrganisation match {
      case None             => false
      case Some(Individual) => false
      case Some(Organisation) =>
        accountHolderType
          .map(_.toString)
          .exists(
            i => Seq(PassiveWithMultiplePersons.toString, OwnerDocumented.toString, Passive.toString).contains(i)
          )
    }

  def getName(): String =
    organisationName
      .getOrElse(individualName.map(_.fullName).getOrElse(""))

  def cpsoIsMandatory(regimeType: RegimeType): Boolean =
    individualOrOrganisation match {
      case None             => false
      case Some(Individual) => false
      case Some(Organisation) =>
        regimeType == CRS && accountHolderType.map(_.toString).contains(PassiveWithMultiplePersons.toString)
    }

  def isComplete(regimeType: RegimeType): Boolean =
    individualOrOrganisation match {
      case Some(Individual) =>
        regimeType match {
          case FATCA =>
            validateFatcaIndividualIsComplete()
          case CRS =>
            true
          case _ => false
        }
      case Some(Organisation) =>
        regimeType match {
          case FATCA =>
            validateFatcaOrgIsComplete()
          case CRS =>
            true
          case _ => false
        }
      case None => false
    }

  private def validateFatcaOrgIsComplete() = {

    val bareMinimum = (individualOrOrganisation, organisationName, accountHolderType, areTheyBasedInTheUk, taxResidentCountries)
    (areTheyBasedInTheUk, areTheyBasedInTheUk.contains(true)) match {
      case (None, _) => false
      case (Some(_), false) =>
        (bareMinimum :* addressNonUk)
          .mapN {
            (_, _, _, _, _, _) =>
              true
          }
          .getOrElse(false)
      case (Some(_), true) =>
        val isManualAddressEntryValid = (bareMinimum :* ukAddressManual)
          .mapN {
            (_, _, _, _, _, _) =>
              true
          }
          .getOrElse(false)

        val isSelectAddressEntryValid = (bareMinimum :* ukAddressFromSelect)
          .mapN {
            (_, _, _, _, _, _) =>
              true
          }
          .getOrElse(false)

        isManualAddressEntryValid || isSelectAddressEntryValid
    }
  }

  private def validateFatcaIndividualIsComplete(): Boolean = {
    val bareMinimumNonUk =
      (individualOrOrganisation, individualName, individualHavePlaceOfBirth, individualHaveDateOfBirth, areTheyBasedInTheUk, taxResidentCountries, addressNonUk)
    val bareMinimum =
      (individualOrOrganisation, individualName, individualHavePlaceOfBirth, individualHaveDateOfBirth, areTheyBasedInTheUk, taxResidentCountries)

    if (areTheyBasedInTheUk.contains(false)) {
      (individualHavePlaceOfBirth, individualHaveDateOfBirth, individualHavePlaceOfBirth.contains(true), individualHaveDateOfBirth.contains(true)) match {
        case (None, None, _, _) => false
        case (Some(_), None, true, _) =>
          (bareMinimumNonUk :* individualPlaceOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
        case (None, Some(_), _, true) =>
          (bareMinimumNonUk :* individualDateOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
        case (Some(_), Some(_), true, true) =>
          ((bareMinimumNonUk :* individualDateOfBirth) :* individualPlaceOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
        case (Some(_), Some(_), false, false) =>
          bareMinimumNonUk
            .mapN {
              (_, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
        case (Some(_), Some(_), true, false) =>
          (bareMinimumNonUk :* individualPlaceOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
        case (Some(_), Some(_), false, true) =>
          (bareMinimumNonUk :* individualDateOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
        case _ =>
          false
      }
    } else if (areTheyBasedInTheUk.contains(true)) {
      (individualHavePlaceOfBirth, individualHaveDateOfBirth, individualHavePlaceOfBirth.contains(true), individualHaveDateOfBirth.contains(true)) match {
        case (None, None, _, _) => false
        case (Some(_), None, true, _) =>
          val manualAddressValid = ((bareMinimum :* individualPlaceOfBirth) :* ukAddressManual)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)

          val selectAddressValid = ((bareMinimum :* individualPlaceOfBirth) :* ukAddressFromSelect)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
          manualAddressValid || selectAddressValid
        case (None, Some(_), _, true) =>
          val manualAddressValid = ((bareMinimum :* individualDateOfBirth) :* ukAddressManual)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)

          val selectAddressValid = ((bareMinimum :* individualDateOfBirth) :* ukAddressFromSelect)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
          manualAddressValid || selectAddressValid
        case (Some(_), Some(_), true, true) => // individualDateOfBirth) :* individualPlaceOfBirth
          val manualAddressValid = (((bareMinimum :* individualDateOfBirth) :* ukAddressManual) :* individualPlaceOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)

          val selectAddressValid = (((bareMinimum :* individualDateOfBirth) :* ukAddressFromSelect) :* individualPlaceOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
          manualAddressValid || selectAddressValid
        case (Some(_), Some(_), false, false) =>
          val manualAddressValid = (bareMinimum :* ukAddressManual)
            .mapN {
              (_, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)

          val selectAddressValid = (bareMinimum :* ukAddressFromSelect)
            .mapN {
              (_, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
          manualAddressValid || selectAddressValid
        case (Some(_), Some(_), true, false) => // bareMinimumNonUk :* individualPlaceOfBirth)
          val manualAddressValid = ((bareMinimum :* ukAddressManual) :* individualPlaceOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)

          val selectAddressValid = ((bareMinimum :* ukAddressFromSelect) :* individualPlaceOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
          manualAddressValid || selectAddressValid
        case (Some(_), Some(_), false, true) =>
          val manualAddressValid = ((bareMinimum :* ukAddressManual) :* individualDateOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)

          val selectAddressValid = ((bareMinimum :* ukAddressFromSelect) :* individualDateOfBirth)
            .mapN {
              (_, _, _, _, _, _, _, _) =>
                true
            }
            .getOrElse(false)
          manualAddressValid || selectAddressValid
        case _ => false
      }

    } else {
      false
    }

  }

}

object AccountHolder {
  implicit val format: OFormat[AccountHolder] = Json.format[AccountHolder]
}
//ukAddressFromSelect - Address
//ukAddressManual - UkAddress
