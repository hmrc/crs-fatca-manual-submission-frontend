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
import models.manual.accountHolders.{AccountHolderTypes, IndividualOrOrganisation}
import models.manual.accountHolders.OrganisationCrs.PassiveWithMultiplePersons
import models.manual.accountHolders.OrganisationFatca.{OwnerDocumented, Passive}
import play.api.libs.json.{Json, OFormat}

case class AccountHolder(individualOrOrganisation: Option[IndividualOrOrganisation] = None,
                         organisationName: Option[String] = None,
                         accountHolderType: Option[AccountHolderTypes] = None
) {

  def canHaveCPSO() =
    individualOrOrganisation match {
      case None                                      => false
      case Some(IndividualOrOrganisation.Individual) => false
      case Some(IndividualOrOrganisation.Organisation) =>
        accountHolderType
          .map(_.toString)
          .exists(
            i => Seq(PassiveWithMultiplePersons.toString, OwnerDocumented.toString, Passive.toString).contains(i)
          )
    }

  def cpsoIsMandatory(regimeType: RegimeType): Boolean =
    individualOrOrganisation match {
      case None                                      => false
      case Some(IndividualOrOrganisation.Individual) => false
      case Some(IndividualOrOrganisation.Organisation) =>
        regimeType == CRS && accountHolderType.map(_.toString).contains(PassiveWithMultiplePersons.toString)
    }
}

object AccountHolder {
  implicit val format: OFormat[AccountHolder] = Json.format[AccountHolder]
}
