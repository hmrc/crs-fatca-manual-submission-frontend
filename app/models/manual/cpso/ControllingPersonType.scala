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

package models.manual.cpso

import models.{Enumerable, WithName}
import play.api.libs.json.*

sealed trait ControllingPersonType

object ControllingPersonType extends Enumerable.Implicits {

  case object OwnershipOfLegalPerson extends WithName("ownershipOfLegalPerson") with ControllingPersonType
  case object OtherMeansOfControl extends WithName("otherMeansOfControl") with ControllingPersonType
  case object SeniorManagingOfficial extends WithName("seniorManagingOfficial") with ControllingPersonType
  case object SettlorOfTrust extends WithName("settlorOfTrust") with ControllingPersonType
  case object TrusteeOfTrust extends WithName("trusteeOfTrust") with ControllingPersonType
  case object ProtectorOfTrust extends WithName("protectorOfTrust") with ControllingPersonType
  case object BeneficiaryOfTrust extends WithName("beneficiaryOfTrust") with ControllingPersonType
  case object OtherControllingPersonOfTrust extends WithName("otherControllingPersonOfTrust") with ControllingPersonType
  case object SettlorEquivalent extends WithName("settlorEquivalent") with ControllingPersonType
  case object TrusteeEquivalent extends WithName("trusteeEquivalent") with ControllingPersonType
  case object ProtectorEquivalent extends WithName("protectorEquivalent") with ControllingPersonType
  case object BeneficiaryEquivalent extends WithName("beneficiaryEquivalent") with ControllingPersonType
  case object OtherLegalArrangementEquivalent extends WithName("otherLegalArrangementEquivalent") with ControllingPersonType
  case object NotReported extends WithName("notReported") with ControllingPersonType

  val values: Seq[ControllingPersonType] = Seq(
    OwnershipOfLegalPerson,
    OtherMeansOfControl,
    SeniorManagingOfficial,
    SettlorOfTrust,
    TrusteeOfTrust,
    ProtectorOfTrust,
    BeneficiaryOfTrust,
    OtherControllingPersonOfTrust,
    SettlorEquivalent,
    TrusteeEquivalent,
    ProtectorEquivalent,
    BeneficiaryEquivalent,
    OtherLegalArrangementEquivalent,
    NotReported
  )

  implicit val enumerable: Enumerable[ControllingPersonType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      )*
    )

  implicit val format: Format[ControllingPersonType] = Format(
    Reads[ControllingPersonType] {
      case JsString(value) =>
        values.find(_.toString == value).map(JsSuccess(_)).getOrElse(JsError("Invalid controlling person type"))
      case _ => JsError("Invalid controlling person type")
    },
    Writes[ControllingPersonType](
      value => JsString(value.toString)
    )
  )
}
