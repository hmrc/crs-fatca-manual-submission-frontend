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
import models.IndividualName
import models.SubmissionsConstants.RegimeType
import utils.RegexConstants.{DEFAULT_ALPHA_FIELD_VALID, DOUBLE_DASH_INVALID}

class IndividualNameFormProvider @Inject() extends Mappings {
  private val nameLineLength = 200

  private def doesNotContainDoubleDash(value: String): Boolean =
    value.matches(DOUBLE_DASH_INVALID)

  def apply(regimeType: RegimeType): Form[IndividualName] = {
    val regime = regimeType.toString.toLowerCase
    Form(
      mapping(
        "firstName" -> validatedText(
          requiredKey = s"cpso.individualName.$regime.error.firstName.required",
          invalidKey = s"cpso.individualName.$regime.error.firstName.invalid.characters",
          lengthKey = s"cpso.individualName.$regime.error.firstName.length",
          regex = DEFAULT_ALPHA_FIELD_VALID,
          maxLength = nameLineLength
        ).verifying(
          s"cpso.individualName.$regime.error.firstName.invalid.characters.combination",
          doesNotContainDoubleDash
        ),
        "lastName" -> validatedText(
          requiredKey = s"cpso.individualName.$regime.error.lastName.required",
          invalidKey = s"cpso.individualName.$regime.error.lastName.invalid.characters",
          lengthKey = s"cpso.individualName.$regime.error.lastName.length",
          regex = DEFAULT_ALPHA_FIELD_VALID,
          maxLength = nameLineLength
        ).verifying(
          s"cpso.individualName.$regime.error.lastName.invalid.characters.combination",
          doesNotContainDoubleDash
        )
      )(IndividualName.apply)(
        x => Some((x.firstName, x.lastName))
      )
    )
  }
}
