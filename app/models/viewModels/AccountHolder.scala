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
import models.manual.account.WhatAccountType
import models.manual.accountHolders.IndividualOrOrganisation.*
import models.manual.accountHolders.OrganisationCrs.PassiveWithMultiplePersons
import models.manual.accountHolders.OrganisationFatca.{OwnerDocumented, Passive}
import models.manual.accountHolders.*
import models.response.Address
import models.{AddressNonUk, ReportId, UkAddress, UserAnswers}
import pages.manual.account.WhatAccountTypePage
import pages.manual.accountHolders.{AccountHolderAddressNonUkPage, AccountHolderIndividualNamePage, AccountHolderOrganisationNamePage, AccountHolderPage, IndividualDateOfBirthPage, IndividualHaveDateOfBirthPage, IndividualHavePlaceOfBirthPage, IndividualOrOrganisationPage, IndividualPlaceOfBirthPage, IsThisTheAddressForAccountHoldersPage, OrganisationCrsPage, OrganisationFatcaPage, SelectAddressPage, WhatIsAddressForAccountHolderPage, WhereAreTheyBasedPage}
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
                         checkEquityInterestTypeRequired: Option[Boolean] = None // crs only
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



  def isAccountHolderComplete(userAnswers: UserAnswers, accountHolderId: AccountHolderId)(implicit reportId: ReportId) = {
    for {
      indOrg <- userAnswers.get(IndividualOrOrganisationPage(accountHolderId))
      _ <- userAnswers.get(AccountHolderIndividualNamePage(accountHolderId)).orElse(userAnswers.get(AccountHolderOrganisationNamePage(accountHolderId)))
      accountHolderDetail <- isIndividualAccountHolderComplete(userAnswers, accountHolderId, indOrg)
      isAhBasedInUk <- userAnswers.get(WhereAreTheyBasedPage(accountHolderId))
      _ <- if isAhBasedInUk then userAnswers.get(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId)).map(_ =>true).orElse(userAnswers.get(SelectAddressPage(accountHolderId, reportId))).map(_=>true)
        .orElse(userAnswers.get(WhatIsAddressForAccountHolderPage(accountHolderId, reportId)).map(_=>true)) else userAnswers.get(AccountHolderAddressNonUkPage(accountHolderId, reportId)).map(_ =>true)
      accountType <- userAnswers.get(WhatAccountTypePage()())
    }
  }


//  def remove(userAnswers: UserAnswers, accountHolderId: AccountHolderId)(implicit reportId: ReportId)=
//   val s = userAnswers.get(AccountHolderPage(accountHolderId,reportId)).get

  def isIndividualAccountHolderComplete(userAnswers: UserAnswers, accountHolderId: AccountHolderId, individualOrOrganisation: IndividualOrOrganisation)(implicit reportId: ReportId): Option[Boolean] =
    individualOrOrganisation match{
      case Individual=>
      for{
        _ <-  userAnswers.get(AccountHolderIndividualNamePage(accountHolderId))
        hasDob <- userAnswers.get(IndividualHaveDateOfBirthPage(accountHolderId))
        _ <- if hasDob then userAnswers.get(IndividualDateOfBirthPage(accountHolderId)) else Option("not required")
        hasPob <- userAnswers.get(IndividualHavePlaceOfBirthPage(accountHolderId))
        - <- if hasDob then userAnswers.get(IndividualPlaceOfBirthPage(accountHolderId, reportId)) else Option("not required")
      } yield true
      case Organisation => userAnswers.get(AccountHolderOrganisationNamePage(accountHolderId)).flatMap(
        _ => userAnswers.get(OrganisationCrsPage(accountHolderId)).orElse(userAnswers.get(OrganisationFatcaPage(accountHolderId)))
      ).map(_ => true)
    }


  def isComplete(regimeType: RegimeType): Boolean =
    individualOrOrganisation match {
      case Some(Individual) =>
        regimeType match {
          case FATCA =>
            validateFatcaIndividualIsComplete()
          case CRS =>
            validateCRSIndividualIsComplete()
          case _ => false
        }
      case Some(Organisation) =>
        regimeType match {
          case FATCA =>
            validateFatcaOrgIsComplete()
          case CRS =>
            validateCRSOrgIsComplete()
          case _ => false
        }
      case None => false
    }

  private def getCheckEquityInterestTypeRequired(): Boolean =
    checkEquityInterestTypeRequired.getOrElse(false)

  private def validateFatcaOrgIsComplete() = {

    val bareMinimum = (individualOrOrganisation, organisationName, accountHolderType, areTheyBasedInTheUk, taxResidentCountries)
    (areTheyBasedInTheUk, areTheyBasedInTheUk.contains(true)) match {
      case (None, _) => false
      case (Some(_), false) =>
        (bareMinimum :* addressNonUk)
          .mapN {
            (_, _, _, _, taxResCountries, _) =>
              taxResidentCountriesCompleteForFatca(taxResCountries)
          }
          .getOrElse(false)
      case (Some(_), true) =>
        val isManualAddressEntryValid = (bareMinimum :* ukAddressManual)
          .mapN {
            (_, _, _, _, taxResCountries, _) =>
              taxResidentCountriesCompleteForFatca(taxResCountries)
          }
          .getOrElse(false)

        val isSelectAddressEntryValid = (bareMinimum :* ukAddressFromSelect)
          .mapN {
            (_, _, _, _, taxResCountries, _) =>
              taxResidentCountriesCompleteForFatca(taxResCountries)
          }
          .getOrElse(false)

        isManualAddressEntryValid || isSelectAddressEntryValid
    }
  }

  private def validateCRSOrgIsComplete(): Boolean = {
    val bareMinimum = (individualOrOrganisation, organisationName, accountHolderType, areTheyBasedInTheUk, taxResidentCountries, selfCertification)

    (areTheyBasedInTheUk, areTheyBasedInTheUk.contains(true)) match {
      case (None, _) =>
        false

      case (Some(_), false) =>
        val isAddressEntryValid = (bareMinimum :* addressNonUk)
          .mapN {
            (_, _, _, _, txResList, _, _) =>
              taxResidentCountriesCompleteForCRS(txResList)
          }
          .getOrElse(false)

        isAddressEntryValid && isEquityInterestTypeInformationComplete()

      case (Some(_), true) =>
        val isManualAddressEntryValid = (bareMinimum :* ukAddressManual)
          .mapN {
            (_, _, _, _, txResList, _, _) =>
              taxResidentCountriesCompleteForCRS(txResList)
          }
          .getOrElse(false)

        val isSelectAddressEntryValid = (bareMinimum :* ukAddressFromSelect)
          .mapN {
            (_, _, _, _, txResList, _, _) =>
              taxResidentCountriesCompleteForCRS(txResList)
          }
          .getOrElse(false)

        (isManualAddressEntryValid || isSelectAddressEntryValid) && isEquityInterestTypeInformationComplete()
    }
  }

  private def validateCRSIndividualIsComplete(): Boolean = {
    val bareMinimumNonUk =
      (individualOrOrganisation,
       individualName,
       individualHavePlaceOfBirth,
       individualHaveDateOfBirth,
       areTheyBasedInTheUk,
       taxResidentCountries,
       addressNonUk,
       selfCertification
      )

    val bareMinimum =
      (individualOrOrganisation,
       individualName,
       individualHavePlaceOfBirth,
       individualHaveDateOfBirth,
       areTheyBasedInTheUk,
       taxResidentCountries,
       selfCertification
      )

    (areTheyBasedInTheUk, areTheyBasedInTheUk.contains(true)) match {
      case (None, _) =>
        false

      case (Some(_), false) =>
        val isAccountHolderDetailsValid = bareMinimumNonUk
          .mapN {
            (_, _, _, _, _, txResList, _, _) =>
              taxResidentCountriesCompleteForCRS(txResList)
          }
          .getOrElse(false)
        isAccountHolderDetailsValid && isIndividualBirthDetailsComplete() && isEquityInterestTypeInformationComplete()

      case (Some(_), true) =>
        val isManualAddressEntryValid = (bareMinimum :* ukAddressManual)
          .mapN {
            (_, _, _, _, _, txResList, _, _) =>
              taxResidentCountriesCompleteForCRS(txResList)
          }
          .getOrElse(false)

        val isSelectAddressEntryValid = (bareMinimum :* ukAddressFromSelect)
          .mapN {
            (_, _, _, _, _, txResList, _, _) =>
              taxResidentCountriesCompleteForCRS(txResList)
          }
          .getOrElse(false)

        (isManualAddressEntryValid || isSelectAddressEntryValid) && isIndividualBirthDetailsComplete() && isEquityInterestTypeInformationComplete()
    }
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

  private def validateFatcaIndividualIsComplete(): Boolean = {
    val bareMinimumNonUk =
      (individualOrOrganisation, individualName, individualHavePlaceOfBirth, individualHaveDateOfBirth, areTheyBasedInTheUk, taxResidentCountries, addressNonUk)

    val bareMinimum =
      (individualOrOrganisation, individualName, individualHavePlaceOfBirth, individualHaveDateOfBirth, areTheyBasedInTheUk, taxResidentCountries)

    (areTheyBasedInTheUk, areTheyBasedInTheUk.contains(true)) match {
      case (None, _) =>
        false

      case (Some(_), false) =>
        val isAccountHolderDetailsValid = bareMinimumNonUk
          .mapN {
            (_, _, _, _, _, taxResCountries, _) =>
              taxResidentCountriesCompleteForFatca(taxResCountries)
          }
          .getOrElse(false)

        isAccountHolderDetailsValid && isIndividualBirthDetailsComplete()

      case (Some(_), true) =>
        val isManualAddressEntryValid = (bareMinimum :* ukAddressManual)
          .mapN {
            (_, _, _, _, _, taxResCountries, _) =>
              taxResidentCountriesCompleteForFatca(taxResCountries)
          }
          .getOrElse(false)

        val isSelectAddressEntryValid = (bareMinimum :* ukAddressFromSelect)
          .mapN {
            (_, _, _, _, _, taxResCountries, _) =>
              taxResidentCountriesCompleteForFatca(taxResCountries)
          }
          .getOrElse(false)

        (isManualAddressEntryValid || isSelectAddressEntryValid) && isIndividualBirthDetailsComplete()
    }
  }

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
