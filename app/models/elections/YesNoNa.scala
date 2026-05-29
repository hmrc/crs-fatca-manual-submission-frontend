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

enum YesNoNa:
  case Yes, No, NA

object YesNoNa:

  given Format[YesNoNa] = Format(
    Reads {
      case JsString("yes") => JsSuccess(Yes)
      case JsString("no")  => JsSuccess(No)
      case JsString("na")  => JsSuccess(NA)
      case other           => JsError(s"Unknown value: $other")
    },
    Writes(
      v =>
        JsString(v match
          case Yes => "yes"
          case No  => "no"
          case NA  => "na"
        )
    )
  )
