package utils

import base.SpecBase
import models.NumberType.Iban
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.AccountType.{CrsAccount, FatcaAccount}
import models.manual.IndividualType.{ControllingPerson, CrsIndividualAccountHolder, SubstantialOwner}
import models.manual.OrgPartyType.{CrsAccountHolder, FatcaAccountHolder, ReportingFIWithFiler, Sponsor}
import models.manual.account.PaymentType.CRSDividends
import models.manual.account.WhatAccountType.Depository
import models.manual.account.{AccountNumber, AccountPaymentsAmount}
import models.manual.accountHolders.*
import models.manual.accountHolders.EquityInterestType.{BeneficiaryEquivalent, SettlorOfTrust}
import models.manual.accountHolders.OrganisationCrs.PassiveWithMultiplePersons
import models.manual.accountHolders.OrganisationFatca.OwnerDocumented
import models.manual.accountHolders.SelfCertification.{NotReported, Yes}
import models.manual.cpso.ControllingPersonType.{OwnershipOfLegalPerson, SeniorManagingOfficial}
import models.manual.filercategory.WhatTypeOfFiler.Registered
import models.manual.filercategory.WhatTypeOfFilerIsSponsor.DirectNonFinancial
import models.manual.*
import models.response.Country
import models.{AccountBalance, AddressNonUk, Currency, ReportId, UkAddress}
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.should.Matchers.{should, shouldEqual}
import utils.manual.XmlMarshaller

import java.time.LocalDate
import scala.language.postfixOps
import scala.xml.{Elem, NodeSeq}

class XmlMarshallerSpec extends SpecBase with PrivateMethodTester{
  val nonUkAddress = AddressNonUk(addressLine1 = "Middle of nowhere",
    addressLine2 = None,
    addressLine3 = "bad place",
    addressLine4 = None,
    postcode = Some("Hell22"),
    country = "HE")
  val residency: TaxResidentInformation = TaxResidentInformation(Country("GB", "United Kingdom"), taxIdentifierNumber= Some("12345"))
  val ukAddress = UkAddress(addressLine1 = "line1", addressLine2 = None, city = "Bradford", county = None, postcode = "D17", country = "UK")
  val crsIndividualAccountHolder = CrsIndividualAccountHolder(
    individualName = IndividualName("James", "Bond"),
    dateOfBirth = Some(IndividualDateOfBirth(LocalDate.of(1999, 12, 12))),
    placeOfBirth = Some(IndividualPlaceOfBirth(Some("London"), Some("Greater London"), "GB")),
    address = ukAddress,
    residency = Seq.empty,
    reportId = reportId,
    interestType = Some(SettlorOfTrust),
    selfCert = NotReported
  )
  val fatcaAh = FatcaAccountHolder(
    name = "FATCA Org",
    address = nonUkAddress,
    residency = Seq(residency),
    reportId = reportId.copy(regime = FATCA),
    accountHolderType = OwnerDocumented
  )
  val selfCert = models.manual.cpso.CpsoSelfCertification.NotReported
  val controllingPerson = ControllingPerson(
    individualName = models.manual.cpso.IndividualName("James", "Bond"),
    dateOfBirth = Some(IndividualDateOfBirth(LocalDate.of(1999, 12, 12))),
    placeOfBirth = Some(models.manual.cpso.IndividualPlaceOfBirth(Some("London"), Some("Greater London"), "GB")),
    address = ukAddress,
    residency = Seq.empty,
    reportId = reportId,
    cpType = OwnershipOfLegalPerson,
    selfCert = selfCert
  )
  val substantialOwner = SubstantialOwner(
    individualName = models.manual.cpso.IndividualName("James", "Bond"),
    dateOfBirth = Some(IndividualDateOfBirth(LocalDate.of(1999, 12, 12))),
    placeOfBirth = Some(models.manual.cpso.IndividualPlaceOfBirth(Some("London"), Some("Greater London"), "GB")),
    address = ukAddress,
    residency = Seq.empty,
    reportId = reportId.copy(regime = FATCA),
  )
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
        val residency = TaxResidentInformation(Country("USA", "United Kingdom"), taxIdentifierNumber = Some("1111"))
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate taxIdToXml(residency, reportId)
        result.size mustEqual 2
        result.head.prefix mustEqual "crs"
        result.last.label mustEqual "IN"
        (result.last \ "@issuedBy").text.trim mustEqual "USA"
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
        (result \ "CountryCode").text.trim mustEqual "UK"
        (result \ "CountryCode").head.prefix mustEqual "cfc"
        (result \ "AddressFree").text.trim mustEqual ukAddress.addressFreeFormat
        (result \ "AddressFree").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "Street").text.trim mustEqual "line1"
        (result \ "AddressFix" \ "Street").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "DistrictName") mustBe empty
        (result \ "AddressFix" \ "PostCode").text.trim mustEqual "D17"
        (result \ "AddressFix" \ "PostCode").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "City").text.trim mustEqual "Bradford"
        (result \ "AddressFix" \ "City").head.prefix mustEqual "cfc"
      }

      "must create a valid AddressFix with the correct prefixes for FATCA" in {
        val address = ukAddress.copy(county = Some("Yorkshire"))
        val reportId = ReportId(FATCA, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate ukAddressToAddressFix(address, reportId)
        result.head.prefix mustEqual "sfa"
        (result \ "CountryCode").text.trim mustEqual "UK"
        (result \ "CountryCode").head.prefix mustEqual "sfa"
        (result \ "AddressFree").text.trim mustEqual address.addressFreeFormat
        (result \ "AddressFix" \ "Street").text.trim mustEqual "line1"
        (result \ "AddressFix" \ "Street").head.prefix mustEqual "sfa"
        (result \ "AddressFix" \ "DistrictName").text.trim mustEqual "Yorkshire"
        (result \ "AddressFix" \ "BuildingIdentifier") mustBe empty
        (result \ "AddressFix" \ "SuiteIdentifier") mustBe empty
        (result \ "AddressFix" \ "FloorIdentifier") mustBe empty
        (result \ "AddressFix" \ "PoB") mustBe empty
        (result \ "AddressFix" \ "PostCode").text.trim mustEqual "D17"
        (result \ "AddressFix" \ "City").text.trim mustEqual "Bradford"
      }
    }


    "nonUkAddressToAddressFix" - {
      val nonUkAddressToAddressFix = PrivateMethod[NodeSeq](Symbol("nonUkAddressToAddressFix"))
      "must create a valid AddressFix with the correct prefixes for CRS" in {
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate nonUkAddressToAddressFix(nonUkAddress, reportId)
        result.head.prefix mustEqual "crs"
        (result \ "CountryCode").text.trim mustEqual "HE"
        (result \ "CountryCode").head.prefix mustEqual "cfc"
        (result \ "AddressFree").text.trim mustEqual nonUkAddress.addressFreeFormat
        (result \ "AddressFree").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "Street").text.trim mustEqual "Middle of nowhere"
        (result \ "AddressFix" \ "Street").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "DistrictName") mustBe empty
        (result \ "AddressFix" \ "BuildingIdentifier") mustBe empty
        (result \ "AddressFix" \ "SuiteIdentifier") mustBe empty
        (result \ "AddressFix" \ "FloorIdentifier") mustBe empty
        (result \ "AddressFix" \ "PoB") mustBe empty
        (result \ "AddressFix" \ "PostCode").text.trim mustEqual "Hell22"
        (result \ "AddressFix" \ "PostCode").head.prefix mustEqual "cfc"
        (result \ "AddressFix" \ "City").text.trim mustEqual "bad place"
        (result \ "AddressFix" \ "City").head.prefix mustEqual "cfc"
      }

      "must create a valid AddressFix with the correct prefixes for FATCA" in {
        val address = nonUkAddress.copy(postcode = None)
        val reportId = ReportId(FATCA, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate nonUkAddressToAddressFix(address, reportId)
        result.head.prefix mustEqual "sfa"
        (result \ "CountryCode").text.trim mustEqual "HE"
        (result \ "AddressFree").text.trim mustEqual address.addressFreeFormat
        (result \ "AddressFix" \ "Street").text.trim mustEqual "Middle of nowhere"
        (result \ "AddressFix" \ "DistrictName") mustBe empty
        (result \ "AddressFix" \ "PostCode") mustBe empty
        (result \ "AddressFix" \ "City").text.trim mustEqual "bad place"
      }
    }

    "accountHolderToXml"- {
      val accountHolderToXml = PrivateMethod[NodeSeq](Symbol("accountHolderToXml"))
      "must create account holder type with the correct prefix for fatca" in {
        val reportId = ReportId(FATCA, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate accountHolderToXml("someType", reportId)
        result.head.prefix mustEqual "ftc"
        result.head.text.trim mustEqual "someType"
      }
      "must create account holder type with the correct prefix for CRS" in {
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate accountHolderToXml("someType", reportId)
        result.head.prefix mustEqual "crs"
        result.head.text.trim mustEqual "someType"
      }
    }
    "filerCategoryToXml" - {
      "should create filer category" in {
        val filerCategoryToXml = PrivateMethod[NodeSeq](Symbol("filerCategoryToXml"))
        val result = XmlMarshaller invokePrivate filerCategoryToXml("filer")
        result.head.prefix mustEqual "ftc"
        result.head.text.trim mustEqual "filer"
      }

    }
    "crsAccountHolderToXml" - {
      val crsAccountHolderToXml = PrivateMethod[NodeSeq](Symbol("crsAccountHolderToXml"))
      "must create account holder type when Equity type is provided" in {
        val result = XmlMarshaller invokePrivate crsAccountHolderToXml(Yes, Some(SettlorOfTrust))
        result.head.prefix mustEqual "crs"
        result.size mustEqual 2
        result.head.text.trim mustEqual Yes.toString
        result.last.text.trim mustEqual SettlorOfTrust.xmlValue
      }
      "must create account holder type when Equity type is NOT provided" in {
        val result = XmlMarshaller invokePrivate crsAccountHolderToXml(NotReported, None)
        result.head.prefix mustEqual "crs"
        result.size mustEqual 1
        result.head.text.trim mustEqual NotReported.toString
      }
    }

    "createNameType" - {
      val createNameType = PrivateMethod[NodeSeq](Symbol("createNameType"))
      "must create individual name with appropriate prefix for fatca" in {
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate createNameType("firstName", "lastName", reportId)
        result.head.prefix mustEqual "crs"
        result.head.text.trim mustEqual "firstName"
        result.last.text.trim mustEqual "lastName"
      }
      "must create individual name with appropriate prefix for crs" in {
        val reportId = ReportId(FATCA, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate createNameType("firstName", "lastName", reportId)
        result.head.prefix mustEqual "sfa"
        result.head.text.trim mustEqual "firstName"
        result.last.text.trim mustEqual "lastName"
      }
    }

    "createBirthInfo" - {
      val createBirthInfo = PrivateMethod[NodeSeq](Symbol("createBirthInfo"))
      "must create an empty NodeSeq if none have been provided" in {
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate createBirthInfo(reportId,None,None,None, None)
        result.size mustEqual 0
      }
      "must BirthInfo with only birthDate" in {
        val reportId = ReportId(FATCA, 2020, None, "fi12")
        val date = LocalDate.now()
        val result = XmlMarshaller invokePrivate createBirthInfo(reportId,Some(date), None,None, None)
        println(result.toString)
        result.size mustEqual 1
        result.head.prefix mustEqual "sfa"
        (result \ "BirthDate").text.trim mustEqual date.toString
        (result \ "CitySubEntity") mustBe empty
        (result \ "City") mustBe empty
        (result \ "CountryInfo") mustBe empty
      }
      "must BirthInfo with only country" in {
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate createBirthInfo(reportId,None,Some(CountryBirthInfo("uk")),None, None)
        result.size mustEqual 1
        result.head.prefix mustEqual "crs"
        (result \ "CountryInfo" \ "CountryCode").text.trim mustEqual "uk"
        (result \ "CitySubEntity") mustBe empty
        (result \ "City") mustBe empty
        (result \ "BirthDate") mustBe empty
      }
      "must BirthInfo with only city" in {
        val reportId = ReportId(CRS, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate createBirthInfo(reportId, None, None, Some(CityBirthInfo("Newcastle")), None)
        result.size mustEqual 1
        result.head.prefix mustEqual "crs"
        (result \ "City").text.trim mustEqual "Newcastle"
        (result \ "CitySubEntity") mustBe empty
        (result \ "BirthDate") mustBe empty
        (result \ "CountryInfo") mustBe empty
      }
      "must BirthInfo with only region" in {
        val reportId = ReportId(FATCA, 2020, None, "fi12")
        val result = XmlMarshaller invokePrivate createBirthInfo(reportId, None, None, None, Some(RegionBirthInfo("north east")))
        result.size mustEqual 1
        result.head.prefix mustEqual "sfa"
        (result \ "CitySubEntity").text.trim mustEqual "north east"
        (result \ "BirthDate") mustBe empty
        (result \ "City") mustBe empty
        (result \ "CountryInfo") mustBe empty
      }
    }
    "createCPInfo" - {
      val createCPInfo = PrivateMethod[NodeSeq](Symbol("createCPInfo"))
      "must create controlling person type and self cert when called" in {
        val yes = models.manual.cpso.CpsoSelfCertification.Yes
        val result = XmlMarshaller invokePrivate createCPInfo(yes, SeniorManagingOfficial)
        result.head.prefix mustEqual "crs"
        result.size mustEqual 2
        result.last.text.trim mustEqual yes.toString
        result.head.text.trim mustEqual SeniorManagingOfficial.xmlValue
      }
    }

    "toOrganisationPartyType" - {

      "must marshal a CrsAccountHolder with EquityInterestType" in {
        val crsAh = CrsAccountHolder(
          name = "CRS Org Ltd",
          address = ukAddress,
          residency = Seq(residency),
          reportId = reportId,
          accountHolderType = PassiveWithMultiplePersons,
          interestType = Some(BeneficiaryEquivalent),
          selfCert = Yes
        )

        val result = XmlMarshaller.toOrganisationPartyType(crsAh)

        result.prefix mustEqual "crs"
        result.label mustEqual "AccountHolder"
        (result \ "Organisation" \"ResCountryCode").text.trim mustEqual "GB"
        (result \ "Organisation" \"IN").text.trim mustEqual "12345"
        (result \ "Organisation" \ "Name").text.trim mustEqual "CRS Org Ltd"
        (result \ "Organisation" \"Name").head.prefix mustEqual "crs"
        (result \ "Organisation" \ "Address").head.prefix mustEqual "crs"
        (result \ "AcctHolderType").text.trim mustEqual PassiveWithMultiplePersons.toString
        (result \ "AcctHolderType").head.prefix mustEqual "crs"
        (result \ "SelfCert").text.trim mustEqual Yes.toString
        (result \ "SelfCert").head.prefix mustEqual "crs"
        (result \ "EquityInterestType").text.trim mustEqual BeneficiaryEquivalent.toString
        (result \ "EquityInterestType").head.prefix mustEqual "crs"
      }

      "must marshal a CrsAccountHolder without EquityInterestType" in {
        val crsAh = CrsAccountHolder(
          name = "CRS Org No Interest Ltd",
          address = nonUkAddress,
          residency = Seq(residency),
          reportId = reportId,
          accountHolderType = PassiveWithMultiplePersons,
          interestType = None,
          selfCert = Yes
        )

        val result = XmlMarshaller.toOrganisationPartyType(crsAh)

        result.prefix mustEqual "crs"
        result.label mustEqual "AccountHolder"
        (result \ "Organisation" \ "Name").text.trim mustEqual "CRS Org No Interest Ltd"
        (result \ "AcctHolderType").text.trim mustEqual PassiveWithMultiplePersons.toString
        (result \ "SelfCert").text.trim mustEqual Yes.toString
        (result \ "EquityInterestType") mustBe empty
      }

      "must marshal a FatcaAccountHolder" in {

        val result = XmlMarshaller.toOrganisationPartyType(fatcaAh)
        println(result)
        result.prefix mustEqual "ftc"
        result.label mustEqual "AccountHolder"
        (result \ "Organisation" \ "ResCountryCode").text.trim mustEqual "GB"
        (result \ "Organisation" \ "TIN").text.trim mustEqual "12345"
        (result \ "Organisation" \ "Name").text.trim mustEqual "FATCA Org"
        (result \ "Organisation" \ "Name").head.prefix mustEqual "sfa"
        (result \ "Organisation" \"Address").head.prefix mustEqual "sfa"
        (result \ "AcctHolderType").text.trim mustEqual OwnerDocumented.toString
        (result \ "AcctHolderType").head.prefix mustEqual "ftc"
        (result \ "SelfCert") mustBe empty
        (result \ "FilerCategory") mustBe empty
      }

      "create a Sponsor" in {
        val sponsor = Sponsor(
          name = "FATCA Org",
          address = nonUkAddress,
          residency = Seq(residency),
          reportId = reportId.copy(regime = FATCA),
          filerCategory = DirectNonFinancial
        )

        val result = XmlMarshaller.toOrganisationPartyType(sponsor)
        result.prefix mustEqual "ftc"
        result.label mustEqual "Sponsor"
        (result \ "AcctHolderType") mustBe empty
        result.head.prefix mustEqual "ftc"
        (result \ "Organisation" \ "Address" \ "AddressFix").head.prefix mustEqual "sfa"
        (result \ "SelfCert") mustBe empty
        (result \ "FilerCategory").text.trim mustEqual DirectNonFinancial.xmlValue
        (result \ "FilerCategory").head.prefix mustEqual "ftc"
      }

      "create a ReportingFiWithFiler" in {
        val sponsor = ReportingFIWithFiler(
          name = "FATCA Org",
          address = nonUkAddress,
          residency = Seq(residency),
          reportId = reportId.copy(regime = FATCA),
          filerCategory = Registered
        )

        val result = XmlMarshaller.toOrganisationPartyType(sponsor)
        result.prefix mustEqual "ftc"
        result.label mustEqual "ReportingFI"
        (result \ "AcctHolderType") mustBe empty
        result.head.prefix mustEqual "ftc"
        (result \ "Organisation" \ "Address" \ "AddressFix").head.prefix mustEqual "sfa"
        (result \ "SelfCert") mustBe empty
        (result \ "FilerCategory").text.trim mustEqual Registered.xmlValue
        (result \ "FilerCategory").head.prefix mustEqual "ftc"
      }
    }
    "toIndividualType" - {
    "CrsIndividualAccountHolder" in {

      val result = XmlMarshaller.toIndividualType(crsIndividualAccountHolder)
      result.prefix mustEqual "crs"
      result.label mustEqual "AccountHolder"
      (result \ "AcctHolderType") mustBe empty
      (result \ "SelfCert").text.trim mustBe NotReported.toString
      (result \ "EquityInterestType").text.trim mustBe SettlorOfTrust.xmlValue
      (result \ "Individual" \ "Name" \ "FirstName").text.trim mustBe "James"
      (result \ "Individual" \ "Name" \ "LastName").text.trim mustBe "Bond"
      (result \ "Individual" \ "BirthInfo" \ "BirthDate").text.trim mustBe LocalDate.of(1999,12,12).toString
      (result \ "Individual" \ "BirthInfo" \ "City").text.trim mustBe "London"
      (result \ "Individual" \ "BirthInfo" \ "CitySubEntity").text.trim mustBe "Greater London"
      (result \ "Individual" \ "BirthInfo" \ "CountryInfo" \ "CountryCode").text.trim mustBe "GB"
    }

      "controllingPerson" in {
        val result = XmlMarshaller.toIndividualType(controllingPerson)

        result.prefix mustEqual "crs"
        result.label mustEqual "ControllingPerson"
        (result \ "SelfCert").text.trim mustBe selfCert.toString
        (result \ "CtrlgPersonType").text.trim mustBe OwnershipOfLegalPerson.xmlValue
        (result \ "Individual" \ "Name" \ "FirstName").text.trim mustBe "James"
        (result \ "Individual" \ "Name" \ "LastName").text.trim mustBe "Bond"
        (result \ "Individual" \ "BirthInfo" \ "BirthDate").text.trim mustBe LocalDate.of(1999,12,12).toString
        (result \ "Individual" \ "BirthInfo" \ "City").text.trim mustBe "London"
        (result \ "Individual" \ "BirthInfo" \ "CitySubEntity").text.trim mustBe "Greater London"
        (result \ "Individual" \ "BirthInfo" \ "CountryInfo" \ "CountryCode").text.trim mustBe "GB"
      }

      "SubstantialOwner" in {
        val result = XmlMarshaller.toIndividualType(substantialOwner)
        result.prefix mustEqual "ftc"
        result.label mustEqual "SubstantialOwner"
        (result \ "SelfCert") mustBe empty
        (result \ "CtrlgPersonType") mustBe empty
        (result \ "EquityInterestType") mustBe empty
        (result \ "Individual" \ "Name" \ "FirstName").text.trim mustBe "James"
        (result \ "Individual" \ "Name" \ "FirstName").head.prefix mustBe "sfa"
        (result \ "Individual" \ "Name" \ "LastName").head.prefix mustBe "sfa"
        (result \ "Individual" \ "Name" \ "LastName").text.trim mustBe "Bond"
        (result \ "Individual" \ "BirthInfo" \ "BirthDate").text.trim mustBe LocalDate.of(1999, 12, 12).toString
        (result \ "Individual" \ "BirthInfo" \ "City").text.trim mustBe "London"
        (result \ "Individual" \ "BirthInfo" \ "CitySubEntity").text.trim mustBe "Greater London"
        (result \ "Individual" \ "BirthInfo" \ "CountryInfo" \ "CountryCode").text.trim mustBe "GB"
      }

    }

    "toAccountReport" -{
      val payments = List(Payment(CRSDividends, AccountPaymentsAmount(Currency.GBP, "400")),
        Payment(CRSDividends, AccountPaymentsAmount(Currency.GBP, "200")))
      val accountBalance = AccountBalance(Currency.GBP, "200")
      val repId = reportId.copy(regime = FATCA)
      "wrap fatca account with accountHolder and substancial owner into a reportingGroup" in {
        val fatcaAccountHolder = XmlMarshaller.toOrganisationPartyType(fatcaAh)
        val so = XmlMarshaller.toIndividualType(substantialOwner)
        val fatcAccount = FatcaAccount(
          numberType = Some(Iban),
          accountIdentifier= AccountNumber(id= "account1"),
          accountBalance = accountBalance,
          isClosed = true,
          payments =  payments
        )
        val result = XmlMarshaller.toAccountReport(fatcAccount,repId)(fatcaAccountHolder ++ so)
        result.prefix mustEqual "ftc"
        result.label mustEqual "ReportingGroup"
        (result \ "AccountReport" \ "Payment").size mustEqual 2
        (result \ "AccountReport" \ "AccountClosed").head.text.trim mustEqual "true"
        (result \ "AccountReport" \ "AccountNumber").head.text.trim mustEqual "account1"
        (result \ "AccountReport" \ "AccountNumber").head \@ "AcctNumberType" mustEqual Iban.xmlValue
        (result \ "AccountReport" \ "Payment" \ "Type").head.text.trim mustEqual CRSDividends.toString
        (result \ "AccountReport" \ "Payment" \ "PaymentAmnt").map(_ \@ "currCode") must contain only Currency.GBP.code
        (result \ "AccountReport" \ "Payment" \ "PaymentAmnt").map(_.text.trim.toInt).sum mustEqual 600
        (result \ "AccountReport" \ "AccountBalance").head.text.trim mustEqual "0.00"
        (result \ "AccountReport" \ "AccountBalance").head \@ "currCode" mustEqual Currency.GBP.code
        (result \ "AccountReport" \ "SubstantialOwner").size mustEqual 1
        (result \ "AccountReport" \ "SubstantialOwner").head.prefix mustEqual "ftc"
        (result \ "AccountReport" \ "AccountHolder").head.prefix mustEqual "ftc"
        (result \ "AccountReport" \ "AccountHolder").size mustEqual 1
      }


      "wrap crs account with accountHolder and controlling person into a reportingGroup" in {
        val crsAccountHolder = XmlMarshaller.toIndividualType(crsIndividualAccountHolder)
        val cp = XmlMarshaller.toIndividualType(controllingPerson)
        val crsAccount = CrsAccount(
          numberType = Some(Iban),
          accountIdentifier = AccountNumber(id = "account1"),
          accountBalance = accountBalance,
          isClosed = false,
          isUndocumented = true,
          isDormant = false,
          wasOpenInRP = models.manual.account.WasAccountOpen.Yes,
          howManyJointHolders= Some(2),
          accountType = Depository,
          payments = payments :+ Payment(CRSDividends, AccountPaymentsAmount(Currency.GBP, "100"))
        )
        val result = XmlMarshaller.toAccountReport(crsAccount, repId.copy(regime = CRS))(crsAccountHolder ++ cp)
        println(result.toString)
        result.prefix mustEqual "crs"
        result.label mustEqual "ReportingGroup"
        (result \ "AccountReport" \ "Payment").size mustEqual 3
        (result \ "AccountReport" \ "AccountNumber").head  \@ "ClosedAccount" mustEqual "false"
        (result \ "AccountReport" \ "AccountType").head.text.trim mustEqual Depository.toString
        (result \ "AccountReport" \ "DDProcedure").head.text.trim mustEqual models.manual.account.WasAccountOpen.Yes.xmlValue
        (result \ "AccountReport" \ "AccountNumber").head  \@ "UndocumentedAccount" mustEqual "true"
        (result \ "AccountReport" \ "AccountNumber").head  \@ "DormantAccount" mustEqual "false"
        (result \ "AccountReport" \ "AccountNumber").head.text.trim mustEqual "account1"
        (result \ "AccountReport" \ "AccountNumber").head \@ "AcctNumberType" mustEqual Iban.xmlValue
        (result \ "AccountReport" \ "Payment" \ "Type").head.text.trim mustEqual CRSDividends.toString
        (result \ "AccountReport" \ "Payment" \ "PaymentAmnt").map(_ \@ "currCode") must contain only Currency.GBP.code
        (result \ "AccountReport" \ "Payment" \ "PaymentAmnt").map(_.text.trim.toInt).sum mustEqual 700
        (result \ "AccountReport" \ "AccountBalance").head.text.trim mustEqual "200"
        (result \ "AccountReport" \ "AccountBalance").head \@ "currCode" mustEqual Currency.GBP.code
        (result \ "AccountReport" \ "ControllingPerson").size mustEqual 1
        (result \ "AccountReport" \ "ControllingPerson").head.prefix mustEqual "crs"
        (result \ "AccountReport" \ "AccountHolder").head.prefix mustEqual "crs"
        (result \ "AccountReport" \ "AccountHolder" \ "Individual").size mustEqual 1
        (result \ "AccountReport" \ "AccountHolder").size mustEqual 1
      }
    }


  }

}
