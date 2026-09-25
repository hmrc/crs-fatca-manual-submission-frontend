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
import models.manual.cpso.IndividualPlaceOfBirth
import play.api.data.{Form, Forms}
import play.api.data.Forms.*
import utils.RegexConstants.nonUkAddressRegex

import javax.inject.Inject

class IndividualPlaceOfBirthFormProvider @Inject() extends Mappings {

  private val maxLength = 200

  def apply(): Form[IndividualPlaceOfBirth] = Form(
    mapping(
      "city" -> validatedOptionalText(
        invalidKey = "cpso.individualPlaceOfBirth.error.city.invalid",
        invalidCombinationKey = "cpso.individualPlaceOfBirth.error.city.invalidCombination",
        lengthKey = "cpso.individualPlaceOfBirth.error.city.length",
        regex = nonUkAddressRegex,
        maxLength = maxLength
      ),
      "region" -> validatedOptionalText(
        invalidKey = "cpso.individualPlaceOfBirth.error.region.invalid",
        invalidCombinationKey = "cpso.individualPlaceOfBirth.error.region.invalidCombination",
        lengthKey = "cpso.individualPlaceOfBirth.error.region.length",
        regex = nonUkAddressRegex,
        maxLength = maxLength
      ),
      "country" -> text("cpso.individualPlaceOfBirth.error.country.required")
    )(
      (city: Option[String], region: Option[String], country: String) => IndividualPlaceOfBirth.apply(city, region, country)
    )(
      x => Some((x.city, x.region, x.country))
    )
  )
}
