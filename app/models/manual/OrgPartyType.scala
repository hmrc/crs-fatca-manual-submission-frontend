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

import models.manual.accountHolders.{EquityInterestType, OrganisationCrs, OrganisationFatca, SelfCertification}
import models.manual.filercategory.{WhatTypeOfFiler, WhatTypeOfFilerIsSponsor}
import models.{AddressModel, ReportId}

sealed trait OrgPartyType {
  def name: String
  def address: AddressModel
  def residency: Seq[TaxResidentInformation]
  def reportId: ReportId
  def xmlElementName: String
}
object OrgPartyType {
  case class CrsAccountHolder(
                               name: String,
                               address: AddressModel,
                               residency: Seq[TaxResidentInformation],
                               reportId: ReportId,
                               accountHolderType: OrganisationCrs,
                               interestType: Option[EquityInterestType],
                               selfCert: SelfCertification
                             ) extends OrgPartyType{
    val xmlElementName: String = "AccountHolder"
  }

  case class FatcaAccountHolder(
                               name: String,
                               address: AddressModel,
                               residency: Seq[TaxResidentInformation],
                               reportId: ReportId,
                               accountHolderType: OrganisationFatca
                             ) extends OrgPartyType {
    val xmlElementName: String = "AccountHolder"
  }
  
  case class SubstantialOwner(
                                name: String,
                                address: AddressModel,
                                residency: Seq[TaxResidentInformation],
                                reportId: ReportId
                              ) extends OrgPartyType {
    val xmlElementName: String = "SubstantialOwner"
  }

  case class Sponsor(
                           name: String,
                           address: AddressModel,
                           residency: Seq[TaxResidentInformation],
                           reportId: ReportId,
                           filerCategory: WhatTypeOfFilerIsSponsor
                         ) extends OrgPartyType{
    val xmlElementName: String = "Sponsor"
  }

  case class ReportingFIWithFiler(
                      name: String,
                      address: AddressModel,
                      residency: Seq[TaxResidentInformation],
                      reportId: ReportId,
                      filerCategory: WhatTypeOfFiler
                    ) extends OrgPartyType{
    val xmlElementName: String = "ReportingFI"
  }

  case class ReportingFI(
                                   name: String,
                                   address: AddressModel,
                                   residency: Seq[TaxResidentInformation],
                                   reportId: ReportId,
                                 ) extends OrgPartyType{
    val xmlElementName: String = "ReportingFI"
  }
}