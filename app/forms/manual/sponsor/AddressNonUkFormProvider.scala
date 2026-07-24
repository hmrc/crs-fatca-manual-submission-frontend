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

package forms.manual.sponsor

import forms.mappings.Mappings
import models.AddressNonUk
import play.api.data.Form
import play.api.data.Forms.*
import utils.RegexConstants.{nonUkAddressRegex, nonUkPostcodeRegex}

import javax.inject.Inject

class AddressNonUkFormProvider @Inject() extends Mappings {

  private val addressLineLength = 200
  private val postcodeLength    = 20

  private def doesNotContainDoubleDash(value: String): Boolean =
    !value.contains("--")

  def apply(): Form[AddressNonUk] =
    Form(
      mapping(
        "addressLine1" ->
          validatedText(
            requiredKey = "addressNonUk.error.addressLine1.required",
            invalidKey = "addressNonUk.error.addressLine1.invalid",
            lengthKey = "addressNonUk.error.addressLine1.length",
            regex = nonUkAddressRegex,
            maxLength = addressLineLength
          ).verifying(
            "addressNonUk.error.addressLine1.invalidCombination",
            doesNotContainDoubleDash
          ),
        "addressLine2" ->
          validatedOptionalText(
            invalidKey = "addressNonUk.error.addressLine2.invalid",
            invalidCombinationKey = "addressNonUk.error.addressLine2.invalidCombination",
            lengthKey = "addressNonUk.error.addressLine2.length",
            regex = nonUkAddressRegex,
            maxLength = addressLineLength
          ),
        "addressLine3" ->
          validatedText(
            requiredKey = "addressNonUk.error.addressLine3.required",
            invalidKey = "addressNonUk.error.addressLine3.invalid",
            lengthKey = "addressNonUk.error.addressLine3.length",
            regex = nonUkAddressRegex,
            maxLength = addressLineLength
          ).verifying(
            "addressNonUk.error.addressLine3.invalidCombination",
            doesNotContainDoubleDash
          ),
        "addressLine4" ->
          validatedOptionalText(
            invalidKey = "addressNonUk.error.addressLine4.invalid",
            invalidCombinationKey = "addressNonUk.error.addressLine4.invalidCombination",
            lengthKey = "addressNonUk.error.addressLine4.length",
            regex = nonUkAddressRegex,
            maxLength = addressLineLength
          ),
        "postcode" ->
          validatedOptionalText(
            invalidKey = "addressNonUk.error.postcode.invalid",
            invalidCombinationKey = "addressNonUk.error.postcode.invalidCombination",
            lengthKey = "addressNonUk.error.postcode.length",
            regex = nonUkPostcodeRegex,
            maxLength = postcodeLength
          ),
        "country" ->
          text(
            "addressNonUk.error.country.required"
          )
      )(AddressNonUk.apply)(
        address =>
          Some(
            address.addressLine1,
            address.addressLine2,
            address.addressLine3,
            address.addressLine4,
            address.postcode,
            address.country
          )
      )
    )
}
