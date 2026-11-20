
package models.manual

import base.SpecBase
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.manual.accountHolders.AccountHolderTypes.{OwnerDocumentedForeignFinancialInstitution, PassiveNonFinancialWithOneOrMoreControllingPerson}
import models.manual.accountHolders.EquityInterestType.{SettlorOfTrust, TrusteeOfTrust}
import models.manual.accountHolders.SelfCertification.Yes
import models.manual.accountHolders.{AdditionalOrgAhInfo, EquityInterestType, SelfCertification}
import models.manual.filercategory.WhatTypeOfFiler.Withholding
import models.manual.filercategory.WhatTypeOfFilerIsSponsor.ForeignFI
import models.manual.filercategory.{FilerCategoryTypes, WhatTypeOfFiler, WhatTypeOfFilerIsSponsor}
import models.{AddressModel, AddressUk, ReportId, TaxResidentInformation, UkAddress}


class OrganisationTypeSpec extends SpecBase {

  private val add: AddressModel = UkAddress(
    addressLine1 = "123 High Street",
    addressLine2 = None,
    city = "London",
    county = None,
    postcode = "SW1A 1AA",
    country = "GB"
  )
  private val residency: Seq[TaxResidentInformation] = Seq.empty

  private def generateReportId(regime: RegimeType): ReportId = ReportId(regime, 2020, None, "fi1")

  private val validCrsAdditionalInfo: AdditionalOrgAhInfo = AdditionalOrgAhInfo(
    accountHolderType = PassiveNonFinancialWithOneOrMoreControllingPerson,
    hasInterestType = true,
    interestType = Some(TrusteeOfTrust),
    selfCert = Some(Yes)
  )
  val baseOrg = OrganisationType(name = "org name",
    address = add,
    residency = residency,
    reportId = reportId,
    xmlEntityType = AccountHolder,
    additionalOrgAhInfo = Some(validCrsAdditionalInfo),
    filerCategory = None)

  "AdditionalOrgAhInfo" - {

    "missingMandatoryCrsData" - {

      "must return false when selfCert is present and hasInterestType" in {

        validCrsAdditionalInfo.hasIncorrectCrsData mustBe false
      }

      "must return true when selfCert is empty" in {
        val info = validCrsAdditionalInfo.copy(selfCert = None)
        info.hasIncorrectCrsData mustBe true
      }

      "must return true when hasInterestType is true and interestType is None" in {
        val info = validCrsAdditionalInfo.copy(interestType = None)
        info.hasIncorrectCrsData mustBe true
      }
    }
  }

  "OrganisationType" - {

    "isMandatoryAccountDataMissing" - {

      "for CRS AccountHolder" - {

        "must return true when additionalOrgAhInfo is None" in {
          val org = baseOrg.copy(additionalOrgAhInfo = None)
          org.hasWrongAccountHolderData mustBe true
        }

        "must return true when additionalOrgAhInfo has missing mandatory CRS data" in {
          val incompleteInfo = validCrsAdditionalInfo.copy(selfCert = None)
          val org = baseOrg.copy(additionalOrgAhInfo = Some(incompleteInfo))
          org.hasWrongAccountHolderData mustBe true
        }

        "must return false when additionalOrgAhInfo has all mandatory CRS data" in {
          baseOrg.hasWrongAccountHolderData mustBe false
        }
      }

      "for FATCA AccountHolder" - {
        val fatcaOrg = baseOrg.copy(reportId = reportId.copy(regime = FATCA))
        val fatcaAccountHolderOrgInfo = AdditionalOrgAhInfo(
          accountHolderType= OwnerDocumentedForeignFinancialInstitution,
          hasInterestType = false, interestType = None, selfCert = None
        )

        "must return true when hasInterestType is true additionalOrgAhInfo" in {
          val newAccHolderOrg = fatcaAccountHolderOrgInfo.copy(hasInterestType = true)
          val org = fatcaOrg.copy(additionalOrgAhInfo = Some(newAccHolderOrg))
          org.hasWrongAccountHolderData mustBe true
        }
        "must return true when selfCert is present in additionalOrgAhInfo" in {
          val newAccHolderOrg = fatcaAccountHolderOrgInfo.copy(selfCert = Some(Yes))
          val org = fatcaOrg.copy(additionalOrgAhInfo = Some(newAccHolderOrg))
          org.hasWrongAccountHolderData mustBe true
        }
        "must return true when interestType is present in additionalOrgAhInfo" in {
          val newAccHolderOrg = fatcaAccountHolderOrgInfo.copy(interestType = Some(SettlorOfTrust))
          val org = fatcaOrg.copy(additionalOrgAhInfo = Some(newAccHolderOrg))
          org.hasWrongAccountHolderData mustBe true
        }
        "must return false when additionalOrgAhInfo only includes a FATCA accountHolderType" in {
          val org = fatcaOrg.copy(additionalOrgAhInfo = Some(fatcaAccountHolderOrgInfo))
          org.hasWrongAccountHolderData mustBe true
        }
      }

      "for non-AccountHolder entity types" - {

        "must return false as long as no additionalOrgAhInfo is provided" in {
          val nonAccountHolders = Seq(ReportingFI, Sponsor, SubstantialOwner, ControllingPerson)

          nonAccountHolders.foreach { entityType =>
            baseOrg.copy(xmlEntityType = entityType, additionalOrgAhInfo = None)
              .hasWrongAccountHolderData mustBe false
          }
        }
        "must return true regardless of other data if additionalOrgAhInfo is provided" in {
          val nonAccountHolders = Seq(ReportingFI, Sponsor, SubstantialOwner, ControllingPerson)

          nonAccountHolders.foreach { entityType =>
            baseOrg.copy(xmlEntityType = entityType, additionalOrgAhInfo = None)
              .hasWrongAccountHolderData mustBe true
          }

        }
      }
    }

    "isIncorrectFilerProvided" - {

      "for FATCA" - {

        "must return true when filerCategory is provided for any entity else than a ReportingFI or a Sponsor" in {
          val nonSponsorOrReportingFi = Seq(AccountHolder, SubstantialOwner, ControllingPerson)

          nonSponsorOrReportingFi.foreach { entityType =>
            baseOrg.copy(xmlEntityType = entityType, filerCategory = Some(ForeignFI))
              .isIncorrectFilerProvided mustBe true
          }
        }

        "must return true for a Sponsor when filerCategory is None or not WhatTypeOfFilerIsSponsor" in {
        baseOrg.copy(xmlEntityType = Sponsor,filerCategory = Some(Withholding))
          .isIncorrectFilerProvided mustBe true
        }
      }

      "for CRS" - {
        val reportId = generateReportId(CRS)
        val allEntities = Seq(AccountHolder, ControllingPerson, ReportingFI)
        "must return true when filerCategory is present for any entity" in {
          allEntities.foreach { entityType =>
            baseOrg.copy(reportId = reportId, xmlEntityType = entityType, filerCategory = Some(Withholding))
              .isIncorrectFilerProvided mustBe true
          }
        }
        "must return false when filerCategory is None for all entities" in {
          allEntities.foreach { entityType =>
            baseOrg.copy(reportId = reportId, xmlEntityType = entityType, filerCategory = None)
              .isIncorrectFilerProvided mustBe false
          }
        }
      }
    }

    "toXml" - {

      "must return MissingMandatoryAccountHolderData when mandatory account data is missing" in {

        OrganisationType.toXml(crsOrg) mustBe Left(MissingMandatoryAccountHolderData)

        val fatcaOrg = buildOrg(regime = FATCA, entityType = AccountHolder, additionalInfo = None)
        OrganisationType.toXml(fatcaOrg) mustBe Left(MissingMandatoryAccountHolderData)
      }

      "must return Left(InvalidObjectForRegime) for Sponsor or SubstantialOwner under CRS" in {
        Seq(Sponsor, SubstantialOwner).foreach { invalidCrsEntity =>
          val org = buildOrg(regime = CRS, entityType = invalidCrsEntity)
          OrganisationType.toXml(org) mustBe Left(InvalidObjectForRegime)
        }
      }

      "must return Left(InvalidObjectForRegime) for ControllingPerson under FATCA" in {
        val org = buildOrg(regime = FATCA, entityType = ControllingPerson)
        OrganisationType.toXml(org) mustBe Left(InvalidObjectForRegime)
      }

      "must return Left(InvalidObjectForRegime) when isIncorrectFilerProvided is true" in {
        val invalidSponsor = buildOrg(
          regime = FATCA,
          entityType = Sponsor,
          filerCategory = None
        )
        OrganisationType.toXml(invalidSponsor) mustBe Left(InvalidObjectForRegime)

        val invalidCrsReportingFi = buildOrg(
          regime = CRS,
          entityType = ReportingFI,
          filerCategory = Some(WhatTypeOfFiler)
        )
        OrganisationType.toXml(invalidCrsReportingFi) mustBe Left(InvalidObjectForRegime)
      }

      "must return Right(NodeSeq) for valid CRS combinations" in {
        val validCrsCases = Seq(
