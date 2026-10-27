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

import models.SubmissionsConstants.{CRS, RegimeType}
import models.manual.accountHolders.IndividualOrOrganisation
import play.api.libs.json.{Json, OFormat}

case class AccountHolder(individualOrOrganisation: Option[IndividualOrOrganisation] = None,
                         organisationName: Option[String] = None,
                         organisationFatca: Option[String] = None,
                         organisationCrs: Option[String] = None
) {

  private def accountHolderType(regimeType: RegimeType) = if regimeType == CRS then organisationCrs else organisationFatca

  def canHaveCPSO(regimeType: RegimeType) =
    individualOrOrganisation match {
      case None                                      => false
      case Some(IndividualOrOrganisation.Individual) => false
      case Some(IndividualOrOrganisation.Organisation) =>
        accountHolderType(regimeType: RegimeType).exists(
          i => Seq("CRS101", "FATCA101", "FATCA102").contains(i)
        )
    }

  def cpsoIsMandatory(regimeType: RegimeType): Boolean =
    individualOrOrganisation match {
      case None                                      => false
      case Some(IndividualOrOrganisation.Individual) => false
      case Some(IndividualOrOrganisation.Organisation) =>
        regimeType == CRS && accountHolderType(regimeType: RegimeType).contains("CRS101")
    }
}

object AccountHolder {
  implicit val format: OFormat[AccountHolder] = Json.format[AccountHolder]
}
