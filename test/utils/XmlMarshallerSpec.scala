package utils

import base.SpecBase
import models.SubmissionsConstants.{CRS, FATCA}
import models.{AddressNonUk, ReportId, TaxResidentInformation, UkAddress}
import models.response.Country
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.should.Matchers.{should, shouldEqual}
import utils.manual.XmlMarshaller

import scala.language.postfixOps
import scala.xml.{Elem, NodeSeq}

class XmlMarshallerSpec extends SpecBase with PrivateMethodTester{
  val nonUkAddress = AddressNonUk(addressLine1 = "Middle of nowhere",
    addressLine2 = None,
    addressLine3 = "bad place",
    addressLine4 = None,
    postcode = Some("Hell22"),
    country = "HE")
  val residency = TaxResidentInformation(Country("GB", "United Kingdom"), taxIdentifierNumbers = Some("12345"))
  val ukAddress = UkAddress(addressLine1 = "line1", addressLine2 = None, city = "Bradford", county = None, postcode = "D17", country = "UK")

  "XmlMarshaller" - {

    "withPrefixRecursive" in {
      val withPrefixRecursive = PrivateMethod[Elem](Symbol("withPrefixRecursive"))
      val sample =
      <sfa:Address>
      <sfa:CountryCode>$
        {"GB"}
      </sfa:CountryCode>
      </sfa:Address>

      val result = XmlMarshaller invokePrivate withPrefixRecursive(sample, "somePrefix")
      result.prefix mustEqual "somePrefix"
    }


    "taxIdToXml" - {
      "create a TIN with sfa prefix for FATCA" in {
        val taxIdToXml = PrivateMethod[NodeSeq](Symbol("taxIdToXml"))
        val reportId = ReportId(FATCA, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate taxIdToXml(residency, reportId)
        println(s"result looks like ${result.toString}")
        result.size mustEqual 2
        result.head.prefix mustEqual "sfa"
        result.last.label mustEqual "TIN"
        (result.last \ "@issuedBy").text mustEqual "GB"
        result.last.text.trim mustEqual "12345"
      }

      "create a In with crs prefix for FATCA" in {
        val taxIdToXml = PrivateMethod[NodeSeq](Symbol("taxIdToXml"))
        val residency = TaxResidentInformation(Country("USA", "United Kingdom"), taxIdentifierNumbers = Some("1111"))
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate taxIdToXml(residency, reportId)
        result.size mustEqual 2
        result.head.prefix mustEqual "crs"
        result.last.label mustEqual "IN"
        (result.last \ "@issuedBy").text mustEqual "USA"
        result.last.text.trim mustEqual "1111"
      }
      }

    "ukAddressToAddressFix" - {
      val ukAddressToAddressFix = PrivateMethod[NodeSeq](Symbol("ukAddressToAddressFix"))
      "must create a valid AddressFix with the correct prefixes for CRS" in {
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate ukAddressToAddressFix(ukAddress, reportId)
        println(result.toString)
        result.head.prefix mustEqual "crs"
        (result \ "AddressFix").head.prefix mustEqual "cfc"
        (result \ "CountryCode").text mustEqual "UK"
        (result \ "CountryCode").head.prefix mustEqual "cfc"
        (result \ "AddressFree").text mustEqual ukAddress.addressFreeFormat
        (result \ "AddressFree").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "Street").text mustEqual "line1"
        (result \ "AddressFix" \ "Street").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "DistrictName") mustBe empty
        (result \ "AddressFix" \ "PostCode").text mustEqual "D17"
        (result \ "AddressFix" \ "PostCode").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "City").text mustEqual "Bradford"
        (result \ "AddressFix" \ "City").head.prefix mustEqual "cfc"
      }

      "must create a valid AddressFix with the correct prefixes for FATCA" in {
        val address = ukAddress.copy(county = Some("Yorkshire"))
        val reportId = ReportId(FATCA, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate ukAddressToAddressFix(address, reportId)
        result.head.prefix mustEqual "sfa"
        (result \ "CountryCode").text mustEqual "UK"
        (result \ "CountryCode").head.prefix mustEqual "sfa"
        (result \ "AddressFree").text mustEqual address.addressFreeFormat
        (result \ "AddressFix" \ "Street").text mustEqual "line1"
        (result \ "AddressFix" \ "Street").head.prefix mustEqual "sfa"
        (result \ "AddressFix" \ "DistrictName").text mustEqual "Yorkshire"
        (result \ "AddressFix" \ "BuildingIdentifier") mustBe empty
        (result \ "AddressFix" \ "SuiteIdentifier") mustBe empty
        (result \ "AddressFix" \ "FloorIdentifier") mustBe empty
        (result \ "AddressFix" \ "PoB") mustBe empty
        (result \ "AddressFix" \ "PostCode").text mustEqual "D17"
        (result \ "AddressFix" \ "City").text mustEqual "Bradford"
      }
    }


    "nonUkAddressToAddressFix" - {
      val nonUkAddressToAddressFix = PrivateMethod[NodeSeq](Symbol("nonUkAddressToAddressFix"))
      "must create a valid AddressFix with the correct prefixes for CRS" in {
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate nonUkAddressToAddressFix(nonUkAddress, reportId)
        result.head.prefix mustEqual "crs"
        (result \ "CountryCode").text mustEqual "HE"
        (result \ "CountryCode").head.prefix mustEqual "cfc"
        (result \ "AddressFree").text mustEqual nonUkAddress.addressFreeFormat
        (result \ "AddressFree").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "Street").text mustEqual "Middle of nowhere"
        (result \ "AddressFix" \ "Street").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "DistrictName") mustBe empty
        (result \ "AddressFix" \ "BuildingIdentifier") mustBe empty
        (result \ "AddressFix" \ "SuiteIdentifier") mustBe empty
        (result \ "AddressFix" \ "FloorIdentifier") mustBe empty
        (result \ "AddressFix" \ "PoB") mustBe empty
        (result \ "AddressFix" \ "PostCode").text mustEqual "Hell22"
        (result \ "AddressFix" \ "PostCode").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "City").text mustEqual "bad place"
        (result \ "AddressFix" \ "City").head.prefix mustEqual "cfc"
      }

      "must create a valid AddressFix with the correct prefixes for FATCA" in {
        val address = nonUkAddress.copy(postcode = None)
        val reportId = ReportId(FATCA, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate nonUkAddressToAddressFix(address, reportId)
        result.head.prefix mustEqual "sfa"
        (result \ "CountryCode").text mustEqual "HE"
        (result \ "AddressFree").text mustEqual address.addressFreeFormat
        (result \ "AddressFix" \ "Street").text mustEqual "Middle of nowhere"
        (result \ "AddressFix" \ "DistrictName") mustBe empty
        (result \ "AddressFix" \ "PostCode") mustBe empty
        (result \ "AddressFix" \ "City").text mustEqual "bad place"
      }
    }

    "toOrganisationPartyType" - {
//      val baseOrg =
    }


    }



}
