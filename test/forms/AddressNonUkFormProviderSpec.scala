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

import forms.behaviours.StringFieldBehaviours
import forms.manual.sponsor.AddressNonUkFormProvider
import models.AddressNonUk
import org.scalatest.OptionValues
import play.api.data.FormError

class AddressNonUkFormProviderSpec extends StringFieldBehaviours with OptionValues {

  private val form = new AddressNonUkFormProvider()()

  private val validData = Map(
    "addressLine1" -> "1 Test Street",
    "addressLine2" -> "Test Building",
    "addressLine3" -> "Paris",
    "addressLine4" -> "Ile de France",
    "postcode"     -> "75001",
    "country"      -> "FR"
  )

  private def errorMessages(
    fieldName: String,
    value: String
  ): Seq[String] =
    form
      .bind(validData.updated(fieldName, value))
      .errors(fieldName)
      .map(_.message)

  "AddressNonUkFormProvider" - {

    "must bind valid data" in {

      val result = form.bind(validData)

      result.errors mustBe empty

      result.value mustBe Some(
        AddressNonUk(
          addressLine1 = "1 Test Street",
          addressLine2 = Some("Test Building"),
          addressLine3 = "Paris",
          addressLine4 = Some("Ile de France"),
          postcode = Some("75001"),
          country = "FR"
        )
      )
    }

    ".addressLine1" - {

      "must fail when empty" in {
        errorMessages("addressLine1", "") must contain(
          "addressNonUk.error.addressLine1.required"
        )
      }

      "must fail when longer than 200 characters" in {
        errorMessages("addressLine1", "a" * 201) must contain(
          "addressNonUk.error.addressLine1.length"
        )
      }

      "must fail when invalid characters are entered" in {
        errorMessages("addressLine1", "1 Test Street!") must contain(
          "addressNonUk.error.addressLine1.invalid"
        )
      }

      "must fail when it contains a double dash" in {
        errorMessages("addressLine1", "1 Test--Street") must contain(
          "addressNonUk.error.addressLine1.invalidCombination"
        )
      }
    }

    ".addressLine2" - {

      "must bind an empty value as None" in {

        val result =
          form.bind(validData.updated("addressLine2", ""))

        result.errors mustBe empty
        result.value.value.addressLine2 mustBe None
      }

      "must fail when longer than 200 characters" in {
        errorMessages("addressLine2", "a" * 201) must contain(
          "addressNonUk.error.addressLine2.length"
        )
      }

      "must fail when invalid characters are entered" in {
        errorMessages("addressLine2", "Test Building!") must contain(
          "addressNonUk.error.addressLine2.invalid"
        )
      }

      "must fail when it contains a double dash" in {
        errorMessages("addressLine2", "Test--Building") must contain(
          "addressNonUk.error.addressLine2.invalidCombination"
        )
      }
    }

    ".addressLine3" - {

      "must fail when empty" in {
        errorMessages("addressLine3", "") must contain(
          "addressNonUk.error.addressLine3.required"
        )
      }

      "must fail when longer than 200 characters" in {
        errorMessages("addressLine3", "a" * 201) must contain(
          "addressNonUk.error.addressLine3.length"
        )
      }

      "must fail when invalid characters are entered" in {
        errorMessages("addressLine3", "Paris!") must contain(
          "addressNonUk.error.addressLine3.invalid"
        )
      }

      "must fail when it contains a double dash" in {
        errorMessages("addressLine3", "Paris--Centre") must contain(
          "addressNonUk.error.addressLine3.invalidCombination"
        )
      }
    }

    ".addressLine4" - {

      "must bind an empty value as None" in {

        val result =
          form.bind(validData.updated("addressLine4", ""))

        result.errors mustBe empty
        result.value.value.addressLine4 mustBe None
      }

      "must fail when longer than 200 characters" in {
        errorMessages("addressLine4", "a" * 201) must contain(
          "addressNonUk.error.addressLine4.length"
        )
      }

      "must fail when invalid characters are entered" in {
        errorMessages("addressLine4", "Ile de France!") must contain(
          "addressNonUk.error.addressLine4.invalid"
        )
      }

      "must fail when it contains a double dash" in {
        errorMessages("addressLine4", "Ile--de France") must contain(
          "addressNonUk.error.addressLine4.invalidCombination"
        )
      }
    }

    ".postcode" - {

      "must bind an empty value as None" in {

        val result =
          form.bind(validData.updated("postcode", ""))

        result.errors mustBe empty
        result.value.value.postcode mustBe None
      }

      "must bind a postcode containing allowed characters" in {

        val result =
          form.bind(validData.updated("postcode", "AB-12/34.5"))

        result.errors mustBe empty
        result.value.value.postcode mustBe Some("AB-12/34.5")
      }

      "must fail when longer than 20 characters" in {
        errorMessages("postcode", "a" * 21) must contain(
          "addressNonUk.error.postcode.length"
        )
      }

      "must fail when invalid characters are entered" in {
        errorMessages("postcode", "AB@123") must contain(
          "addressNonUk.error.postcode.invalid"
        )
      }

      "must fail when it contains a double dash" in {
        errorMessages("postcode", "AB--123") must contain(
          "addressNonUk.error.postcode.invalidCombination"
        )
      }
    }

    ".country" - {

      "must fail when empty" in {
        errorMessages("country", "") must contain(
          "addressNonUk.error.country.required"
        )
      }
    }
  }
}
