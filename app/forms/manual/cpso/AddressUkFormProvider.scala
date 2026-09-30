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

package forms.manual.cpso

import forms.mappings.Mappings
import models.UkAddress
import play.api.data.Form
import play.api.data.Forms.*
import utils.RegexConstants.{DOUBLE_DASH_INVALID, POSTCODE_FORMAT, POSTCODE_VALID, ukAddressRegex}

import javax.inject.Inject

class AddressUkFormProvider @Inject() extends Mappings {

  private val addressLineLength = 200

  private def doesNotContainDoubleDash(value: String): Boolean =
    value.matches(DOUBLE_DASH_INVALID)

  def apply(): Form[UkAddress] = Form(
    mapping(
      "addressLine1" ->
        validatedText(
          requiredKey = "cpso.addressUk.error.addressLine1.required",
          invalidKey = "cpso.addressUk.error.addressLine1.invalid.characters",
          lengthKey = "cpso.addressUk.error.addressLine1.length",
          regex = ukAddressRegex,
          maxLength = addressLineLength
        ).verifying(
          "cpso.addressUk.error.addressLine1.invalid.characters.combination",
          doesNotContainDoubleDash
        ),
      "addressLine2" ->
        validatedOptionalText(
          invalidKey = "cpso.addressUk.error.addressLine2.invalid.characters",
          invalidCombinationKey = "cpso.addressUk.error.addressLine2.invalid.characters.combination",
          lengthKey = "cpso.addressUk.error.addressLine2.length",
          regex = ukAddressRegex,
          maxLength = addressLineLength
        ),
      "city" ->
        validatedText(
          requiredKey = "cpso.addressUk.error.city.required",
          invalidKey = "cpso.addressUk.error.city.invalid.characters",
          lengthKey = "cpso.addressUk.error.city.length",
          regex = ukAddressRegex,
          maxLength = addressLineLength
        ).verifying(
          "cpso.addressUk.error.city.invalid.characters.combination",
          doesNotContainDoubleDash
        ),
      "county" ->
        validatedOptionalText(
          invalidKey = "cpso.addressUk.error.county.invalid.characters",
          invalidCombinationKey = "cpso.addressUk.error.county.invalid.characters.combination",
          lengthKey = "cpso.addressUk.error.county.length",
          regex = ukAddressRegex,
          maxLength = addressLineLength
        ),
      "postCode" -> mandatoryPostcode(
        "cpso.addressUk.error.postCode.required",
        "cpso.addressUk.error.postCode.length",
        POSTCODE_VALID,
        "cpso.addressUk.error.postCode.invalid",
        POSTCODE_FORMAT,
        "cpso.addressUk.error.postCode.format"
      ),
      "country" -> text("accountHolders.ukAddress.error.country.required")
    )(UkAddress.apply)(
      x => Some((x.addressLine1, x.addressLine2, x.city, x.county, x.postcode, x.country))
    )
  )
}
