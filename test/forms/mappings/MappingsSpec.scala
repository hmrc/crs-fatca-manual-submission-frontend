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

package forms.mappings

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}
import models.Enumerable
import utils.RegexConstants.{POSTCODE_FORMAT, POSTCODE_VALID}

object MappingsSpec {

  sealed trait Foo
  case object Bar extends Foo
  case object Baz extends Foo

  object Foo {

    val values: Set[Foo] = Set(Bar, Baz)

    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(
        values.toSeq.map(
          v => v.toString -> v
        ): _*
      )
  }
}

class MappingsSpec extends AnyFreeSpec with Matchers with OptionValues with Mappings {

  import MappingsSpec._

  "text" - {

    val testForm: Form[String] =
      Form(
        "value" -> text()
      )

    "must bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "must not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a string of whitespace only" in {
      val result = testForm.bind(Map("value" -> " \t"))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must return a custom error message" in {
      val form   = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "boolean" - {

    val testForm: Form[Boolean] =
      Form(
        "value" -> boolean()
      )

    "must bind true" in {
      val result = testForm.bind(Map("value" -> "true"))
      result.get mustEqual true
    }

    "must bind false" in {
      val result = testForm.bind(Map("value" -> "false"))
      result.get mustEqual false
    }

    "must not bind a non-boolean" in {
      val result = testForm.bind(Map("value" -> "not a boolean"))
      result.errors must contain(FormError("value", "error.boolean"))
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind" in {
      val result = testForm.fill(true)
      result.apply("value").value.value mustEqual "true"
    }
  }

  "int" - {

    val testForm: Form[Int] =
      Form(
        "value" -> int()
      )

    "must bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
  }

  "enumerable" - {

    val testForm = Form(
      "value" -> enumerable[Foo]()
    )

    "must bind a valid option" in {
      val result = testForm.bind(Map("value" -> "Bar"))
      result.get mustEqual Bar
    }

    "must not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      result.errors must contain(FormError("value", "error.invalid"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }
  }

  "currency" - {

    val testForm: Form[BigDecimal] =
      Form(
        "value" -> currency()
      )

    "must bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "must bind a valid decimal with 1 decimal place" in {
      val result = testForm.bind(Map("value" -> "1.2"))
      result.get mustEqual 1.2
    }

    "must bind a valid decimal with 2 decimal places" in {
      val result = testForm.bind(Map("value" -> "1.23"))
      result.get mustEqual 1.23
    }

    "must bind a valid number with spaces, commas and `£` characters" in {
      val result = testForm.bind(Map("value" -> "£ 1,234 . 01"))
      result.get mustEqual 1234.01
    }

    "must not bind values with a `£` after any numbers" in {
      val result = testForm.bind(Map("value" -> "123 £456"))
      result.errors must contain only FormError("value", "error.nonNumeric")
    }

    "must not bind values with non-numeric characters except commas, spaces and `£`s" in {
      val result = testForm.bind(Map("value" -> "abc"))
      result.errors must contain only FormError("value", "error.nonNumeric")
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a number with more than 2 decimal places" in {
      val result = testForm.bind(Map("value" -> "1.234"))
      result.errors must contain only FormError("value", "error.invalidNumeric")
    }

    "must not bind negative numbers" in {
      val result = testForm.bind(Map("value" -> "-1"))
      result.errors must contain only FormError("value", "error.nonNumeric")
    }

    "must unbind a valid value" in {
      val result = testForm.fill(1)
      result.apply("value").value.value mustEqual "1"
    }
  }

  "postcode" - {

    val testForm: Form[String] =
      Form(
        "postCode" -> mandatoryPostcode(
          "uKPostcode.error.required",
          "uKPostcode.error.length",
          POSTCODE_VALID,
          "uKPostcode.error.invalid",
          POSTCODE_FORMAT,
          "uKPostcode.error.format"
        )
      )

    "must return required error" in {
      val expectedError = Seq(FormError("postCode", "uKPostcode.error.required"))
      val result        = testForm.bind(Map("postCode" -> ""))
      result.errors mustEqual expectedError
    }

    "must return length error" in {
      val expectedError = Seq(FormError("postCode", "uKPostcode.error.length"))
      val result        = testForm.bind(Map("postCode" -> "12345678901"))
      result.errors mustEqual expectedError
    }

    "must return invalid error" - {

      Seq(
        "SW1A-1AA", // hyphen
        "SW1A_1AA", // underscore
        "SW1A@1AA", // @
        "SW1A#1AA", // #
        "SW1A£1AA", // pound sign
        "SW1A%1AA", // %
        "SW1A&1AA", // &
        "SW1A(1AA", // (
        "SW1A)1AA", // )
        "SW1A/1AA", // /
        "SW1A\\1AA", // backslash
        "SW1A,1AA", // comma
        "SW1A.1AA", // dot
        "SW1A:1AA", // colon
        "SW1A;1AA", // semicolon
        "SW1A'1AA", // apostrophe
        "SW1A\"1AA", // double quote
        "SW1A?1AA", // question mark
        "SW1A!1AA", // exclamation mark
        "SW1A\t1AA", // tab
        "SW1A\n1AA", // newline
        "SW1A\r1AA" // carriage return
      ).foreach {
        postcode =>
          s"for postcode [$postcode]" in {
            val result = testForm.bind(Map("postCode" -> postcode))

            result.errors mustEqual Seq(
              FormError("postCode", "uKPostcode.error.invalid")
            )
          }
      }

    }

    "must return format error" - {
      Seq(
        "12345", // starts with digits
        "ABC 123", // wrong format
        "A", // too short
        "AA", // too short
        "SW1", // missing inward code
        "SW1A", // missing inward code
        "SW1A 1", // missing letters
        "SW1A 1A", // only one trailing letter
        "SW1A 11A", // second character in inward code must be a letter
        "SW1A 111", // ends with digits instead of letters
        "SW1A AA1", // inward code starts with letters instead of digit
        "SW1A 1AAA", // too many trailing letters
        "SW1AA 1AA", // too many characters in outward code
        "SWA1 1AA", // missing required digit in outward code
        "ABCD 1AA", // too many leading letters
        "1A 1AA", // starts with digit
        "A123 1AA", // outward code structure invalid
        "SW 1AA", // missing digit before space
        "SW11A AA", // inward code missing digit
        "SW1A A1A" // inward code format invalid
      ).foreach {
        postcode =>
          s"for postcode [$postcode]" in {
            val result = testForm.bind(Map("postCode" -> postcode))

            result.errors mustEqual Seq(
              FormError("postCode", "uKPostcode.error.format")
            )
          }
      }

    }
  }
}
