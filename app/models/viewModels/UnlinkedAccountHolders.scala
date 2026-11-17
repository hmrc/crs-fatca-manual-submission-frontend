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

import models.viewModels.manual.cpso.ControllingOrSubstantialOwners
import models.{ReportId, UserAnswers}
import pages.manual.cpso.CpsoAccountHolderIdsPage

object UnlinkedAccountHolders {

  def accountHoldersWithoutACpso(answers: UserAnswers)(using reportId: ReportId): Seq[String] =
    allAccountHolders(answers)
      .filterNot(
        accountHolders => linkedAccountHolderIds(answers).contains(accountHolders._1)
      )
      .map(
        accountHolder => accountHolder._2.getName()
      )

  private def allAccountHolders(userAnswers: UserAnswers)(using reportId: ReportId) =
    AccountHolders
      .existingAccountHolders(userAnswers)
      .filter(
        (_, accountHolder) => accountHolder.canHaveCPSO() && accountHolder.getName().nonEmpty
      )

  private def linkedAccountHolderIds(answers: UserAnswers)(using reportId: ReportId): Set[AccountHolderId] =
    ControllingOrSubstantialOwners
      .existingCpsos(answers)
      .flatMap(
        (cpsoId, _) => answers.get(CpsoAccountHolderIdsPage(cpsoId, reportId)).getOrElse(Set.empty[AccountHolderId])
      )
      .toSet
}
