/*
 * Copyright 2023 HM Revenue & Customs
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

import models.UkAddress
import play.api.libs.json.*

case class Address(uprn: Option[Long],
                   addressLine1: String,
                   addressLine2: Option[String],
                   addressLine3: Option[String],
                   addressLine4: Option[String],
                   town: String,
                   postCode: Option[String],
                   country: Country
) {

  lazy val formatAsSeq: Seq[String] = Seq(
    Some(addressLine1),
    addressLine2,
    addressLine3,
    addressLine4,
    Some(town),
    postCode,
    if (isOtherCountry) Some(country.description) else None
  ).flatten

  val isGB: Boolean           = this.country.code == Address.GBCountryCode
  val isOtherCountry: Boolean = this.country.code != Address.GBCountryCode

  def ukAddress: UkAddress =
    UkAddress(addressLine1 = addressLine1, addressLine2 = addressLine2, city = town, county = None, postcode = postCode.getOrElse(""), country = country.code)
}

object Address {
  val GBCountryCode = "GB"

  implicit val format: OFormat[Address] = Json.format[Address]
}
