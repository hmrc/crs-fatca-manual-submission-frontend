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

package services.manual.account

import models.SubmissionsConstants.{CRS, FATCA}
import models.viewModels.{AccountHolderId, AccountId}
import models.{ReportId, UserAnswers}
import pages.manual.account.*
import pages.manual.accountHolders.{AccountHoldersPage, AccountPage as AccountHolderAccountsPage}
import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.util.{Success, Try}

@Singleton
class AccountsService @Inject() () {

  def orderedAccounts(
    userAnswers: UserAnswers,
    reportId: ReportId
  ): Seq[(AccountId, String)] =
    userAnswers
      .get(AccountsPage(reportId))
      .toSeq
      .flatMap(_.accounts.values)
      .sortBy {
        account =>
          val creationTime =
            account.creationDateTime.getOrElse(Instant.EPOCH)
          (
            creationTime.getEpochSecond,
            creationTime.getNano,
            account.accountId.value
          )
      }
      .flatMap {
        account =>
          val candidates =
            if (account.haveNumber.contains(true)) {
              Seq(account.whatIsAccountNumber, account.identifier)
            } else {
              Seq(account.identifier, account.whatIsAccountNumber)
            }
          candidates.flatten
            .find(_.trim.nonEmpty)
            .map(account.accountId -> _)
      }

  def removeAccount(
    userAnswers: UserAnswers,
    reportId: ReportId,
    accountId: AccountId
  ): Try[UserAnswers] = {
    given ReportId = reportId
    val accountHolderIds = userAnswers
      .get(AccountHoldersPage())
      .toSeq
      .flatMap(_.accountHolders.keys)
      .map(AccountHolderId.apply)

    for {
      _ <- userAnswers
        .get(AccountDataPage(accountId))
        .toRight(
          new IllegalArgumentException(
            "Cannot remove an account outside the current report"
          )
        )
        .toTry
      unlinked <- accountHolderIds.foldLeft(Try(userAnswers)) {
        (result, accountHolderId) =>
          result.flatMap {
            answers =>
              val page = AccountHolderAccountsPage(reportId, accountHolderId)
              answers
                .get(page)
                .filter(_.contains(accountId))
                .map(_ - accountId) match {
                case Some(remaining) if remaining.isEmpty =>
                  answers.remove(page)
                case Some(remaining) =>
                  answers.setWithReportId(page, remaining)
                case None =>
                  Success(answers)
              }
          }
      }
      removed <- unlinked.remove(AccountDataPage(accountId))
      selectionCleared <-
        if (removed.get(CurrentAccountIdPage()).contains(accountId)) {
          removed.remove(CurrentAccountIdPage())
        } else {
          Success(removed)
        }
    } yield selectionCleared
  }

  def clearCurrentAccount(
    userAnswers: UserAnswers,
    reportId: ReportId
  ): Try[UserAnswers] =
    userAnswers.remove(CurrentAccountIdPage()(reportId))

  def isComplete(
    userAnswers: UserAnswers,
    reportId: ReportId,
    accountId: AccountId
  ): Boolean = {
    given ReportId = reportId

    val complete = for {
      _ <- userAnswers.get(AccountIdPage(accountId)).filter(_ == accountId)

      number <- userAnswers.get(HaveNumberPage(accountId)).flatMap {
        case true =>
          for {
            _ <- userAnswers.get(NumberTypePage(accountId))
            accountNumber <- userAnswers.get(
              WhatIsAccountNumberPage(accountId, reportId)
            )
          } yield accountNumber
        case false =>
          userAnswers.get(IdentifierPage(accountId))
      }
      if number.trim.nonEmpty
      isClosed <- userAnswers.get(AccountClosedPage(accountId))
      _ <- reportId.regime match {
        case CRS =>
          for {
            _ <-
              if (isClosed) {
                userAnswers.get(WhatWasTheAccountCurrencyPage(accountId))
              } else {
                userAnswers.get(WhatWasTheAccountBalancePage(accountId))
              }
            _       <- userAnswers.get(IsUndocumentedAccountPage(accountId))
            _       <- userAnswers.get(IsDormantAccountPage(accountId))
            _       <- userAnswers.get(WasAccountOpenPage(accountId))
            isJoint <- userAnswers.get(IsJointAccountPage(accountId))
            if !isJoint ||
              userAnswers.get(HowManyJointAccountHoldersPage(accountId)).isDefined
            _ <- userAnswers.get(WhatAccountTypePage(accountId))
          } yield ()

        case FATCA =>
          userAnswers.get(WhatWasTheAccountBalancePage(accountId))
      }

      havePayments <- userAnswers.get(HavePaymentsPage(accountId))
      paymentsComplete = userAnswers.get(AccountPaymentListPage(accountId)).exists {
        payments =>
          payments.nonEmpty && payments.forall(_.accountPaymentsAmount.exists {
            amount =>
              amount.amount.trim.nonEmpty &&
              amount.currency.code.trim.nonEmpty
          })
      }
      if !havePayments || paymentsComplete
    } yield ()

    complete.isDefined
  }
}
