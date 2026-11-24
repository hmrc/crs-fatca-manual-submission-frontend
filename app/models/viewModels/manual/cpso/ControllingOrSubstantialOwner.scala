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

import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.manual.TaxResidentInformation
import models.manual.accountHolders.IndividualDateOfBirth
import models.manual.cpso.IndividualOrOrganisation.{Individual, Organisation}
import models.manual.cpso.{ControllingPersonTypes, CpsoSelfCertification, IndividualName, IndividualOrOrganisation, IndividualPlaceOfBirth}
import models.response.Address
import models.{AddressNonUk, ReportId, UkAddress, UserAnswers}
import pages.manual.cpso.ControllingOrSubstantialOwnersPage
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.*

import java.time.Instant

case class ControllingOrSubstantialOwner(accountHolders: Option[Seq[String]] = None,
                                         individualName: Option[IndividualName] = None,
                                         organisationName: Option[String] = None,
                                         individualOrOrganisation: Option[IndividualOrOrganisation] = None,
                                         individualHaveDateOfBirth: Option[Boolean] = None,
                                         individualDateOfBirth: Option[IndividualDateOfBirth] = None,
                                         individualHavePlaceOfBirth: Option[Boolean] = None,
                                         individualPlaceOfBirth: Option[IndividualPlaceOfBirth] = None,
                                         controllingPersonType: Option[ControllingPersonTypes] = None,
                                         selfCertification: Option[CpsoSelfCertification] = None,
                                         areTheyBasedInTheUk: Option[Boolean] = None,
                                         addressNonUk: Option[AddressNonUk] = None,
                                         ukAddressFromSelect: Option[Address] = None,
                                         ukAddressManual: Option[UkAddress] = None,
                                         taxResidentCountries: Option[Seq[TaxResidentInformation]] = None,
                                         creationDateTime: Option[Instant] = None
) {

  def name = organisationName.getOrElse(individualName.map(_.fullName).getOrElse(""))

  def isComplete(regimeType: RegimeType): Boolean =
    individualOrOrganisation match {
      case None | Some(Individual) =>
        (for {
          _                <- individualName
          _                <- individualHavePlaceOfBirth
          _                <- individualHaveDateOfBirth
          accountHolderIds <- accountHolders
          basedInUk        <- areTheyBasedInTheUk
          taxResidents     <- taxResidentCountries
        } yield isCpsoLinkedToAccountHolder(accountHolderIds) &&
          isIndividualBirthDetailsComplete() &&
          taxResidencyComplete(regimeType, taxResidents) &&
          addressComplete(basedInUk) &&
          fatcaRequirements(regimeType) &&
          crsRequirements(regimeType)).getOrElse(false)
      case Some(Organisation) =>
        (for {
          _                <- organisationName
          accountHolderIds <- accountHolders
          basedInUk        <- areTheyBasedInTheUk
          taxResidents     <- taxResidentCountries
        } yield isCpsoLinkedToAccountHolder(accountHolderIds) &&
          addressComplete(basedInUk) &&
          taxResidencyComplete(regimeType, taxResidents)).getOrElse(false)
    }

  private def isCpsoLinkedToAccountHolder(accountHolderIds: Seq[String]): Boolean = accountHolderIds.nonEmpty

  private def fatcaRequirements(regimeType: RegimeType) =
    regimeType != FATCA || individualOrOrganisation.contains(Individual)

  private def isIndividualBirthDetailsComplete(): Boolean =
    (!individualHavePlaceOfBirth.contains(true) || individualPlaceOfBirth.isDefined) &&
      (!individualHaveDateOfBirth.contains(true) || individualDateOfBirth.isDefined)

  private def addressComplete(basedInTheUk: Boolean): Boolean =
    if basedInTheUk then ukAddressManual.isDefined || ukAddressFromSelect.isDefined else addressNonUk.isDefined

  private def crsRequirements(regimeType: RegimeType): Boolean =
    regimeType != CRS || (selfCertification.isDefined && controllingPersonType.isDefined)

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

}

object ControllingOrSubstantialOwner {
  implicit val format: OFormat[ControllingOrSubstantialOwner] = Json.format[ControllingOrSubstantialOwner]
}

case class ControllingOrSubstantialOwners(currentCpsoId: Option[CPSOId],
                                          cpsos: Map[String, ControllingOrSubstantialOwner],
                                          doYouWantToAddAnotherCpso: Option[Boolean] = None,
                                          cpsoSectionComplete: Option[Boolean] = None
)

object ControllingOrSubstantialOwners {

  private val CurrentcpsoId             = "currentCPSOId"
  private val DoYouWantToAddAnotherCpso = "doYouWantToAddAnotherCpso"
  private val CpsoSectionComplete       = "cpsoSectionComplete"

  implicit val format: OFormat[ControllingOrSubstantialOwners] = new OFormat[ControllingOrSubstantialOwners] {

    override def reads(json: JsValue): JsResult[ControllingOrSubstantialOwners] =
      json.validate[JsObject].flatMap {
        obj =>

          val cpsoId =
            (obj \ CurrentcpsoId).validateOpt[CPSOId]

          val doYouWantToAddAnotherCpso =
            (obj \ DoYouWantToAddAnotherCpso).validateOpt[Boolean]

          val cpsoSectionComplete =
            (obj \ CpsoSectionComplete).validateOpt[Boolean]

          val cpsoEntries =
            JsObject(
              obj.fields.filterNot(
                f => f._1 == CurrentcpsoId || f._1 == DoYouWantToAddAnotherCpso || f._1 == CpsoSectionComplete
              )
            ).validate[Map[String, ControllingOrSubstantialOwner]]

          for {
            currentId              <- cpsoId
            cpso                   <- cpsoEntries
            addAnotherCPSO         <- doYouWantToAddAnotherCpso
            addCpsoSectionComplete <- cpsoSectionComplete
          } yield ControllingOrSubstantialOwners(currentId, cpso, addCpsoSectionComplete)
      }

    override def writes(value: ControllingOrSubstantialOwners): JsObject = {
      val cpsoJson =
        JsObject(
          value.cpsos.map {
            case (id, cpso) =>
              id -> Json.toJson(cpso)
          }
        )

      value.currentCpsoId match {
        case Some(id) =>
          cpsoJson + (CurrentcpsoId -> Json.toJson(id))

        case None =>
          cpsoJson
      }
    }
  }

  def existingCpsos(userAnswers: UserAnswers)(implicit reportId: ReportId): Seq[(CPSOId, ControllingOrSubstantialOwner)] =
    userAnswers
      .get(ControllingOrSubstantialOwnersPage())
      .map(
        _.cpsos
          .map(
            (cpsoId, controllingOrSubstantialOwner) => (CPSOId(cpsoId), controllingOrSubstantialOwner)
          )
          .toSeq
      )
      .getOrElse(Seq.empty)
}
