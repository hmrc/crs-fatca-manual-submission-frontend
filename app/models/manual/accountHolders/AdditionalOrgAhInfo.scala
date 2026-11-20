package models.manual.accountHolders

import models.manual.accountHolders.AccountHolderTypes.{CrsReportablePerson, PassiveNonFinancialEntityThatIsCRSReportable, PassiveNonFinancialWithOneOrMoreControllingPerson}

case class AdditionalOrgAhInfo(accountHolderType: AccountHolderTypes, hasInterestType:Boolean,  interestType: Option[EquityInterestType], selfCert: Option[SelfCertification] ) {
  private val isCrsAccountType = if accountHolderType == PassiveNonFinancialWithOneOrMoreControllingPerson || accountHolderType == CrsReportablePerson || accountHolderType == PassiveNonFinancialEntityThatIsCRSReportable then true else false
  val hasIncorrectCrsData: Boolean = if !isCrsAccountType || selfCert.isEmpty || (hasInterestType & interestType.isEmpty) then true else false
  val hasIncorrectFatcaData: Boolean = if isCrsAccountType || interestType.isDefined || selfCert.isDefined then true else false
}
