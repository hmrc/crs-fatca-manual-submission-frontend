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

import models.SubmissionsConstants.CRS
import models.viewModels.AccountHolders
import models.viewModels.manual.cpso.{CPSOId, ControllingOrSubstantialOwner}
import models.{ReportId, UserAnswers}
import pages.manual.cpso.ControllingOrSubstantialOwnersPage

import java.time.Instant

class CpsoService {

  def existingCpsos(userAnswers: UserAnswers, reportId: ReportId): Seq[(CPSOId, ControllingOrSubstantialOwner)] =
    userAnswers
      .get(ControllingOrSubstantialOwnersPage()(using reportId))
      .map(
        _.cpsos
          .map(
            (cpsoId, controllingOrSubstantialOwner) => (CPSOId(cpsoId), controllingOrSubstantialOwner)
          )
          .toSeq
      )
      .getOrElse(Seq.empty)
      .sortBy(_._2.creationDateTime)(Ordering.Option[Instant].reverse)
      .reverse
      .filter(
        (_, cpso) => cpso.name.nonEmpty
      )

  def isCpsoComplete(userAnswers: UserAnswers, cpsoId: CPSOId, reportId: ReportId): Boolean =
    existingCpsos(userAnswers, reportId)
      .filter(
        (csId, _) => csId == cpsoId
      )
      .map {
        case (csId, controllingOrSubstantialOwner) => controllingOrSubstantialOwner.isComplete(reportId.regime)
      }
      .headOption
      .getOrElse(false)

  def areAllCpsosComplete(userAnswers: UserAnswers, reportId: ReportId): Boolean =
    if (!accountHoldersRequireCpso(userAnswers, reportId)) {
      true
    } else {
      existingCpsos(userAnswers, reportId) match {
        case Seq() => reportId.regime != CRS
        case cpsos =>
          cpsos.forall {
            case (_, cpso) => cpso.isComplete(reportId.regime)
          }
      }
    }

  private def accountHoldersRequireCpso(userAnswers: UserAnswers, reportId: ReportId): Boolean =
    AccountHolders
      .existingAccountHolders(userAnswers)(using reportId)
      .exists {
        case (_, accountHolder) =>
          accountHolder.canHaveCPSO() && accountHolder.organisationName.nonEmpty
      }
}
