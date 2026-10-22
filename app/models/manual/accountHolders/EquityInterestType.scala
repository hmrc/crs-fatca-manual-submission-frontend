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

package models.manual.accountHolders

import models.{Enumerable, WithName}
import play.api.libs.json.*

sealed trait EquityInterestType

object EquityInterestType {

  case object SettlorOfTrust extends WithName("settlorOfTrust") with EquityInterestType
  case object TrusteeOfTrust extends WithName("trusteeOfTrust") with EquityInterestType
  case object ProtectorOfTrust extends WithName("protectorOfTrust") with EquityInterestType
  case object BeneficiaryOfTrust extends WithName("beneficiaryOfTrust") with EquityInterestType
  case object OtherTrustEquityInterestHolder extends WithName("otherTrustEquityInterestHolder") with EquityInterestType
  case object SettlorEquivalent extends WithName("settlorEquivalent") with EquityInterestType
  case object TrusteeEquivalent extends WithName("trusteeEquivalent") with EquityInterestType
  case object ProtectorEquivalent extends WithName("protectorEquivalent") with EquityInterestType
  case object BeneficiaryEquivalent extends WithName("beneficiaryEquivalent") with EquityInterestType
  case object OtherEquivalent extends WithName("otherEquivalent") with EquityInterestType

  val values: Seq[EquityInterestType] = Seq(
    SettlorOfTrust,
    TrusteeOfTrust,
    ProtectorOfTrust,
    BeneficiaryOfTrust,
    OtherTrustEquityInterestHolder,
    SettlorEquivalent,
    TrusteeEquivalent,
    ProtectorEquivalent,
    BeneficiaryEquivalent,
    OtherEquivalent
  )

  implicit val enumerable: Enumerable[EquityInterestType] =
    Enumerable(
      values.map(
        value => value.toString -> value
      )*
    )

  implicit val format: Format[EquityInterestType] = new Format[EquityInterestType] {
    override def reads(json: JsValue): JsResult[EquityInterestType] = json match {
      case JsString(value) => values.find(_.toString == value).map(JsSuccess(_)).getOrElse(JsError("Invalid equity interest type"))
      case _               => JsError("Invalid equity interest type")
    }

    override def writes(value: EquityInterestType): JsValue = JsString(value.toString)
  }
}

case class EquityInterestTypes(types: Seq[EquityInterestType])

object EquityInterestTypes {
  implicit val format: OFormat[EquityInterestTypes] = Json.format[EquityInterestTypes]
}
