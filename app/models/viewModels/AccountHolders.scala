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
import pages.manual.accountHolders.AccountHoldersPage
import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import viewmodels.govuk.all.CheckboxItemViewModel

case class AccountHolders(currentAccountHolderId: Option[AccountHolderId], accountHolders: Map[String, AccountHolder])

object AccountHolders {
  private val CurrentAccountHolderId = "currentAccountHolderId"

  implicit val format: OFormat[AccountHolders] = new OFormat[AccountHolders] {

    override def reads(json: JsValue): JsResult[AccountHolders] =
      json.validate[JsObject].flatMap {
        obj =>

          val currentAccountHolderId =
            (obj \ CurrentAccountHolderId).validateOpt[AccountHolderId]

          val accountHolderEntries =
            JsObject(
              obj.fields.filterNot(_._1 == CurrentAccountHolderId)
            ).validate[Map[String, AccountHolder]]

          for {
            currentId      <- currentAccountHolderId
            accountHolders <- accountHolderEntries
          } yield AccountHolders(currentId, accountHolders)
      }

    override def writes(value: AccountHolders): JsObject = {
      val accountHolderJson =
        JsObject(
          value.accountHolders.map {
            case (id, accountHolder) =>
              id -> Json.toJson(accountHolder)
          }
        )

      value.currentAccountHolderId match {
        case Some(id) =>
          accountHolderJson + (CurrentAccountHolderId -> Json.toJson(id))

        case None =>
          accountHolderJson
      }
    }
  }

  def existingAccountHolders(userAnswers: UserAnswers)(implicit reportId: ReportId): Seq[(AccountHolderId, AccountHolder)] =
    userAnswers
      .get(AccountHoldersPage())
      .map(
        _.accountHolders
          .map(
            (accountHolderID, accountHolder) => (AccountHolderId(accountHolderID), accountHolder)
          )
          .toSeq
      )
      .getOrElse(Seq.empty)

  def checkboxOptions(accountHolders: Seq[(AccountHolderId, AccountHolder)]): Seq[CheckboxItem] =
    accountHolders.zipWithIndex.map {
      case ((accountHolderId, accountHolder), index) =>
        CheckboxItemViewModel(
          content = Text(accountHolder.organisationName.getOrElse("")),
          fieldId = "value",
          index = index,
          value = accountHolderId.value
        ).copy(name = Some(s"value[$index]"))
    }

}
