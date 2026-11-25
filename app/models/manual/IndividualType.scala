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

package models.manual

import models.manual.accountHolders.*
import models.manual.cpso.{ControllingPersonType, CpsoSelfCertification}
import models.{AddressModel, ReportId}

import java.time.LocalDate

sealed trait IndividualType {
  def firstName: String
  def lastName: String
  def birthDate: Option[LocalDate]
  def country: Option[CountryBirthInfo]
  def city: Option[CityBirthInfo]
  def region: Option[RegionBirthInfo]
  def reportId: ReportId
  def address: AddressModel
  def residency: Seq[TaxResidentInformation]
  def xmlElementName: String
}

object IndividualType {

  case class CrsIndividualAccountHolder(
    individualName: models.manual.accountHolders.IndividualName,
    dateOfBirth: Option[IndividualDateOfBirth],
    placeOfBirth: Option[models.manual.accountHolders.IndividualPlaceOfBirth],
    address: AddressModel,
    residency: Seq[TaxResidentInformation],
    reportId: ReportId,
    interestType: Option[EquityInterestType],
    selfCert: SelfCertification
  ) extends IndividualType {
    val xmlElementName: String       = "AccountHolder"
    val firstName: String            = individualName.firstName
    val lastName: String             = individualName.lastName
    val birthDate: Option[LocalDate] = dateOfBirth.map(_.dateOfBirth)

    val country: Option[CountryBirthInfo] = placeOfBirth.map(
      p => CountryBirthInfo(p.country)
    )

    val city: Option[CityBirthInfo] = placeOfBirth.flatMap(
      p => p.city.map(CityBirthInfo(_))
    )

    val region: Option[RegionBirthInfo] = placeOfBirth.flatMap(
      p => p.region.map(RegionBirthInfo(_))
    )
  }

  case class ControllingPerson(
    individualName: models.manual.cpso.IndividualName,
    dateOfBirth: Option[IndividualDateOfBirth],
    placeOfBirth: Option[models.manual.cpso.IndividualPlaceOfBirth],
    address: AddressModel,
    residency: Seq[TaxResidentInformation],
    reportId: ReportId,
    cpType: ControllingPersonType,
    selfCert: CpsoSelfCertification
  ) extends IndividualType {
    val xmlElementName: String       = "ControllingPerson"
    val firstName: String            = individualName.firstName
    val lastName: String             = individualName.lastName
    val birthDate: Option[LocalDate] = dateOfBirth.map(_.dateOfBirth)

    val country: Option[CountryBirthInfo] = placeOfBirth.map(
      p => CountryBirthInfo(p.country)
    )

    val city: Option[CityBirthInfo] = placeOfBirth.flatMap(
      p => p.city.map(CityBirthInfo(_))
    )

    val region: Option[RegionBirthInfo] = placeOfBirth.flatMap(
      p => p.region.map(RegionBirthInfo(_))
    )
  }

  case class SubstantialOwner(
    individualName: models.manual.cpso.IndividualName,
    dateOfBirth: Option[IndividualDateOfBirth],
    placeOfBirth: Option[models.manual.cpso.IndividualPlaceOfBirth],
    address: AddressModel,
    residency: Seq[TaxResidentInformation],
    reportId: ReportId
  ) extends IndividualType {
    val xmlElementName: String       = "SubstantialOwner"
    val firstName: String            = individualName.firstName
    val lastName: String             = individualName.lastName
    val birthDate: Option[LocalDate] = dateOfBirth.map(_.dateOfBirth)

    val country: Option[CountryBirthInfo] = placeOfBirth.map(
      p => CountryBirthInfo(p.country)
    )

    val city: Option[CityBirthInfo] = placeOfBirth.flatMap(
      p => p.city.map(CityBirthInfo(_))
    )

    val region: Option[RegionBirthInfo] = placeOfBirth.flatMap(
      p => p.region.map(RegionBirthInfo(_))
    )
  }

}
