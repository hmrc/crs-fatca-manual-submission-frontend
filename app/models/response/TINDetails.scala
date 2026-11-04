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

import models.response.TINType._
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

final case class TINDetails(TINType: TINType, TIN: String, IssuedBy: String) {

  def toKey(implicit messages: Messages): String = TINType match {
    case UTR   => messages("CheckDetailsForFinancialInstitution.tinType.utr")
    case CRN   => messages("CheckDetailsForFinancialInstitution.tinType.crn")
    case TURN  => messages("CheckDetailsForFinancialInstitution.tinType.turn")
    case OTHER => messages("CheckDetailsForFinancialInstitution.tinType.other")
  }
}

object TINDetails:
  given format: OFormat[TINDetails] = Json.format[TINDetails]
