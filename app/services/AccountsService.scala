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

import models.NumberType.{Iban, Semp}
import models.SubmissionsConstants.{CRS, FATCA}
import models.viewModels.{Account, AccountHolderId, AccountId}
import models.{ReportId, UserAnswers}
import pages.manual.account.*
import pages.manual.accountHolders.{AccountHoldersPage, AccountPage as AccountHolderAccountsPage}

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try}

@Singleton
class AccountsService @Inject() () {

  def orderedAccounts(
    userAnswers: UserAnswers,
    reportId: ReportId
  ): Seq[(AccountId, String)] = {
    val accountsById = userAnswers
      .get(AccountsPage(reportId))
      .toSeq
      .flatMap(_.accounts.values)
      .flatMap(
        account => displayNumber(account).map(account.accountId -> _)
      )
      .toMap

    val recordedOrder = userAnswers
      .get(AccountOrderPage(reportId))
      .getOrElse(Seq.empty)
      .distinct

    val unrecordedIds = accountsById.keySet
      .diff(recordedOrder.toSet)
      .toSeq
      .sortBy(_.value)

    (recordedOrder ++ unrecordedIds).flatMap {
      accountId => accountsById.get(accountId).map(accountId -> _)
    }
  }

  def removeAccount(
    userAnswers: UserAnswers,
    reportId: ReportId,
    accountId: AccountId
  ): Try[UserAnswers] = {
    given ReportId = reportId

    if (userAnswers.get(AccountDataPage(accountId)).isEmpty) {
      Failure(
        new IllegalArgumentException(
          "Cannot remove an account outside the current report"
        )
      )
    } else {
      for {
        unlinked <- unlinkAccountFromHolders(
          userAnswers,
          reportId,
          accountId
        )

        removed <- unlinked.remove(AccountDataPage(accountId))

        selectionCleared <-
          if (removed.get(CurrentAccountIdPage()).contains(accountId)) {
            removed.remove(CurrentAccountIdPage())
          } else {
            Success(removed)
          }

        updated <-
          selectionCleared.get(AccountOrderPage(reportId)) match {
            case Some(order) =>
              selectionCleared.set(
                AccountOrderPage(reportId),
                order.filterNot(_ == accountId)
              )
            case None =>
              Success(selectionCleared)
          }
      } yield updated
    }
  }

  private def unlinkAccountFromHolders(
    userAnswers: UserAnswers,
    reportId: ReportId,
    accountId: AccountId
  ): Try[UserAnswers] = {
    given ReportId = reportId

    val accountHolderIds = userAnswers
      .get(AccountHoldersPage())
      .map(_.accountHolders.keys.toSeq.map(AccountHolderId.apply))
      .getOrElse(Seq.empty)

    accountHolderIds.foldLeft[Try[UserAnswers]](Success(userAnswers)) {
      (result, accountHolderId) =>
        result.flatMap {
          answers =>
            val page = AccountHolderAccountsPage(
              reportId,
              accountHolderId
            )

            answers.get(page) match {
              case Some(linkedAccounts) if linkedAccounts.contains(accountId) =>
                val remainingAccounts = linkedAccounts - accountId

                if (remainingAccounts.isEmpty) {
                  answers.remove(page)
                } else {
                  answers.setWithReportId(page, remainingAccounts)
                }

              case _ =>
                Success(answers)
            }
        }
    }
  }

  def recordAccount(
    userAnswers: UserAnswers,
    reportId: ReportId,
    accountId: AccountId
  ): Try[UserAnswers] = {
    given ReportId = reportId

    val existingIds = userAnswers
      .get(AccountsPage(reportId))
      .map(_.accounts.keySet.map(AccountId.apply))
      .getOrElse(Set.empty)

    if (!existingIds.contains(accountId)) {
      Failure(
        new IllegalArgumentException(
          "Cannot record an account outside the current report"
        )
      )
    } else {
      val recordedOrder = userAnswers
        .get(AccountOrderPage(reportId))
        .getOrElse(Seq.empty)
        .filter(existingIds.contains)
        .distinct

      val otherUnrecordedIds = existingIds
        .diff(recordedOrder.toSet)
        .filterNot(_ == accountId)
        .toSeq
        .sortBy(_.value)

      val updatedOrder =
        if (recordedOrder.contains(accountId)) {
          recordedOrder ++ otherUnrecordedIds
        } else {
          (recordedOrder ++ otherUnrecordedIds) :+ accountId
        }

      userAnswers.setWithReportId(
        AccountOrderPage(reportId),
        updatedOrder
      )
    }
  }

  def selectCurrentAccount(
    userAnswers: UserAnswers,
    reportId: ReportId,
    accountId: AccountId
  ): Try[UserAnswers] = {
    given ReportId = reportId

    if (userAnswers.get(AccountIdPage(accountId)).contains(accountId)) {
      userAnswers.setWithReportId(CurrentAccountIdPage(), accountId)
    } else {
      Failure(
        new IllegalArgumentException(
          "Cannot select an account outside the current report"
        )
      )
    }
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

    val haveNumber = userAnswers.get(HaveNumberPage(accountId))
    val numberType = userAnswers.get(NumberTypePage(accountId))

    val identifyingAnswersComplete =
      userAnswers.get(AccountIdPage(accountId)).contains(accountId) &&
        (haveNumber match {
          case Some(true) =>
            numberType.isDefined &&
            userAnswers
              .get(WhatIsAccountNumberPage(accountId, reportId))
              .exists(_.trim.nonEmpty)

          case Some(false) =>
            userAnswers
              .get(IdentifierPage(accountId))
              .exists(_.trim.nonEmpty)

          case None => false
        })

    val requiresAccountType =
      !(haveNumber.contains(true) &&
        numberType.exists(
          value => value == Iban || value == Semp
        ))

    val accountDetailsComplete =
      userAnswers.get(AccountClosedPage(accountId)).exists {
        isClosed =>
          reportId.regime match {
            case CRS =>
              val balanceOrCurrencyComplete =
                if (isClosed) {
                  userAnswers
                    .get(WhatWasTheAccountCurrencyPage(accountId))
                    .isDefined
                } else {
                  userAnswers
                    .get(WhatWasTheAccountBalancePage(accountId))
                    .isDefined
                }

              balanceOrCurrencyComplete &&
              userAnswers.get(IsUndocumentedAccountPage(accountId)).isDefined &&
              userAnswers.get(IsDormantAccountPage(accountId)).isDefined &&
              userAnswers.get(WasAccountOpenPage(accountId)).isDefined &&
              jointAccountAnswersComplete(userAnswers, accountId) &&
              (!requiresAccountType ||
                userAnswers.get(WhatAccountTypePage(accountId)).isDefined)

            case FATCA =>
              userAnswers
                .get(WhatWasTheAccountBalancePage(accountId))
                .isDefined

            case _ => false
          }
      }

    identifyingAnswersComplete &&
    accountDetailsComplete &&
    paymentAnswersComplete(userAnswers, accountId)
  }

  private def jointAccountAnswersComplete(
    userAnswers: UserAnswers,
    accountId: AccountId
  )(implicit reportId: ReportId): Boolean =
    userAnswers.get(IsJointAccountPage(accountId)) match {
      case Some(true) =>
        userAnswers.get(HowManyJointAccountHoldersPage(accountId)).isDefined
      case Some(false) => true
      case None        => false
    }

  private def paymentAnswersComplete(
    userAnswers: UserAnswers,
    accountId: AccountId
  )(implicit reportId: ReportId): Boolean =
    userAnswers.get(HavePaymentsPage(accountId)) match {
      case Some(false) => true

      case Some(true) =>
        userAnswers.get(AccountPaymentListPage(accountId)).exists {
          payments =>
            payments.nonEmpty && payments.forall {
              payment =>
                payment.accountPaymentsAmount.exists {
                  amount =>
                    amount.amount.trim.nonEmpty &&
                    amount.currency.code.trim.nonEmpty
                }
            }
        }

      case None => false
    }

  private def displayNumber(account: Account): Option[String] = {
    val candidates =
      if (account.haveNumber.contains(true)) {
        Seq(account.whatIsAccountNumber, account.identifier)
      } else {
        Seq(account.identifier, account.whatIsAccountNumber)
      }

    candidates.flatten.find(_.trim.nonEmpty)
  }
}
