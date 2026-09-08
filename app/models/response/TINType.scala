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

package models.response

import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Reads, Writes}

sealed trait TINType

object TINType {

  case object UTR extends TINType
  case object CRN extends TINType
  case object TURN extends TINType
  case object OTHER extends TINType

  val allValues: Seq[TINType] = Seq(UTR, CRN, TURN, OTHER)

  private val stringMapping: Map[String, TINType] = allValues
    .map(
      t => t.toString -> t
    )
    .toMap

  private val reverseMapping: Map[TINType, String] = stringMapping.map(_.swap)

  implicit val reads: Reads[TINType] = Reads {
    case JsString(str) =>
      stringMapping
        .get(str)
        .map(JsSuccess(_))
        .getOrElse(JsError(s"Invalid TINType: $str"))
    case _ => JsError("Invalid TINType")
  }

  implicit val writes: Writes[TINType] = Writes {
    tinType =>
      JsString(reverseMapping(tinType))
  }

  implicit val format: Format[TINType] = Format(reads, writes)
}
