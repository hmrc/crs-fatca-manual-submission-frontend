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

import models.{ReportId, UserAnswers}
import pages.manual.account.AccountsPage
import pages.manual.accountHolders.AccountPage

object UnlinkedAccounts {

  private def allAccounts(answers: UserAnswers)(using reportId: ReportId): Seq[Account] =
    answers
      .get(AccountsPage(reportId))
      .map(_.accounts.values.toSeq)
      .getOrElse(Seq.empty)

  private def linkedAccountIds(answers: UserAnswers)(using reportId: ReportId): Set[AccountId] =
    AccountHolders
      .existingAccountHolders(answers)
      .flatMap(
        (holderId, _) => answers.get(AccountPage(reportId, holderId)).getOrElse(Set.empty[AccountId])
      )
      .toSet

  def accountsWithoutAnAccountHolder(answers: UserAnswers)(using reportId: ReportId): Seq[String] =
    allAccounts(answers)
      .filterNot(
        account => linkedAccountIds(answers).contains(account.accountId)
      )
      .map(
        account => account.whatIsAccountNumber.getOrElse(account.identifier.getOrElse(""))
      )

}
