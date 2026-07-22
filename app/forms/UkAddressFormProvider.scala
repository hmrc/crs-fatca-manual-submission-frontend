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

package forms

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.*
import models.UkAddress
import utils.RegexConstants.{POSTCODE_FORMAT, POSTCODE_VALID}

class UkAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[UkAddress] = Form(
    mapping(
      "addressLine1" -> mandatoryAddress(
        "ukAddress.error.addressLine1.required",
        "ukAddress.error.addressLine1.length",
        "ukAddress.error.addressLine1.invalid.characters",
        "ukAddress.error.addressLine1.invalid.characters.combination"
      ),
      "addressLine2" -> optionalAddress(
        "ukAddress.error.addressLine2.length",
        "ukAddress.error.addressLine2.invalid.characters",
        "ukAddress.error.addressLine2.invalid.characters.combination"
      ),
      "city" -> mandatoryAddress(
        "ukAddress.error.city.required",
        "ukAddress.error.city.length",
        "ukAddress.error.city.invalid.characters",
        "ukAddress.error.city.invalid.characters.combination"
      ),
      "county" ->
        optionalAddress(
          "ukAddress.error.county.length",
          "ukAddress.error.county.invalid.characters",
          "ukAddress.error.county.invalid.characters.combination"
        ),
      "postCode" -> mandatoryPostcode(
        "ukAddress.error.postCode.required",
        "ukAddress.error.postCode.length",
        POSTCODE_VALID,
        "ukAddress.error.postCode.invalid",
        POSTCODE_FORMAT,
        "ukAddress.error.postCode.format"
      ),
      "country" -> text("ukAddress.error.country.required")
    )(UkAddress.apply)(
      x => Some((x.addressLine1, x.addressLine2, x.city, x.county, x.postcode, x.country))
    )
  )
}
