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

package models

import play.api.libs.json.*

sealed trait AddressModel

case class UkAddress(addressLine1: String, addressLine2: Option[String], city: String, county: Option[String] = None, postcode: String, country: String)
  extends AddressModel {
  val addressFreeFormat: String = List(
    addressLine1,
    addressLine2.getOrElse(""),
    city,
    county.getOrElse(""),
    postcode,
    country
  ).map(_.trim).filterNot(_ == "").mkString(", ")
}

object UkAddress {

  implicit val format: OFormat[UkAddress] = Json.format

  def from(postCode: String) = UkAddress("", None, "", None, postCode, "")
}

case class AddressNonUk(addressLine1: String,
                        addressLine2: Option[String],
                        addressLine3: String,
                        addressLine4: Option[String],
                        postcode: Option[String],
                        country: String
                       ) extends AddressModel {

  val addressFreeFormat: String =  List(
    addressLine1,
    addressLine2.getOrElse(""),
    addressLine3,
    addressLine4.getOrElse(""),
    postcode.getOrElse(""),
    country
  ).map(_.trim).filterNot(_ == "").mkString(", ")

}

object AddressNonUk {

  implicit val format: OFormat[AddressNonUk] = Json.format
}



object AddressModel {



  implicit val addressFormatter: Format[AddressModel] = new Format[AddressModel] {
    override def writes(address: AddressModel): JsValue = address match {
      case uk: UkAddress       => UkAddress.format.writes(uk) ++ Json.obj("type" -> "uk")
      case nonUk: AddressNonUk => AddressNonUk.format.writes(nonUk) ++ Json.obj("type" -> "nonUk")
    }

    override def reads(json: JsValue): JsResult[AddressModel] =
      (json \ "type").validate[String].flatMap {
        case "uk"    => json.validate[UkAddress]
        case "nonUk" => json.validate[AddressNonUk]
        case other   => JsError(s"Unknown address type: $other")
      }

  }
}
