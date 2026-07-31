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

package forms.manual.account

import forms.behaviours.StringFieldBehaviours
import models.Currency
import models.SubmissionsConstants.{CRS, FATCA}
import play.api.data.FormError

class WhatWasTheAccountCurrencyFormProviderSpec extends StringFieldBehaviours {

  private val requiredCurrencyKey = "whatWasTheAccountBalance.error.required.currency"

  "Currency field" - {

    val fieldName = "currency"

    "must fail when currency is empty" in {
      val form   = new WhatWasTheAccountCurrencyFormProvider()(CRS)
      val result = form.bind(Map.empty[String, String])
      result.errors must contain(FormError(fieldName, requiredCurrencyKey))
    }

    "when regime is FATCA" - {
      "must include VED" in {
        val form   = new WhatWasTheAccountCurrencyFormProvider()(FATCA)
        val result = form.bind(Map(fieldName -> "VED"))
        result.value mustBe Some(Currency("VED", "Venezuelan Bolivar (VED)"))
      }
    }

    "when regime is CRS" - {
      "must NOT include VED" in {
        val form   = new WhatWasTheAccountCurrencyFormProvider()(CRS)
        val result = form.bind(Map(fieldName -> "VED"))
        result.errors must contain(FormError(fieldName, requiredCurrencyKey))
      }
    }
  }
}
