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
import utils.RegexConstants
import utils.RegexConstants.{ukAddressRegex, POSTCODE_FORMAT, POSTCODE_VALID}

class UkAddressFormProvider @Inject() extends Mappings {
  private val addressLineLength = 200

  private def doesNotContainDoubleDash(value: String): Boolean =
    value.matches(RegexConstants.DOUBLE_DASH_INVALID)

  def apply(): Form[UkAddress] = Form(
    mapping(
      "addressLine1" ->
        validatedText(
          requiredKey = "ukAddress.error.addressLine1.required",
          invalidKey = "ukAddress.error.addressLine1.invalid.characters",
          lengthKey = "ukAddress.error.addressLine1.length",
          regex = ukAddressRegex,
          maxLength = addressLineLength
        ).verifying(
          "ukAddress.error.addressLine1.invalid.characters.combination",
          doesNotContainDoubleDash
        ),
      "addressLine2" ->
        validatedOptionalText(
          invalidKey = "ukAddress.error.addressLine2.invalid.characters",
          invalidCombinationKey = "ukAddress.error.addressLine2.invalid.characters.combination",
          lengthKey = "ukAddress.error.addressLine2.length",
          regex = ukAddressRegex,
          maxLength = addressLineLength
        ),
      "city" ->
        validatedText(
          requiredKey = "ukAddress.error.city.required",
          invalidKey = "ukAddress.error.city.invalid.characters",
          lengthKey = "ukAddress.error.city.length",
          regex = ukAddressRegex,
          maxLength = addressLineLength
        ).verifying(
          "ukAddress.error.city.invalid.characters.combination",
          doesNotContainDoubleDash
        ),
      "county" ->
        validatedOptionalText(
          invalidKey = "ukAddress.error.county.invalid.characters",
          invalidCombinationKey = "ukAddress.error.county.invalid.characters.combination",
          lengthKey = "ukAddress.error.county.length",
          regex = ukAddressRegex,
          maxLength = addressLineLength
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
