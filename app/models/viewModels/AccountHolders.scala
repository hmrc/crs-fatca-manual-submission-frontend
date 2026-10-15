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

import play.api.libs.json.*

case class AccountHolders(currentAccountHolderId: Option[AccountHolderId], accountHolders: Map[String, AccountHolder])

object AccountHolders:
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
