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

package forms.manual.accountHolders

import forms.behaviours.StringFieldBehaviours
import forms.manual.accountHolders.IndividualNameFormProvider
import play.api.data.FormError

class IndividualNameFormProviderSpec extends StringFieldBehaviours {

  val form = new IndividualNameFormProvider()()

  ".FirstName" - {

    val fieldName = "FirstName"
    val requiredKey = "individualName.error.FirstName.required"
    val lengthKey = "individualName.error.FirstName.length"
    val maxLength = 200

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".LastName" - {

    val fieldName = "LastName"
    val requiredKey = "individualName.error.LastName.required"
    val lengthKey = "individualName.error.LastName.length"
    val maxLength = 200

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
