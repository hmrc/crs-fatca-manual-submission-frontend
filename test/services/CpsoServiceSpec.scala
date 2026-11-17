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
import models.manual.cpso.IndividualName
import models.viewModels.manual.cpso.{CPSOId, ControllingOrSubstantialOwner, ControllingOrSubstantialOwners}
import models.{ReportId, UserAnswers}
import pages.manual.cpso.ControllingOrSubstantialOwnersPage

import java.time.Instant

class CpsoServiceSpec extends SpecBase {

  private val service = new CpsoService()

  private val fatcaReportId = ReportId(FATCA, 2025, None, "TestfiID")

  private val oldestCreated = Instant.parse("2025-03-01T09:00:00Z")
  private val middleCreated = Instant.parse("2025-08-15T12:30:00Z")
  private val newestCreated = Instant.parse("2026-01-20T16:45:00Z")

  private def owner(
                     name: String,
                     creationDateTime: Option[Instant] = None
                   ): ControllingOrSubstantialOwner =
    ControllingOrSubstantialOwner(
      organisationName = Some(name),
      creationDateTime = creationDateTime
    )

  private def answersWith(
                           cpsos: Map[String, ControllingOrSubstantialOwner],
                           report: ReportId = reportId
                         ): UserAnswers =
    emptyUserAnswers.withPage(
      ControllingOrSubstantialOwnersPage()(report),
      ControllingOrSubstantialOwners(None, cpsos)
    )

  "CpsoService.existingCpsos" - {

    "must return no CPsos when none have been added" in {
      service.existingCpsos(emptyUserAnswers) mustBe empty
    }

    "must return no CPsos when the CPSo collection is empty" in {
      service.existingCpsos(answersWith(Map.empty)) mustBe empty
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

      service.existingCpsos(answers) must contain theSameElementsAs Seq(
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

      service.existingCpsos(answers).map(_._2.name) mustBe Seq(
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

      service.existingCpsos(answers).map(_._2.name) mustBe Seq(
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

      service.existingCpsos(answers).map(_._2.name) mustBe Seq("CRS owner")
      service.existingCpsos(answers)(using fatcaReportId).map(_._2.name) mustBe Seq("FATCA owner")
    }
  }
}
