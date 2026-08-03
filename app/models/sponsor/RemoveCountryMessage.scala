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

package models.sponsor

val otherCountryCodes = Seq("XX")
val pluralCountryCodes = Seq("CC", "CF", "DO", "MH", "AE", "GB", "UM", "US", "VI", "AX", "CG","CG", "CK", "FK", "FO", "GS", "HM", "IO", "KY", "MP", "NL", "PH", "PN", "PS", "SB", "TC", "TF", "VG", "WF", "IM" )

enum RemoveCountryMessage:
  case OtherCountryMessage, AllCountryMessage, PluralCountryNameMessage

object RemoveCountryMessage {

  def getRemoveCountryMessage(code: String): RemoveCountryMessage = code match
    case c if otherCountryCodes.contains(c)  => OtherCountryMessage
    case c if pluralCountryCodes.contains(c) => PluralCountryNameMessage
    case _                                   => AllCountryMessage

}

