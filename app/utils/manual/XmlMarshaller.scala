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

import models.manual.AccountType.{CrsAccount, FatcaAccount}
import models.manual.IndividualType.{ControllingPerson, CrsIndividualAccountHolder}
import models.manual.OrgPartyType.{CrsAccountHolder, FatcaAccountHolder, ReportingFIWithFiler, Sponsor}
import models.manual.account.PaymentType
import models.manual.accountHolders.{EquityInterestType, SelfCertification}
import models.manual.cpso.{ControllingPersonType, CpsoSelfCertification}
import models.manual.{AccountType, CityBirthInfo, CountryBirthInfo, IndividualType, OrgPartyType, Payment, RegionBirthInfo, TaxResidentInformation}
import models.{AddressNonUk, ReportId, UkAddress}

import java.time.LocalDate
import scala.xml.*

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
      residency.taxIdentifierNumber
        .map(
          taxId =>
            <sfa:TIN issuedBy={residency.country.code}>
          {taxId}
        </sfa:TIN>
              .copy(prefix = repId.getChildPrefix(true), label = repId.tinLabel)
        )
        .getOrElse(NodeSeq.Empty)
    }

  private def wrapElement(elementName: String, reportId: ReportId)(children: NodeSeq): Elem =
    Elem(
      prefix = reportId.parentPrefix,
      label = elementName,
      attributes = Null,
      scope = TopScope,
      minimizeEmpty = false,
      child = children*
    )

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
    </sfa:Address>.withPrefixRecursive(newPrefix = repId.getChildPrefix(false)).copy(prefix = repId.getChildPrefix(true))

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
    </sfa:Address>.withPrefixRecursive(newPrefix = repId.getChildPrefix(false)).copy(prefix = repId.getChildPrefix(true))

  private def filerCategoryToXml(filer: String): NodeSeq =
    <ftc:FilerCategory>{filer}</ftc:FilerCategory>

  private def crsAccountHolderToXml(selfCertification: SelfCertification, interestType: Option[EquityInterestType]): NodeSeq =
    <crs:SelfCert>
        {selfCertification.toString}
      </crs:SelfCert> ++
      interestType
        .map(
          intType => <crs:EquityInterestType>
          {intType.xmlValue}
        </crs:EquityInterestType>
        )
        .getOrElse(NodeSeq.Empty)

  private def accountHolderToXml(accountHolderType: String, reportId: ReportId): NodeSeq =
    <ftc:AcctHolderType>
      {accountHolderType}
    </ftc:AcctHolderType>.copy(prefix = reportId.parentPrefix)

  def toOrganisationPartyType(organisation: OrgPartyType): Elem = {
    // TODO docSpec logic
    val addressFix = organisation.address match {
      case uk: UkAddress        => ukAddressToAddressFix(uk, organisation.reportId)
      case abroad: AddressNonUk => nonUkAddressToAddressFix(abroad, organisation.reportId)
    }
    val orgName         = <sfa:Name>{organisation.name}</sfa:Name>.copy(prefix = organisation.reportId.getChildPrefix(true))
    val taxIds: NodeSeq = NodeSeq.fromSeq(organisation.residency.flatMap(taxIdToXml(_, organisation.reportId)))
    val additionalInfo = organisation match {
      case ah: CrsAccountHolder     => accountHolderToXml(ah.accountHolderType.toString, ah.reportId) ++ crsAccountHolderToXml(ah.selfCert, ah.interestType)
      case ah: FatcaAccountHolder   => accountHolderToXml(ah.accountHolderType.toString, ah.reportId)
      case fi: ReportingFIWithFiler => filerCategoryToXml(fi.filerCategory.xmlValue)
      case sponsor: Sponsor         => filerCategoryToXml(sponsor.filerCategory.xmlValue)
      case _                        => NodeSeq.Empty
    }
    val baseOrg: NodeSeq = wrapElement("Organisation", organisation.reportId)(taxIds ++ orgName ++ addressFix)
    wrapElement(organisation.xmlElementName, organisation.reportId)(baseOrg ++ additionalInfo)
  }

  private def createNameType(firstName: String, lastName: String, reportId: ReportId) =
    <sfa:Name>
      <sfa:FirstName>
        {firstName}
      </sfa:FirstName>
      <sfa:LastName>
        {lastName}
      </sfa:LastName>
    </sfa:Name>.withPrefixRecursive(newPrefix = reportId.getChildPrefix(true))

  private def createBirthInfo(reportId: ReportId,
                              birthDate: Option[LocalDate],
                              country: Option[CountryBirthInfo],
                              city: Option[CityBirthInfo],
                              region: Option[RegionBirthInfo]
  ): NodeSeq =
    if (birthDate.orElse(country).orElse(city).orElse(region).isDefined) {
      <sfa:BirthInfo>
        {
        birthDate
          .map(
            birth => <sfa:BirthDate>
        {birth.toString}
      </sfa:BirthDate>
          )
          .getOrElse(NodeSeq.Empty)
      }{
        city
          .map(
            cityOfBirth => <sfa:City>
        {cityOfBirth.city}
      </sfa:City>
          )
          .getOrElse(NodeSeq.Empty)
      }{
        region
          .map(
            citySubEntity => <sfa:CitySubEntity>
        {citySubEntity.region}
      </sfa:CitySubEntity>
          )
          .getOrElse(NodeSeq.Empty)
      }{
        country
          .map(
            countOfBirth => <sfa:CountryInfo>
        <sfa:CountryCode>
          {countOfBirth.country}
        </sfa:CountryCode>
      </sfa:CountryInfo>
          )
          .getOrElse(NodeSeq.Empty)
      }
      </sfa:BirthInfo>.withPrefixRecursive(newPrefix = reportId.getChildPrefix(true))
    } else NodeSeq.Empty

  private def createCPInfo(selfCert: CpsoSelfCertification, cpType: ControllingPersonType): NodeSeq =
    <crs:CtrlgPersonType>{cpType.xmlValue}</crs:CtrlgPersonType>
      <crs:SelfCert>{selfCert.toString}</crs:SelfCert>

  def toIndividualType(individual: IndividualType): Elem = {
    // TODO docSpec logic
    val addressFix = individual.address match {
      case uk: UkAddress        => ukAddressToAddressFix(uk, individual.reportId)
      case abroad: AddressNonUk => nonUkAddressToAddressFix(abroad, individual.reportId)
    }
    val individualName  = createNameType(individual.firstName, individual.lastName, individual.reportId)
    val birthInfo       = createBirthInfo(individual.reportId, individual.birthDate, individual.country, individual.city, individual.region)
    val taxIds: NodeSeq = NodeSeq.fromSeq(individual.residency.flatMap(taxIdToXml(_, individual.reportId)))
    val additionalInformation = individual match {
      case ind: CrsIndividualAccountHolder => crsAccountHolderToXml(ind.selfCert, ind.interestType)
      case ind: ControllingPerson          => createCPInfo(ind.selfCert, ind.cpType)
      case _                               => NodeSeq.Empty
    }
    val baseIndividual = wrapElement("Individual", individual.reportId)(individualName ++ addressFix ++ taxIds ++ birthInfo)
    wrapElement(individual.xmlElementName, individual.reportId)(baseIndividual ++ additionalInformation)
  }

  private def getAccountData(account: AccountType, reportId: ReportId): NodeSeq =
    account match {
      case ac: CrsAccount   => getCrsAccountNumber(ac, reportId)
      case ac: FatcaAccount => getFatcaAccountNumber(ac, reportId)
    }

  private def createPaymentType(payment: Payment, reportId: ReportId): NodeSeq =
    <ftc:Payment>
      <ftc:Type>{payment.paymentType.toString}</ftc:Type>
      <ftc:PaymentAmnt currCode={payment.amount.currency.code}>
        {payment.amount.amount}</ftc:PaymentAmnt>
    </ftc:Payment>.withPrefixRecursive(reportId.parentPrefix)

  private def getCrsAccountNumber(account: CrsAccount, reportId: ReportId): NodeSeq = {
    val accountNumber = account.numberType
      .map(
        numType =>
          <crs:AccountNumber AcctNumberType={numType.xmlValue} UndocumentedAccount={account.isUndocumented.toString} ClosedAccount={
            account.isClosed.toString
          } DormantAccount={account.isDormant.toString}>
        {account.accountIdentifier.id}</crs:AccountNumber>
      )
      .getOrElse(<crs:AccountNumber UndocumentedAccount={account.isUndocumented.toString} ClosedAccount={account.isClosed.toString} DormantAccount={
        account.isDormant.toString
      }>
        {account.accountIdentifier.id}</crs:AccountNumber>)
    val ddProcedureAndBalance = <crs:DDProcedure>{account.wasOpenInRP.xmlValue}</crs:DDProcedure> ++ <crs:AccountBalance currCode={
      account.balance.currency.code
    }>{account.balance.amount}</crs:AccountBalance>
    val typeAndBalance = <crs:AccountType>{account.accountType}</crs:AccountType> ++ account.payments.flatMap(
      payment => createPaymentType(payment, reportId)
    )
    val maybeJoint = account.howManyJointHolders
      .map(
        holders => <crs:JointAccount><crs:Number>
      {holders}</crs:Number></crs:JointAccount>
      )
      .getOrElse(NodeSeq.Empty)
    accountNumber ++ ddProcedureAndBalance ++ typeAndBalance ++ maybeJoint
  }

  private def getFatcaAccountNumber(ac: FatcaAccount, reportId: ReportId): NodeSeq =
    ac.numberType
      .map(
        numType => <ftc:AccountNumber AcctNumberType={numType.xmlValue}>{ac.accountNumber}</ftc:AccountNumber>
      )
      .getOrElse(<ftc:AccountNumber>{ac.accountNumber}</ftc:AccountNumber>) ++ <ftc:AccountClosed>
      {ac.isClosed}</ftc:AccountClosed>.flatMap(
      _ ++ <ftc:AccountBalance currCode={ac.balance.currency.code}>
      {ac.balance.amount}</ftc:AccountBalance>
        ++ ac.payments.flatMap(
          payment => createPaymentType(payment, reportId)
        )
    )

  def toAccountReport(account: AccountType, reportId: ReportId)(accountHolderAndMaybeSoCp: NodeSeq): Elem = {
    // TODO docSpec logic
    val accountSpecifics = wrapElement(
      "AccountReport",
      reportId
    )(getAccountData(account, reportId) ++ accountHolderAndMaybeSoCp)
    wrapElement("ReportingGroup", reportId)(accountSpecifics)
  }

}
