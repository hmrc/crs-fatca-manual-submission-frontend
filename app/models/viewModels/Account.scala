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

import models.NumberType
import play.api.libs.json.{Json, OFormat}

case class Account(haveNumber: Option[Boolean] = None,
                   identifier: Option[String] = None,
                   numberType: Option[NumberType] = None,
                   whatIsAccountNumber: Option[String] = None,
                   accountId: AccountId
) {

  def accountNumber: Option[String] =
    (haveNumber, identifier, whatIsAccountNumber) match {
      case (Some(false), Some(value), None) => Some(value)
      case (Some(true), None, Some(value))  => Some(value)
      case (_, _, _)                        => None
    }
}

object Account {
  implicit val format: OFormat[Account] = Json.format[Account]
}
