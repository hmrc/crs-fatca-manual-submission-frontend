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

import forms.behaviours.FieldBehaviours
import models.SubmissionsConstants.*
import models.{Currencies, Currency}
import play.api.data.FormError

class WhatWasTheAccountBalanceFormProviderSpec extends FieldBehaviours {

  private val currency                   = Currencies.all(FATCA).head
  private val requiredCurrencyKey        = "whatWasTheAccountBalance.error.required.currency"
  private val requiredAmountKey          = "whatWasTheAccountBalance.error.required.amount"
  private val minusAmountErrorKey        = "whatWasTheAccountBalance.error.minus.FATCA"
  private val fatcaInvalidFormatErrorKey = "whatWasTheAccountBalance.error.invalid.FATCA"
  private val crsInvalidFormatErrorKey   = "whatWasTheAccountBalance.error.invalid.CRS"

  "Amount field" - {

    val fieldName = "amount"

    "when regime is FATCA" - {

      val form = new WhatWasTheAccountBalanceFormProvider()(FATCA)

      "must bind valid positive numbers" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "123.45"))
        result.value.value.amount mustBe "123.45"
      }

      "must bind valid whole numbers" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "123"))
        result.value.value.amount mustBe "123"
      }

      "must bind valid negative numbers with minus at the start" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "-123.45"))
        result.value.value.amount mustBe "-123.45"
      }

      "must fail when amount contains invalid characters" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "123abc"))
        result.errors must contain(FormError(fieldName, fatcaInvalidFormatErrorKey))
      }

      "must fail when minus sign is misplaced (not at the start)" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "12-34"))
        result.errors must contain(FormError(fieldName, minusAmountErrorKey))
      }

      "must fail when amount has more than 2 decimal places" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "123.456"))
        result.errors must contain(FormError(fieldName, "whatWasTheAccountBalance.error.decimalPlaces"))
      }

      "must fail when amount is empty" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> ""))
        result.errors must contain(FormError(fieldName, requiredAmountKey))
      }

      "must not bind a value with more than one decimal point" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> " 1.2.3 "))
        result.errors must contain(FormError(fieldName, fatcaInvalidFormatErrorKey))
      }

      "must not bind a value without numbers" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> " -.. "))
        result.errors must contain(FormError(fieldName, requiredAmountKey))
      }
    }
    "when regime is CRS" - {

      val form = new WhatWasTheAccountBalanceFormProvider()(CRS)

      "must bind valid positive numbers" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "123.45"))
        result.value.value.amount mustBe "123.45"
      }

      "must bind valid whole numbers" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "123"))
        result.value.value.amount mustBe "123"
      }

      "must fail when amount is negative" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "-123.45"))
        result.errors must contain(FormError(fieldName, crsInvalidFormatErrorKey))
      }

      "must fail when amount contains invalid characters" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "123abc"))
        result.errors must contain(FormError(fieldName, crsInvalidFormatErrorKey))
      }

      "must fail when amount has more than 2 decimal places" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> "123.456"))
        result.errors must contain(FormError(fieldName, "whatWasTheAccountBalance.error.decimalPlaces"))
      }

      "must fail when amount is empty" in {
        val result = form.bind(Map("currency" -> currency.code, fieldName -> ""))
        result.errors must contain(FormError(fieldName, requiredAmountKey))
      }
    }

//    "when normalised" - {
//      val form = new WhatWasTheAccountBalanceFormProvider()(FATCA)
//
//      "must not bind a value without numbers" in {
//        val result = form.bind(Map("currency" -> currency.code, fieldName -> " -.. "))
//        result.errors must contain(FormError(fieldName, requiredAmountKey))
//      }
//
//      "must not bind a value with more than one decimal point" in {
//        val result = form.bind(Map("currency" -> currency.code, fieldName -> " 1.2.3 "))
//        result.errors must contain(FormError(fieldName, crsInvalidFormatErrorKey))
//      }
//
//      "must strip whitespace" in {
//        val result = form.bind(Map("currency" -> currency.code, fieldName -> " 123.45 "))
//        result.value.value.amount mustBe "123.45"
//      }
//
//      "transform trailing decimal to full decimal (e.g 5. = 5.00)" in {
//        val result = form.bind(Map("currency" -> currency.code, fieldName -> " 5. "))
//        result.value.value.amount mustBe "5.00"
//      }
//
//      "transform leading decimal to 0. (e.g .5 = 0.5)" in {
//        val result = form.bind(Map("currency" -> currency.code, fieldName -> " .5 "))
//        result.value.value.amount mustBe "0.5"
//      }
//      "transform leading negative decimal to -0. (e.g -.5 = -0.50)" in {
//        val result = form.bind(Map("currency" -> currency.code, fieldName -> " -.5 "))
//        result.value.value.amount mustBe "-0.50"
//      }
//      "must remove leading zeroes (e.g 005 = 5)" in {
//        val result = form.bind(Map("currency" -> currency.code, fieldName -> "005"))
//        result.value.value.amount mustBe "5"
//      }
//
//    }
  }

  "Currency field" - {

    val fieldName = "currency"

    "must fail when currency is empty" in {
      val form   = new WhatWasTheAccountBalanceFormProvider()(FATCA)
      val result = form.bind(Map(fieldName -> "", "amount" -> "123"))
      result.errors must contain(FormError(fieldName, requiredCurrencyKey))
    }
    "when regime is FATCA" - {
      "must include VED" in {
        val form   = new WhatWasTheAccountBalanceFormProvider()(FATCA)
        val result = form.bind(Map(fieldName -> "VED", "amount" -> "123"))
        result.value.value.currency mustBe Currency("VED", "Venezuelan Bolivar (VED)")
      }
    }
    "when regime is CRS" - {
      "must NOT include VED" in {
        val form   = new WhatWasTheAccountBalanceFormProvider()(CRS)
        val result = form.bind(Map(fieldName -> "VED", "amount" -> "123"))
        result.errors must contain(FormError(fieldName, requiredCurrencyKey))
      }
    }
  }
}
