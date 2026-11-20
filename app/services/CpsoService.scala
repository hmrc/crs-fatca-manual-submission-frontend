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

import models.SubmissionsConstants.{CRS, FATCA}
import models.viewModels.AccountHolders
import models.viewModels.manual.cpso.{CPSOId, ControllingOrSubstantialOwner}
import models.{ReportId, UserAnswers}
import pages.manual.cpso.ControllingOrSubstantialOwnersPage

import java.time.Instant

class CpsoService {

  def existingCpsos(userAnswers: UserAnswers)(implicit reportId: ReportId): Seq[(CPSOId, ControllingOrSubstantialOwner)] =
    userAnswers
      .get(ControllingOrSubstantialOwnersPage())
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


  def areAllCpsosComplete(userAnswers: UserAnswers)(implicit reportId: ReportId): Boolean = {
    //are there any account holders that require cpsos.
    // If not then return true
    //else get all cpsos, if empty and fatca then return true
    // if crs and empty return false
    // go through list of cpso and call iscomplete method

    val accountHoldersRequireCpso = AccountHolders
      .existingAccountHolders(userAnswers)
      .exists(
        (_, accountHolder) => accountHolder.canHaveCPSO() && accountHolder.organisationName.nonEmpty
      )

    val allCpsos = existingCpsos(userAnswers)

    if(!accountHoldersRequireCpso) {
      true
    } else {
        if(allCpsos.isEmpty && reportId.regime == FATCA) {
          true
        }
        else if(allCpsos.isEmpty && reportId.regime == CRS) {
          false
        }
        else {
          allCpsos.forall(_._2.isComplete(reportId.regime))
        }
    }

  }
}
