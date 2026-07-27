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

import play.api.libs.json.{Format, JsString, Reads, Writes}

import scala.annotation.tailrec
import scala.util.Random

case class AccountId(value: String)

object AccountId {

  implicit val format: Format[AccountId] =
    Format(Reads.StringReads.map(AccountId.apply),
           Writes(
             id => JsString(id.value)
           )
    )

  private val random = new Random()

  @tailrec
  final def generate(existingIds: Set[String]): AccountId = {

    val accountId = AccountId(
      (1 to 10)
        .map(
          _ => random.nextInt(10)
        )
        .mkString
    )

    if (existingIds.contains(accountId.value)) {
      generate(existingIds)
    } else {
      accountId
    }
  }
}
