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

package services

import base.SpecBase
import models.SubmissionsConstants.FATCA
import models.manual.TaxResidentInformation
import models.manual.accountHolders.AccountHolderTypes.{CrsReportablePerson, OwnerDocumentedForeignFinancialInstitution}
import models.manual.accountHolders.IndividualOrOrganisation.{Individual as AccountHolderIndividual, Organisation as AccountHolderOrganisation}
import models.manual.cpso.IndividualOrOrganisation.{Individual as CpsoIndividual, Organisation as CpsoOrganisation}
import models.manual.cpso.{ControllingPersonType, ControllingPersonTypes, CpsoSelfCertification, IndividualName}
import models.response.Country
import models.viewModels.manual.cpso.{CPSOId, ControllingOrSubstantialOwner, ControllingOrSubstantialOwners}
import models.viewModels.{AccountHolder, AccountHolders}
import models.{AddressNonUk, ReportId, UserAnswers}
import pages.manual.accountHolders.AccountHoldersPage
import pages.manual.cpso.ControllingOrSubstantialOwnersPage

import java.time.Instant

class CpsoServiceSpec extends SpecBase {

  private val service = new CpsoService()

  private val fatcaReportId = ReportId(FATCA, 2025, None, "TestfiID")

  private val oldestCreated = Instant.parse("2025-03-01T09:00:00Z")
  private val middleCreated = Instant.parse("2025-08-15T12:30:00Z")
  private val newestCreated = Instant.parse("2026-01-20T16:45:00Z")

  private val addressNonUk           = AddressNonUk("1 Test Street", None, "", None, None, "GB")
  private val taxResidentInformation = TaxResidentInformation(Country.GB, hasTaxIdentificationNumber = Some(true), taxIdentifierNumber = Some("12333"))

  private val completeCpso = ControllingOrSubstantialOwner(
    individualOrOrganisation = Some(CpsoIndividual),
    individualName = Some(IndividualName("Jane", "Doe")),
    accountHolders = Some(Seq("ah-1")),
    individualHaveDateOfBirth = Some(false),
    individualHavePlaceOfBirth = Some(false),
    areTheyBasedInTheUk = Some(false),
    addressNonUk = Some(addressNonUk),
    taxResidentCountries = Some(Seq(taxResidentInformation)),
    selfCertification = Some(CpsoSelfCertification.Yes),
    controllingPersonType = Some(ControllingPersonTypes(Seq(ControllingPersonType.OwnershipOfLegalPerson)))
  )

  private val incompleteCpso = completeCpso.copy(individualName = None)

  private val completeOrganisationCpso = ControllingOrSubstantialOwner(
    individualOrOrganisation = Some(CpsoOrganisation),
    organisationName = Some("Acme Ltd"),
    accountHolders = Some(Seq("ah-1")),
    areTheyBasedInTheUk = Some(false),
    addressNonUk = Some(addressNonUk),
    taxResidentCountries = Some(Seq(taxResidentInformation))
  )

  private val accountHolderRequiringCpso = AccountHolder(
    individualOrOrganisation = Some(AccountHolderOrganisation),
    organisationName = Some("Acme Ltd"),
    accountHolderType = Some(OwnerDocumentedForeignFinancialInstitution)
  )

  private val individualAccountHolder = AccountHolder(
    individualOrOrganisation = Some(AccountHolderIndividual)
  )

  private val nonMandatoryOrganisationAccountHolder = AccountHolder(
    individualOrOrganisation = Some(AccountHolderOrganisation),
    organisationName = Some("Non-mandatory Ltd"),
    accountHolderType = Some(CrsReportablePerson)
  )

  private def owner(
    name: String,
    creationDateTime: Option[Instant] = None
  ): ControllingOrSubstantialOwner =
    ControllingOrSubstantialOwner(
      organisationName = Some(name),
      creationDateTime = creationDateTime
    )

  private def answersWith(
    cpsos: Map[String, ControllingOrSubstantialOwner] = Map.empty,
    accountHolders: Map[String, AccountHolder] = Map.empty,
    report: ReportId = reportId
  ): UserAnswers = {
    val withCpsos = emptyUserAnswers.withPage(
      ControllingOrSubstantialOwnersPage()(report),
      ControllingOrSubstantialOwners(None, cpsos)
    )

    if accountHolders.isEmpty then {
      withCpsos
    } else {
      withCpsos.withPage(
        AccountHoldersPage()(report),
        AccountHolders(None, accountHolders)
      )
    }
  }

  "CpsoService.existingCpsos" - {

    "must return no CPsos when none have been added" in {
      service.existingCpsos(emptyUserAnswers, reportId) mustBe empty
    }

    "must return no CPsos when the CPSo collection is empty" in {
      service.existingCpsos(answersWith(Map.empty), reportId) mustBe empty
    }

    "must return each CPSo with its id and details unchanged" in {
      val organisationOwner = ControllingOrSubstantialOwner(
        accountHolders = Some(Seq("AH-1")),
        organisationName = Some("Acme Holdings Ltd"),
        creationDateTime = Some(middleCreated)
      )

      val individualOwner = ControllingOrSubstantialOwner(
        individualName = Some(IndividualName("Jane", "Doe")),
        creationDateTime = Some(newestCreated)
      )

      val answers = answersWith(
        Map(
          "21" -> organisationOwner,
          "22" -> individualOwner
        )
      )

      service.existingCpsos(answers, reportId) must contain theSameElementsAs Seq(
        CPSOId("21") -> organisationOwner,
        CPSOId("22") -> individualOwner
      )
    }

    "must order CPsos by creationDateTime, oldest first" in {
      val answers = answersWith(
        Map(
          "21" -> owner("Newest owner", Some(newestCreated)),
          "22" -> owner("Oldest owner", Some(oldestCreated)),
          "23" -> owner("Middle owner", Some(middleCreated))
        )
      )

      service.existingCpsos(answers, reportId).map(_._2.name) mustBe Seq(
        "Oldest owner",
        "Middle owner",
        "Newest owner"
      )
    }

    "must list CPsos without a creationDateTime before those that have one" in {
      val answers = answersWith(
        Map(
          "21" -> owner("Newest owner", Some(newestCreated)),
          "22" -> owner("Undated owner"),
          "23" -> owner("Oldest owner", Some(oldestCreated))
        )
      )

      service.existingCpsos(answers, reportId).map(_._2.name) mustBe Seq(
        "Undated owner",
        "Oldest owner",
        "Newest owner"
      )
    }

    "must return only the CPsos for the requested report" in {
      val answers = emptyUserAnswers
        .withPage(
          ControllingOrSubstantialOwnersPage()(reportId),
          ControllingOrSubstantialOwners(None, Map("21" -> owner("CRS owner")))
        )
        .withPage(
          ControllingOrSubstantialOwnersPage()(fatcaReportId),
          ControllingOrSubstantialOwners(None, Map("99" -> owner("FATCA owner")))
        )

      service.existingCpsos(answers, reportId).map(_._2.name) mustBe Seq("CRS owner")
      service.existingCpsos(answers, fatcaReportId).map(_._2.name) mustBe Seq("FATCA owner")
    }
  }
  "CpsoService.areAllCpsosComplete" - {

    "must return true when no account holders require a CPSo" in {
      service.areAllCpsosComplete(answersWith(), reportId) mustBe true
    }

    "must return true when the account holders do not require a CPSo" in {
      val answers = answersWith(
        accountHolders = Map(
          "ah-1" -> individualAccountHolder,
          "ah-2" -> nonMandatoryOrganisationAccountHolder
        )
      )

      service.areAllCpsosComplete(answers, reportId) mustBe true
    }

    "must return false for a CRS report when an account holder requires a CPSo but none have been added" in {
      val answers = answersWith(
        accountHolders = Map("ah-1" -> accountHolderRequiringCpso)
      )

      service.areAllCpsosComplete(answers, reportId) mustBe false
    }

    "must return true for a FATCA report when an account holder requires a CPSo but none have been added" in {
      val answers = answersWith(
        accountHolders = Map("ah-1" -> accountHolderRequiringCpso),
        report = fatcaReportId
      )

      service.areAllCpsosComplete(answers, fatcaReportId) mustBe true
    }

    "must return true when every CPSo is complete" - {

      "for a CRS report" in {
        val answers = answersWith(
          cpsos = Map("21" -> completeCpso),
          accountHolders = Map("ah-1" -> accountHolderRequiringCpso)
        )

        service.areAllCpsosComplete(answers, reportId) mustBe true
      }

      "for a FATCA report" in {
        val answers = answersWith(
          cpsos = Map("21" -> completeCpso),
          accountHolders = Map("ah-1" -> accountHolderRequiringCpso),
          report = fatcaReportId
        )

        service.areAllCpsosComplete(answers, fatcaReportId) mustBe true
      }
    }

    "must return false when any CPSo is incomplete" - {

      "for a CRS report" in {
        val answers = answersWith(
          cpsos = Map(
            "21" -> completeCpso,
            "22" -> incompleteCpso
          ),
          accountHolders = Map("ah-1" -> accountHolderRequiringCpso)
        )

        service.areAllCpsosComplete(answers, reportId) mustBe false
      }

      "for a FATCA report" in {
        val answers = answersWith(
          cpsos = Map(
            "21" -> completeCpso,
            "22" -> incompleteCpso
          ),
          accountHolders = Map("ah-1" -> accountHolderRequiringCpso),
          report = fatcaReportId
        )

        service.areAllCpsosComplete(answers, fatcaReportId) mustBe false
      }
    }

    "must return true when CPsos exist but no account holder requires one" in {
      val answers = answersWith(
        cpsos = Map("21" -> incompleteCpso),
        accountHolders = Map("ah-1" -> individualAccountHolder)
      )

      service.areAllCpsosComplete(answers, reportId) mustBe true
    }
  }

  "CpsoService.isCpsoComplete" - {

    "must return true when the identified CP-SO is complete" in {
      val answers = answersWith(Map("21" -> completeCpso))

      service.isCpsoComplete(answers, CPSOId("21"), reportId) mustBe true
    }

    "must return true for a FATCA report when the identified CP-SO is complete" in {
      val answers = answersWith(Map("21" -> completeCpso), report = fatcaReportId)

      service.isCpsoComplete(answers, CPSOId("21"), fatcaReportId) mustBe true
    }

    "must return false when the identified CP-SO is incomplete" in {
      val answers = answersWith(Map("21" -> incompleteCpso))

      service.isCpsoComplete(answers, CPSOId("21"), reportId) mustBe false
    }

    "must return false for a CRS report when the CP-SO has no self certification" in {
      val answers = answersWith(Map("21" -> completeCpso.copy(selfCertification = None)))

      service.isCpsoComplete(answers, CPSOId("21"), reportId) mustBe false
    }

    "must return false for a CRS report when the CP-SO has no controlling person type" in {
      val answers = answersWith(Map("21" -> completeCpso.copy(controllingPersonType = None)))

      service.isCpsoComplete(answers, CPSOId("21"), reportId) mustBe false
    }

    "must not require self certification or controlling person type for a FATCA report" in {
      val cpso    = completeCpso.copy(selfCertification = None, controllingPersonType = None)
      val answers = answersWith(Map("21" -> cpso), report = fatcaReportId)

      service.isCpsoComplete(answers, CPSOId("21"), fatcaReportId) mustBe true
    }

    "must return true for a CRS report when an organisation CP-SO has all of its own details" in {
      val answers = answersWith(Map("21" -> completeOrganisationCpso))

      service.isCpsoComplete(answers, CPSOId("21"), reportId) mustBe true
    }

    "must return false when the CP-SO is not linked to an account holder" in {
      val answers = answersWith(Map("21" -> completeCpso.copy(accountHolders = Some(Seq.empty))))

      service.isCpsoComplete(answers, CPSOId("21"), reportId) mustBe false
    }

    "must return false when the requested CP-SO does not exist" in {
      val answers = answersWith(Map("21" -> completeCpso))

      service.isCpsoComplete(answers, CPSOId("99"), reportId) mustBe false
    }

    "must return false when no CP-SOs have been added" in {
      service.isCpsoComplete(answersWith(), CPSOId("21"), reportId) mustBe false
    }

    "must return false when there are no user answers at all" in {
      service.isCpsoComplete(emptyUserAnswers, CPSOId("21"), reportId) mustBe false
    }

    "must only consider the requested CP-SO, not the others in the report" in {
      val answers = answersWith(Map("21" -> completeCpso, "22" -> incompleteCpso))

      service.isCpsoComplete(answers, CPSOId("21"), reportId) mustBe true
      service.isCpsoComplete(answers, CPSOId("22"), reportId) mustBe false
    }
  }

}
