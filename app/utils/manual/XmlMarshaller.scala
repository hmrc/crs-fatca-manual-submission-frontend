package utils.manual

import models.SubmissionsConstants.FATCA
import models.{AddressNonUk, ReportId, TaxResidentInformation, UkAddress}
import models.manual.{OrganisationType, Soooz}

import scala.xml.{Elem, NodeBuffer, NodeSeq}

object XmlMarshaller {



  extension (elem: Elem)
    def withPrefixRecursive(newPrefix: String): Elem =
      elem.copy(
        prefix = newPrefix,
        child = elem.child.map {
          case childElem: Elem => childElem.withPrefixRecursive(newPrefix)
          case other => other
        }
      )
      
  def taxIdToXml(residency: TaxResidentInformation,repId:ReportId ): NodeSeq = {
    val countryCode: String = residency.country.code
      <sfa:ResCountryCode>$
        {countryCode}
      </sfa:ResCountryCode>
      NodeSeq.fromSeq(residency.taxIdentifierNumbers.map(taxId => <sfa:TIN issuedBy={countryCode}>taxId</sfa:TIN>
      .copy(prefix= repId.sfaOrCrsPrefix, label = repId.tinLabel)).getOrElse(NodeSeq.Empty))
  }



  def ukAddressToAddressFix(address:UkAddress, repId:ReportId): NodeSeq ={
    <sfa:Address>
      <sfa:CountryCode>$
        {address.country}
      </sfa:CountryCode>
      <sfa:AddressFix>
        <sfa:Street>${address.addressLine1}</sfa:Street>
        ${address.county.map(coun=> <sfa:DistrictName>coun</sfa:DistrictName>).getOrElse(NodeSeq.Empty)}
        <sfa:PostCode>${address.postcode}</sfa:PostCode>
        <sfa:City>${address.city}</sfa:City>
      </sfa:AddressFix>
      <sfa:AddressFree>${address.wholeAddress}</sfa:AddressFree>
    </sfa:Address>.withPrefixRecursive(newPrefix = repId.sfaOrCrsPrefix)
}


  def nonUkAddressToAddressFix(address:AddressNonUk, repId:ReportId): NodeSeq ={
    <sfa:Address>
      <sfa:CountryCode>${address.country}</sfa:CountryCode>
      <sfa:AddressFix>
        <sfa:Street>${address.addressLine1}</sfa:Street>
        ${address.addressLine4.map(reg=> <sfa:DistrictName>reg</sfa:DistrictName>).getOrElse(NodeSeq.Empty)}
        ${address.postcode.map(p=> <sfa:PostCode>p</sfa:PostCode>).getOrElse(NodeSeq.Empty)}
        <sfa:City>${address.addressLine3}</sfa:City>
      </sfa:AddressFix>
      <sfa:AddressFree>${address.wholeAddress}</sfa:AddressFree>
    </sfa:Address>.withPrefixRecursive(newPrefix = repId.sfaOrCrsPrefix)
  }



  def toOrganisationPartyType(organisation: OrganisationType): Elem = ???


}
