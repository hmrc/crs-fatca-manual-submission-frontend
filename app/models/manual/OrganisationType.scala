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

import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.accountHolders.AdditionalOrgAhInfo
import models.manual.filercategory.{FilerCategoryTypes, WhatTypeOfFiler, WhatTypeOfFilerIsSponsor}
import models.{AddressModel, ReportId, TaxResidentInformation}
import utils.manual.XmlMarshaller

import scala.xml.NodeSeq

case class OrganisationType (name: String, address:AddressModel, residency: Seq[TaxResidentInformation], reportId: ReportId, xmlEntityType: XmlEntityType, additionalOrgAhInfo: Option[AdditionalOrgAhInfo], filerCategory: Option[FilerCategoryTypes]) {
  val hasWrongAccountHolderData: Boolean =  if (xmlEntityType == AccountHolder & reportId.regime == CRS ) {
    additionalOrgAhInfo.forall(_.hasIncorrectCrsData)
  } else if (xmlEntityType == AccountHolder & reportId.regime == FATCA) {
    additionalOrgAhInfo.isEmpty || additionalOrgAhInfo.forall(_.hasIncorrectFatcaData)
  } else if (xmlEntityType != AccountHolder & additionalOrgAhInfo.isDefined) then true else false
    
  

  val isIncorrectFilerProvided: Boolean =
    if(reportId.regime == FATCA & xmlEntityType == Sponsor) {
      !filerCategory.contains(WhatTypeOfFilerIsSponsor)}
    else if (reportId.regime == CRS || (reportId.regime == FATCA & xmlEntityType != ReportingFI)){
      filerCategory.isDefined
    } else false

}

object OrganisationType {
  def toXml(org:OrganisationType): Either[XmlErrors, NodeSeq] =
    if (org.hasWrongAccountHolderData) {
      Left(MissingMandatoryAccountHolderData)
    } else if ((org.xmlEntityType == Sponsor | org.xmlEntityType == SubstantialOwner) & org.reportId.regime == CRS){
      Left(InvalidObjectForRegime)
    }
    else if (org.xmlEntityType == ControllingPerson & org.reportId.regime == FATCA){
      Left(InvalidObjectForRegime)
    }
    else if (org.isIncorrectFilerProvided)  {
      Left(InvalidObjectForRegime)
    }
    else {
      Right(XmlMarshaller.toOrganisationPartyType(org))
    }
}

