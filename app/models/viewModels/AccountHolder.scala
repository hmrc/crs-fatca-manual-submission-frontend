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

import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.manual.TaxResidentInformation
import models.manual.accountHolders.*
import models.manual.accountHolders.IndividualOrOrganisation.*
import models.manual.accountHolders.OrganisationCrs.PassiveWithMultiplePersons
import models.manual.accountHolders.OrganisationFatca.{OwnerDocumented, Passive}
import models.response.Address
import models.{AddressNonUk, UkAddress}
import play.api.libs.json.{Json, OFormat}

import java.time.Instant

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
                         checkEquityInterestTypeRequired: Option[Boolean] = None, // crs only
                         creationDateTime: Option[Instant] = None
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
        (for {
          _            <- individualName
          _            <- individualHavePlaceOfBirth
          _            <- individualHaveDateOfBirth
          accountIds   <- account
          basedInUk    <- areTheyBasedInTheUk
          taxResidents <- taxResidentCountries
        } yield isAccountHolderLinkedToAccount(accountIds) &&
          addressComplete(basedInUk) &&
          taxResidencyComplete(regimeType, taxResidents) &&
          isIndividualBirthDetailsComplete() &&
          crsRequirements(regimeType)).getOrElse(false)

      case Some(Organisation) =>
        (for {
          _            <- organisationName
          _            <- accountHolderType
          accountIds   <- account
          basedInUk    <- areTheyBasedInTheUk
          taxResidents <- taxResidentCountries
        } yield isAccountHolderLinkedToAccount(accountIds) &&
          addressComplete(basedInUk) &&
          taxResidencyComplete(regimeType, taxResidents) &&
          crsRequirements(regimeType)).getOrElse(false)

      case None => false
    }

  private def addressComplete(basedInTheUk: Boolean): Boolean =
    if basedInTheUk then ukAddressManual.isDefined || ukAddressFromSelect.isDefined else addressNonUk.isDefined

  private def isAccountHolderLinkedToAccount(accountIds: Seq[String]): Boolean = accountIds.nonEmpty

  private def crsRequirements(regimeType: RegimeType): Boolean =
    regimeType != CRS || (selfCertification.isDefined && isEquityInterestTypeInformationComplete())

  private def taxResidencyComplete(regimeType: RegimeType, taxResCountries: Seq[TaxResidentInformation]): Boolean =
    regimeType match {
      case FATCA => taxResidentCountriesCompleteForFatca(taxResCountries)
      case CRS   => taxResidentCountriesCompleteForCRS(taxResCountries)
      case _     => false
    }

  private def taxResidentCountriesCompleteForFatca(taxResCountries: Seq[TaxResidentInformation]): Boolean =
    taxResCountries.nonEmpty && taxResCountries.forall(
      tx => tx.taxIdentifierNumber.isDefined
    )

  private def taxResidentCountriesCompleteForCRS(taxResCountries: Seq[TaxResidentInformation]): Boolean =
    taxResCountries.nonEmpty && taxResCountries.forall {
      case TaxResidentInformation(_, Some(true), taxIdentifiers) => taxIdentifiers.isDefined
      case TaxResidentInformation(_, Some(false), _)             => true
      case _                                                     => false
    }

  private def getCheckEquityInterestTypeRequired(): Boolean =
    checkEquityInterestTypeRequired.getOrElse(false)

  private def isEquityInterestTypeInformationComplete(): Boolean = {
    val isProvideEquityInterestTypeAnswered = !getCheckEquityInterestTypeRequired() || provideEquityInterestType.isDefined
    val isEquityInterestTypeProvided        = !provideEquityInterestType.contains(true) || equityInterestType.isDefined

    isProvideEquityInterestTypeAnswered && isEquityInterestTypeProvided
  }

  private def isIndividualBirthDetailsComplete(): Boolean =
    (!individualHavePlaceOfBirth.contains(true) || individualPlaceOfBirth.isDefined) &&
      (!individualHaveDateOfBirth.contains(true) || individualDateOfBirth.isDefined)

}

object AccountHolder {
  implicit val format: OFormat[AccountHolder] = Json.format[AccountHolder]
}
