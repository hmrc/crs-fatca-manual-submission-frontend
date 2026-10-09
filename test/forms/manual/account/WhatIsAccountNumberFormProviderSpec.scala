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
import models.NumberType
import models.NumberType.{Iban, Isin, Oban, Osin, Other, Semp}
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import org.scalatest.matchers.should.Matchers.shouldBe

class WhatIsAccountNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "whatIsAccountNumber.error.required"
  val fieldName   = "value"

  def formFor(numType: NumberType, regimeType: RegimeType = CRS) =
    new WhatIsAccountNumberFormProvider()(numType, regimeType)

  private def messages(form: play.api.data.Form[String], value: String) =
    form.bind(Map(fieldName -> value)).errors(fieldName).map(_.message)

  "WhatIsAccountNumberFormProviderSpec" - {
    "Iban" - {

      val form      = formFor(Iban)
      val formFATCA = formFor(Iban, FATCA)

      "must bind a valid IBAN" in {
        val result = form.bind(Map(fieldName -> "GB12ABCD12345678901234"))
        result.hasErrors shouldBe false
        result.hasGlobalErrors shouldBe false
      }

      "cannot be empty" in {
        val result = form.bind(Map(fieldName -> ""))
        result.errors(fieldName).map(_.message) shouldBe Seq(requiredKey)
      }

      "must fail when longer than 34 characters" in {
        val result = form.bind(Map(fieldName -> "A" * 35))
        result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.length.IBAN")
      }

      "must fail when it contains non-alphanumeric characters" in {
        val result = form.bind(Map(fieldName -> "GB12-abcd-1234"))
        result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.characters.IBAN")
      }

      "must fail when the value does not match the IBAN shape" in {
        val result = form.bind(Map(fieldName -> "GBabc"))
        result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.format.IBAN")
      }

      "must fail when the first two characters are not a valid country code" in {
        val result = form.bind(Map(fieldName -> "ZZ12ABCD123456"))
        result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.countryCode.IBAN")
      }

      "bind a lowercase" in {
        val result = form.bind(Map(fieldName -> "gb29abcd12345678901234"))
        result.hasErrors shouldBe false
        result.hasGlobalErrors shouldBe false
      }

      "must fail for XX under CRS but bind under FATCA" in {
        messages(form, "XX12ABCD123456") shouldBe Seq("whatIsAccountNumber.error.invalid.countryCode.IBAN")
        formFATCA.bind(Map(fieldName -> "XX12ABCD123456")).hasErrors shouldBe false
      }

    }

    "Isin" - {

      val form = formFor(Isin)

      "must bind a valid ISIN" in {
        val result = form.bind(Map(fieldName -> "GB1234567890"))
        result.hasErrors shouldBe false
        result.hasGlobalErrors shouldBe false
      }

      "cannot be empty" in {
        val result = form.bind(Map(fieldName -> ""))
        result.errors(fieldName).map(_.message) shouldBe Seq(requiredKey)
      }

      "must fail when the value is not exactly 12 characters" in {
        val result = form.bind(Map(fieldName -> "GB123456789"))
        result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.length.ISIN")
      }

      "must fail when it contains non-alphanumeric characters" in {
        val result = form.bind(Map(fieldName -> "GB00-1234567"))
        result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.characters.ISIN")
      }

      "must fail when the last character is not a digit" in {
        val result = form.bind(Map(fieldName -> "GB123456789X"))
        result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.format.ISIN")
      }

      "must fail when the value does not start with two letters" in {
        val result = form.bind(Map(fieldName -> "123456789012"))
        result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.format.ISIN")
      }

      "must fail when the first two characters are not a valid country code" in {
        val result = form.bind(Map(fieldName -> "ZZ1234567890"))
        result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.countryCode.ISIN")
      }
    }

    Seq(Oban, Osin, Semp, Other).foreach {
      numType =>
        s"$numType" - {

          val form = formFor(numType)

          "must bind a value with no banned sequences" in {
            val result = form.bind(Map(fieldName -> "ACC-123 & Co /2"))
            result.hasErrors shouldBe false
            result.hasGlobalErrors shouldBe false
          }

          "cannot be empty" in {
            val result = form.bind(Map(fieldName -> ""))
            result.errors(fieldName).map(_.message) shouldBe Seq(requiredKey)
          }

          "must fail when longer than 200 characters" in {
            val result = form.bind(Map(fieldName -> "A" * 201))
            result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.length.OTHER")
          }

          "must fail when it contains a double dash" in {
            val result = form.bind(Map(fieldName -> "ACC--123"))
            result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.characters.OTHER")
          }

          "must fail when it contains an ampersand hash" in {
            val result = form.bind(Map(fieldName -> "A&#C"))
            result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.characters.OTHER")
          }

          "must fail when it contains a forward slash asterisk" in {
            val result = form.bind(Map(fieldName -> "notes/*file"))
            result.errors(fieldName).map(_.message) shouldBe Seq("whatIsAccountNumber.error.invalid.characters.OTHER")
          }

          "bind values containing a lone ampersand, slash or dash" in {
            Seq("A&C", "notes/file", "ACC-123").foreach {
              v =>
                val result = form.bind(Map(fieldName -> v))
                result.hasErrors shouldBe false
            }
          }
        }
    }
  }
}
