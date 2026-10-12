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
import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.CheckboxItemViewModel

case class Accounts(
  currentAccountId: Option[AccountId],
  accounts: Map[String, Account]
)

object Accounts {

  private val CurrentAccountId = "currentAccountId"

  implicit val format: OFormat[Accounts] = new OFormat[Accounts] {

    override def reads(json: JsValue): JsResult[Accounts] =
      json.validate[JsObject].flatMap {
        obj =>

          val currentAccountId =
            (obj \ CurrentAccountId).validateOpt[AccountId]

          val accountEntries =
            JsObject(
              obj.fields.filterNot(_._1 == CurrentAccountId)
            ).validate[Map[String, Account]]

          for {
            currentId <- currentAccountId
            accounts  <- accountEntries
          } yield Accounts(currentId, accounts)
      }

    override def writes(value: Accounts): JsObject = {
      val accountJson =
        JsObject(
          value.accounts.map {
            case (id, account) =>
              id -> Json.toJson(account)
          }
        )

      value.currentAccountId match {
        case Some(id) =>
          accountJson + (CurrentAccountId -> Json.toJson(id))

        case None =>
          accountJson
      }
    }
  }

  def existingAccounts(
    userAnswers: UserAnswers,
    reportId: ReportId
  ): Seq[(AccountId, String)] =
    userAnswers
      .get(AccountsPage(reportId))
      .map(
        _.accounts.values
          .flatMap {
            account =>
              account.accountNumber.map(account.accountId -> _)
          }
          .toSeq
      )
      .getOrElse(Seq.empty)

  def checkboxOptions(
    accounts: Seq[(AccountId, String)]
  ): Seq[CheckboxItem] =
    accounts.zipWithIndex.map {
      case ((accountId, accountNumber), index) =>
        CheckboxItemViewModel(
          content = Text(accountNumber),
          fieldId = "value",
          index = index,
          value = accountId.value
        ).copy(name = Some(s"value[$index]"))
    }
}
