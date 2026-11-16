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

case class AccountHolders(currentAccountHolderId: Option[AccountHolderId],
                          accountHolders: Map[String, AccountHolder],
                          doYouWantToAddAnotherAccountHolder: Option[Boolean] = None
)

object AccountHolders {
  private val CurrentAccountHolderId             = "currentAccountHolderId"
  private val DoYouWantToAddAnotherAccountHolder = "doYouWantToAddAnotherAccountHolder"

  implicit val format: OFormat[AccountHolders] = new OFormat[AccountHolders] {

    override def reads(json: JsValue): JsResult[AccountHolders] =
      json.validate[JsObject].flatMap {
        obj =>

          val currentAccountHolderId =
            (obj \ CurrentAccountHolderId).validateOpt[AccountHolderId]

          val doYouWantToAddAnotherAccountHolder =
            (obj \ DoYouWantToAddAnotherAccountHolder).validateOpt[Boolean]

          val accountHolderEntries =
            JsObject(
              obj.fields.filterNot(
                f => f._1 == CurrentAccountHolderId || f._1 == DoYouWantToAddAnotherAccountHolder
              )
            ).validate[Map[String, AccountHolder]]

          for {
            currentId               <- currentAccountHolderId
            accountHolders          <- accountHolderEntries
            addAnotherAccountHolder <- doYouWantToAddAnotherAccountHolder
          } yield AccountHolders(currentId, accountHolders, addAnotherAccountHolder)
      }

    override def writes(value: AccountHolders): JsObject = {
      val accountHolderJson =
        JsObject(
          value.accountHolders.map {
            case (id, accountHolder) =>
              id -> Json.toJson(accountHolder)
          }
        )

      val accountHolderJsonAndCurrentAccountHolderId = value.currentAccountHolderId match {
        case Some(id) =>
          accountHolderJson + (CurrentAccountHolderId -> Json.toJson(id))

        case None =>
          accountHolderJson
      }

      value.doYouWantToAddAnotherAccountHolder match {
        case Some(addAccountHolder) =>
          accountHolderJsonAndCurrentAccountHolderId + (DoYouWantToAddAnotherAccountHolder -> Json.toJson(addAccountHolder))
        case None =>
          accountHolderJsonAndCurrentAccountHolderId
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
