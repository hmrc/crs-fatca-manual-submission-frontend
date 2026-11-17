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

import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.accountHolders.AccountHolderTypes
import models.manual.accountHolders.AccountHolderTypes.*
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.manual.accountHolders.IndividualName
import models.viewModels.manual.cpso.{CPSOId, ControllingOrSubstantialOwner, ControllingOrSubstantialOwners}
import models.{ReportId, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.manual.accountHolders.AccountHoldersPage
import pages.manual.cpso.{ControllingOrSubstantialOwnersPage, CpsoAccountHolderIdsPage}

class UnlinkedAccountHoldersSpec extends AnyFreeSpec with Matchers {

  private val crsReportId   = ReportId(CRS, 2025, None, "TestfiID")
  private val fatcaReportId = ReportId(FATCA, 2025, None, "TestfiID")

  private def organisation(
                            name: String,
                            accountHolderType: AccountHolderTypes = PassiveNonFinancialWithOneOrMoreControllingPerson
                          ): AccountHolder =
    AccountHolder(
      individualOrOrganisation = Some(Organisation),
      organisationName = Some(name),
      accountHolderType = Some(accountHolderType)
    )

  private val individualAccountHolder = AccountHolder(
    individualOrOrganisation = Some(Individual),
    individualName = Some(IndividualName("Jane", "Doe"))
  )

  private def answersWith(
                           accountHolders: Seq[(AccountHolderId, AccountHolder)] = Seq.empty,
                           cpsos: Map[String, ControllingOrSubstantialOwner] = Map.empty,
                           links: Map[String, Set[AccountHolderId]] = Map.empty,
                           reportId: ReportId = crsReportId
                         ): UserAnswers = {
    val base = UserAnswers("id")
      .set(
        AccountHoldersPage()(reportId),
        AccountHolders(
          currentAccountHolderId = accountHolders.headOption.map(_._1),
          accountHolders = accountHolders
            .map(
              (accountHolderId, accountHolder) => accountHolderId.value -> accountHolder
            )
            .toMap
        )
      )
      .get

    val withCpsos =
      if cpsos.isEmpty then {
        base
      } else {
        base
          .set(
            ControllingOrSubstantialOwnersPage()(reportId),
            ControllingOrSubstantialOwners(
              currentCpsoId = cpsos.keys.headOption.map(CPSOId.apply),
              cpsos = cpsos
            )
          )
          .get
      }

    links.foldLeft(withCpsos) {
      case (answers, (cpsoId, accountHolderIds)) =>
        answers
          .set(CpsoAccountHolderIdsPage(CPSOId(cpsoId), reportId), accountHolderIds)
          .get
    }
  }

  private def accountHoldersWithoutACpso(answers: UserAnswers, reportId: ReportId = crsReportId): Seq[String] =
    UnlinkedAccountHolders.accountHoldersWithoutACpso(answers)(using reportId)

  "UnlinkedAccountHolders.accountHoldersWithoutACpso" - {

    "must return the mandatory account holders that are not linked to a CPSo" in {
      val linkedId   = AccountHolderId("ah-1")
      val unlinkedId = AccountHolderId("ah-2")

      val answers = answersWith(
        accountHolders = Seq(
          linkedId   -> organisation("Linked Ltd"),
          unlinkedId -> organisation("Unlinked Ltd")
        ),
        cpsos = Map("cpso-1" -> ControllingOrSubstantialOwner()),
        links = Map("cpso-1" -> Set(linkedId))
      )

      accountHoldersWithoutACpso(answers) mustBe Seq("Unlinked Ltd")
    }

    "must return every mandatory account holder when no CPSo has been added" in {
      val answers = answersWith(
        accountHolders = Seq(
          AccountHolderId("ah-1") -> organisation("Beta Ltd"),
          AccountHolderId("ah-2") -> organisation("Alpha Ltd")
        )
      )

      accountHoldersWithoutACpso(answers).sorted mustBe Seq("Alpha Ltd", "Beta Ltd")
    }

    "must return no account holders when every mandatory account holder is linked to a CPSo" in {
      val firstId  = AccountHolderId("ah-1")
      val secondId = AccountHolderId("ah-2")

      val answers = answersWith(
        accountHolders = Seq(
          firstId  -> organisation("First Ltd"),
          secondId -> organisation("Second Ltd")
        ),
        cpsos = Map("cpso-1" -> ControllingOrSubstantialOwner()),
        links = Map("cpso-1" -> Set(firstId, secondId))
      )

      accountHoldersWithoutACpso(answers) mustBe empty
    }

    "must treat account holders linked to different CPsos as linked" in {
      val firstId  = AccountHolderId("ah-1")
      val secondId = AccountHolderId("ah-2")

      val answers = answersWith(
        accountHolders = Seq(
          firstId  -> organisation("First Ltd"),
          secondId -> organisation("Second Ltd")
        ),
        cpsos = Map(
          "cpso-1" -> ControllingOrSubstantialOwner(),
          "cpso-2" -> ControllingOrSubstantialOwner()
        ),
        links = Map(
          "cpso-1" -> Set(firstId),
          "cpso-2" -> Set(secondId)
        )
      )

      accountHoldersWithoutACpso(answers) mustBe empty
    }

    "must exclude individual account holders" in {
      val answers = answersWith(
        accountHolders = Seq(
          AccountHolderId("ah-1") -> organisation("Organisation Ltd"),
          AccountHolderId("ah-2") -> individualAccountHolder
        )
      )

      accountHoldersWithoutACpso(answers) mustBe Seq("Organisation Ltd")
    }

    "must exclude CRS organisation types that do not require a CPSo" - {
      Seq(CrsReportablePerson, PassiveNonFinancialEntityThatIsCRSReportable).foreach {
        accountHolderType =>
          s"when the account holder type is $accountHolderType" in {
            val answers = answersWith(
              accountHolders = Seq(
                AccountHolderId("ah-1") -> organisation("Not Requiring A CPSo Ltd", accountHolderType)
              )
            )

            accountHoldersWithoutACpso(answers) mustBe empty
          }
      }
    }

    "must exclude an account holder where the individual or organisation question has not been answered" in {
      val answers = answersWith(
        accountHolders = Seq(
          AccountHolderId("ah-1") -> AccountHolder(
            organisationName = Some("Unknown Entity Ltd"),
            accountHolderType = Some(PassiveNonFinancialWithOneOrMoreControllingPerson)
          )
        )
      )

      accountHoldersWithoutACpso(answers) mustBe empty
    }

    "must exclude a mandatory organisation without a name" in {
      val answers = answersWith(
        accountHolders = Seq(
          AccountHolderId("ah-1") -> AccountHolder(
            individualOrOrganisation = Some(Organisation),
            organisationName = None,
            accountHolderType = Some(PassiveNonFinancialWithOneOrMoreControllingPerson)
          )
        )
      )

      accountHoldersWithoutACpso(answers) mustBe empty
    }

    "must return no account holders for a FATCA report" in {
      val answers = answersWith(
        accountHolders = Seq(
          AccountHolderId("ah-1") -> organisation("Fatca Ltd")
        ),
        reportId = fatcaReportId
      )

      accountHoldersWithoutACpso(answers, fatcaReportId) mustBe empty
    }

    "must return no account holders when none have been added" in {
      accountHoldersWithoutACpso(UserAnswers("id")) mustBe empty
    }
  }
}
