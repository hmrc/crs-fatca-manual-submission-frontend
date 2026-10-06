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

import forms.behaviours.StringFieldBehaviours
import models.SubmissionsConstants.{CRS, FATCA}
import org.scalacheck.Gen
import play.api.data.FormError

class SelectAddressFormProviderSpec extends StringFieldBehaviours {

  ".value crs" - {
    val form        = new SelectAddressFormProvider()(CRS)
    val fieldName   = "value"
    val requiredKey = "cpso.crs.selectAddress.error.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.const("1 Address line 1 Road, Address line 2 Road Town, zz11zz")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".value fatca" - {
    val form        = new SelectAddressFormProvider()(FATCA)
    val fieldName   = "value"
    val requiredKey = "cpso.fatca.selectAddress.error.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.const("1 Address line 1 Road, Address line 2 Road Town, zz11zz")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
