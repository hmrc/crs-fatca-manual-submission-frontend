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

import forms.behaviours.BooleanFieldBehaviours
import models.SubmissionsConstants.{CRS, FATCA}
import play.api.data.FormError

class CPSOWhereAreTheyBasedFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKeyCrs   = "cpso.whereAreTheyBased.error.required.CRS"
  val requiredKeyFatca = "cpso.whereAreTheyBased.error.required.FATCA"
  val invalidKey       = "error.boolean"

  val form = new CPSOWhereAreTheyBasedFormProvider()(CRS)

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )
    "when regime is CRS" - {
      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKeyCrs)
      )
    }
    "when regime is FATCA" - {
      val form = new CPSOWhereAreTheyBasedFormProvider()(FATCA)

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKeyFatca)
      )
    }

  }
}
