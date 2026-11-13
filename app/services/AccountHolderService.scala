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
import models.manual.account.WhatAccountType
import models.viewModels.{AccountHolder, AccountHolderId, AccountHolders}
import models.{ReportId, UserAnswers}
import pages.manual.account.AccountsPage

import java.time.Instant
import javax.inject.Inject

class AccountHolderService @Inject() () {

  def accountHolders(userAnswers: UserAnswers)(implicit reportId: ReportId): Seq[(AccountHolderId, AccountHolder)] = {
    val accountTypes: Map[String, Option[WhatAccountType]] =
      userAnswers
        .get(AccountsPage(reportId))
        .map(
          _.accounts.view.mapValues(_.whatAccountType).toMap
        )
        .getOrElse(Map.empty)

    AccountHolders
      .existingAccountHolders(userAnswers)
      .filter(
        (_, accountHolder) => accountHolder.getName().nonEmpty
      )
      .map {
        case (accountHolderId, accountHolder) =>
          accountHolderId -> withEquityInterestTypeRequirement(accountHolder, accountTypes)
      }
      .sortBy(_._2.creationDateTime)(Ordering.Option[Instant].reverse)
      .reverse
  }

  private def withEquityInterestTypeRequirement(
    accountHolder: AccountHolder,
    accountTypes: Map[String, Option[WhatAccountType]]
  )(implicit reportId: ReportId): AccountHolder =
    accountHolder.account.filter(_.nonEmpty) match {
      case Some(linkedAccountIds) if reportId.regime == CRS =>
        val equityInterestTypeRequired =
          linkedAccountIds.exists(
            accountId => accountTypes.get(accountId).flatten.contains(WhatAccountType.InvestmentEntity)
          )

        accountHolder.copy(checkEquityInterestTypeRequired = Some(equityInterestTypeRequired))

      case _ =>
        accountHolder
    }
}
