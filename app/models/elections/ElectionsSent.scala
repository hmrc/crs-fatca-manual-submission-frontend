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

package models.elections

import play.api.libs.json.*

enum RegimeType:
  case CRS, FATCA

object RegimeType:

  implicit val reads: Reads[RegimeType] =
    Reads {
      case JsString(value) =>
        try
          JsSuccess(RegimeType.valueOf(value))
        catch {
          case _: IllegalArgumentException => JsError(s"Invalid RegimeType: $value")
        }
      case _ => JsError("RegimeType must be a string")
    }

  implicit val writes: Writes[RegimeType] = Writes(
    r => JsString(r.toString)
  )

  implicit val format: Format[RegimeType] = Format(reads, writes)

case class ElectionsSent(regime: RegimeType, reportingYear: Int, fiName: String)

object ElectionsSent:
  implicit val format: OFormat[ElectionsSent] = Json.format[ElectionsSent]
