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

package utils.manual

import models.manual.accountHolders.AdditionalOrgAhInfo
import models.manual.{AccountHolder, OrganisationType, XmlEntityType}
import models.{AddressNonUk, ReportId, TaxResidentInformation, UkAddress}

import scala.xml.{Elem, NodeBuffer, NodeSeq}

object XmlMarshaller {

  extension (elem: Elem)

    private def withPrefixRecursive(newPrefix: String): Elem =
      elem.copy(
        prefix = newPrefix,
        child = elem.child.map {
          case childElem: Elem => childElem.withPrefixRecursive(newPrefix)
          case other           => other
        }
      )

  private def taxIdToXml(residency: TaxResidentInformation, repId: ReportId): NodeSeq =
    <sfa:ResCountryCode>
      {residency.country.code}
    </sfa:ResCountryCode>.copy(prefix = repId.getChildPrefix(true)) ++ {
      residency.taxIdentifierNumbers
        .map(
          taxId =>
            <sfa:TIN issuedBy={residency.country.code}>
          {taxId}
        </sfa:TIN>
              .copy(prefix = repId.getChildPrefix(true), label = repId.tinLabel)
        )
        .getOrElse(NodeSeq.Empty)
    }

  private def additionalAccountHolderToXml(ahData: AdditionalOrgAhInfo, repId: ReportId): NodeSeq =
    <ftc:AcctHolderType>{ahData.accountHolderType.toString}</ftc:AcctHolderType>.copy(prefix = repId.parentPrefix)
      ++ {
        ahData.selfCert
          .map(
            selfCertification => <crs:SelfCert>{selfCertification}</crs:SelfCert>
          )
          .getOrElse(NodeSeq.Empty)
      } ++ {
        ahData.interestType
          .map(
            intType => <crs:EquityInterestType>
                {intType.toString}
              </crs:EquityInterestType>
          )
          .getOrElse(NodeSeq.Empty)
      }

  private def ukAddressToAddressFix(address: UkAddress, repId: ReportId): NodeSeq =
    <sfa:Address>
      <sfa:CountryCode>
        {address.country}
      </sfa:CountryCode>
      <sfa:AddressFix>
        <sfa:Street>
          {address.addressLine1}
        </sfa:Street>{
      address.county
        .map(
          coun => <sfa:DistrictName>
        {coun}
      </sfa:DistrictName>
        )
        .getOrElse(NodeSeq.Empty)
    }<sfa:PostCode>
        {address.postcode}
      </sfa:PostCode>
        <sfa:City>
          {address.city}
        </sfa:City>
      </sfa:AddressFix>
      <sfa:AddressFree>
        {address.addressFreeFormat}
      </sfa:AddressFree>
    </sfa:Address>.withPrefixRecursive(newPrefix = repId.getChildPrefix(false)).copy(prefix = repId.addressPrefix)

  private def nonUkAddressToAddressFix(address: AddressNonUk, repId: ReportId): NodeSeq =
    <sfa:Address>
      <sfa:CountryCode>
        {address.country}
      </sfa:CountryCode>
      <sfa:AddressFix>
        <sfa:Street>
          {address.addressLine1}
        </sfa:Street>{
      address.addressLine4
        .map(
          reg => <sfa:DistrictName>
        {reg}
      </sfa:DistrictName>
        )
        .getOrElse(NodeSeq.Empty)
    }{
      address.postcode
        .map(
          postCode => <sfa:PostCode>
        {postCode}
      </sfa:PostCode>
        )
        .getOrElse(NodeSeq.Empty)
    }<sfa:City>
        {address.addressLine3}
      </sfa:City>
      </sfa:AddressFix>
      <sfa:AddressFree>
        {address.addressFreeFormat}
      </sfa:AddressFree>
    </sfa:Address>.withPrefixRecursive(newPrefix = repId.getChildPrefix(false)).copy(prefix = repId.addressPrefix)

  def toOrganisationPartyType(organisation: OrganisationType): NodeSeq = {
    val addressFix = organisation.address match {
      case uk: UkAddress        => ukAddressToAddressFix(uk, organisation.reportId)
      case abroad: AddressNonUk => nonUkAddressToAddressFix(abroad, organisation.reportId)
    }
    val orgName = <sfa:Name>{organisation.name}</sfa:Name>.copy(prefix = organisation.reportId.getChildPrefix(true))
    val taxIds  = organisation.residency.map(taxIdToXml(_, organisation.reportId))
    val additionalOrgAhInfo =
      if organisation.xmlEntityType == AccountHolder then
        organisation.additionalOrgAhInfo
          .map(
            add => additionalAccountHolderToXml(add, organisation.reportId)
          )
          .getOrElse(NodeSeq.Empty)
      else NodeSeq.Empty
    orgName +: (taxIds ++ additionalOrgAhInfo ++ addressFix).map(XmlEntityType.wrap(organisation.xmlEntityType, organisation.reportId))

  }
}
