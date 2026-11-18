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

package models.manual.sponsor

import models.{AddressModel, AddressNonUk, TaxResidentInformation, UkAddress}
import play.api.libs.json.{Json, OFormat}

import scala.xml.{Elem, NodeBuffer}

case class SponsorDetail(sponsorName: String, giin: String, address:AddressModel, residency: Seq[TaxResidentInformation]) {             
  val addressFix : Elem = address match{
    case e: AddressNonUk => e.toAddressFix
    case e: UkAddress => e.toAddressFix
  }

  def toXml(taxId:TaxResidentInformation): NodeBuffer =
    val countryCode: String = taxId.country.code
    <sfa:ResCountryCode>$
      {countryCode}
    </sfa:ResCountryCode>
      <sfa:TIN issuedBy="${countryCode}">a</sfa:TIN>
    
    
    
  val toOrg: Elem =
  <ftc:Sponsor>
                  ${residency.map(toXml)}
                  <sfa:Name>${sponsorName}</sfa:Name>
                  ${addressFix}
  </ftc:Sponsor>
}

object SponsorDetail {
  implicit val format: OFormat[SponsorDetail]= Json.format[SponsorDetail]
}
