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
import play.api.data.FormError
import utils.RegexConstants

class IndividualNameFormProviderSpec extends StringFieldBehaviours {

  val form                    = new IndividualNameFormProvider()()
  val allowedChars            = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789&'\\^` "
  val allowedSeq: Seq[String] = allowedChars.map(_.toString) :+ "-"

  ".FirstName" - {

    val fieldName     = "FirstName"
    val requiredKey   = "individualName.error.FirstName.required"
    val lengthKey     = "individualName.error.FirstName.length"
    val invalidKey    = "individualName.error.FirstName.invalid"
    val doubleDashKey = "individualName.error.FirstName.doubledash"
    val maxLength     = 200

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsIncludeSpecificValuesWithMaxLength(maxLength, allowedChars)
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

    behave like fieldWithIncludedChars(
      form,
      fieldName,
      allowedChars = allowedSeq,
      invalidErr = FormError(fieldName, invalidKey, Seq(RegexConstants.DEFAULT_STRING_FIELD_VALID))
    )

    "has double dash throw error" in {
      val result = form.bind(Map(fieldName -> "test--test")).apply(fieldName)
      result.errors must contain only FormError(fieldName, doubleDashKey, Seq(RegexConstants.DOUBLE_DASH_INVALID))
    }

    "has multiple error - maxchar + double dash" in {
      val maxString = (0 to 200).mkString

      val result = form.bind(Map(fieldName -> s"$maxString--")).apply(fieldName)
      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }
  }

  ".LastName" - {

    val fieldName     = "LastName"
    val requiredKey   = "individualName.error.LastName.required"
    val lengthKey     = "individualName.error.LastName.length"
    val invalidKey    = "individualName.error.LastName.invalid"
    val doubleDashKey = "individualName.error.LastName.doubledash"
    val maxLength     = 200

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsIncludeSpecificValuesWithMaxLength(maxLength, allowedChars)
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

    behave like fieldWithIncludedChars(
      form,
      fieldName,
      allowedChars = allowedSeq,
      invalidErr = FormError(fieldName, invalidKey, Seq(RegexConstants.DEFAULT_STRING_FIELD_VALID))
    )

    "has double dash throw error" in {
      val result = form.bind(Map(fieldName -> "test--test")).apply(fieldName)
      result.errors must contain only FormError(fieldName, doubleDashKey, Seq(RegexConstants.DOUBLE_DASH_INVALID))
    }

    "has multiple error - maxchar + double dash" in {
      val maxString = (0 to 200).mkString

      val result = form.bind(Map(fieldName -> s"$maxString--")).apply(fieldName)
      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }
  }
}
