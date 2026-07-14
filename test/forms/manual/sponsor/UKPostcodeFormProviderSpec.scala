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

import forms.behaviours.StringFieldBehaviours
import forms.manual.sponsor.UKPostcodeFormProvider
import play.api.data.FormError

class UKPostcodeFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "uKPostcode.error.required"
  val lengthKey   = "uKPostcode.error.length"
  val maxLength   = 10

  val form = new UKPostcodeFormProvider()()

  ".postCode" - {

    val fieldName = "postCode"

    "bind valid data" in {
      val testPostCode = "ZZ1 1ZZ"
      val result       = form.bind(Map(fieldName -> testPostCode)).apply(fieldName)
      result.value.value mustBe testPostCode
      result.errors mustBe empty
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
