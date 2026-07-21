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
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.data.FormError

class UkAddressFormProviderSpec extends StringFieldBehaviours {

  val form = new UkAddressFormProvider()()

  private val validData = Map(
    "addressLine1" -> "Address Line 1",
    "addressLine2" -> "Address Line 2",
    "city"         -> "City",
    "county"       -> "County",
    "postCode"     -> "AA1 1AA",
    "country"      -> "GB"
  )

  "UkAddressFormProvider" - {
    "bind valid data" in {
      val result = form.bind(validData)
      result.errors shouldBe empty
    }

    "addressLine1" - {
      "cannot be empty" in {
        val result = form.bind(validData.updated("addressLine1", ""))
        result.errors("addressLine1").map(_.message) shouldBe Seq("ukAddress.error.addressLine1.required")
      }

      "must fail when longer than 200 characters" in {
        val longAddressLine1 = "A" * 201
        val result           = form.bind(validData.updated("addressLine1", longAddressLine1))
        result.errors("addressLine1").map(_.message) shouldBe Seq("ukAddress.error.addressLine1.length")
      }

      "must fail when invalid characters are entered" in {
        val invalidAddressLine1 = "Address Line 1!"
        val result              = form.bind(validData.updated("addressLine1", invalidAddressLine1))
        result.errors("addressLine1").map(_.message) shouldBe Seq("ukAddress.error.addressLine1.invalid.characters")
      }

      "must fail when it contains a double dash" in {
        val invalidAddressLine1 = "Address--Line 1"
        val result              = form.bind(validData.updated("addressLine1", invalidAddressLine1))
        result.errors("addressLine1").map(_.message) shouldBe Seq("ukAddress.error.addressLine1.invalid.characters.combination")
      }
    }

    "addressLine2" - {
      "must fail when longer than 200 characters" in {
        val longAddressLine2 = "A" * 201
        val result           = form.bind(validData.updated("addressLine2", longAddressLine2))
        result.errors("addressLine2").map(_.message) shouldBe Seq("ukAddress.error.addressLine2.length")
      }

      "must fail when invalid characters are entered" in {
        val invalidAddressLine2 = "Address Line 2!"
        val result              = form.bind(validData.updated("addressLine2", invalidAddressLine2))
        result.errors("addressLine2").map(_.message) shouldBe Seq("ukAddress.error.addressLine2.invalid.characters")
      }

      "must fail when it contains a double dash" in {
        val invalidAddressLine2 = "Address--Line 2"
        val result              = form.bind(validData.updated("addressLine2", invalidAddressLine2))
        result.errors("addressLine2").map(_.message) shouldBe Seq("ukAddress.error.addressLine2.invalid.characters.combination")
      }
    }

    "city" - {

      "cannot be empty" in {
        val result = form.bind(validData.updated("city", ""))
        result.errors("city").map(_.message) shouldBe Seq("ukAddress.error.city.required")
      }

      "must fail when longer than 200 characters" in {
        val longCity = "A" * 201
        val result   = form.bind(validData.updated("city", longCity))
        result.errors("city").map(_.message) shouldBe Seq("ukAddress.error.city.length")
      }

      "must fail when invalid characters are entered" in {
        val invalidCity = "City!"
        val result      = form.bind(validData.updated("city", invalidCity))
        result.errors("city").map(_.message) shouldBe Seq("ukAddress.error.city.invalid.characters")
      }

      "must fail when it contains a double dash" in {
        val invalidCity = "City--Name"
        val result      = form.bind(validData.updated("city", invalidCity))
        result.errors("city").map(_.message) shouldBe Seq("ukAddress.error.city.invalid.characters.combination")
      }
    }

    "county" - {
      "must fail when longer than 200 characters" in {
        val longCounty = "A" * 201
        val result     = form.bind(validData.updated("county", longCounty))
        result.errors("county").map(_.message) shouldBe Seq("ukAddress.error.county.length")
      }

      "must fail when invalid characters are entered" in {
        val invalidCounty = "County!"
        val result        = form.bind(validData.updated("county", invalidCounty))
        result.errors("county").map(_.message) shouldBe Seq("ukAddress.error.county.invalid.characters")
      }

      "must fail when it contains a double dash" in {
        val invalidCounty = "County--Name"
        val result        = form.bind(validData.updated("county", invalidCounty))
        result.errors("county").map(_.message) shouldBe Seq("ukAddress.error.county.invalid.characters.combination")
      }
    }

    "postCode" - {
      "cannot be empty" in {
        val result = form.bind(validData.updated("postCode", ""))
        result.errors("postCode").map(_.message) shouldBe Seq("ukAddress.error.postCode.required")
      }

      "must fail when longer than 8 characters" in {
        val longPostCode = "LS27 9DA" * 2
        val result       = form.bind(validData.updated("postCode", longPostCode))
        result.errors("postCode").map(_.message) shouldBe Seq("ukAddress.error.postCode.length")
      }

      "must fail when invalid characters are entered" in {
        val invalidPostCode = "AA1 1AA!"
        val result          = form.bind(validData.updated("postCode", invalidPostCode))
        result.errors("postCode").map(_.message) shouldBe Seq("ukAddress.error.postCode.invalid")
      }

      "must fail when Postcode format is invalid" in {
        val invalidPostCode = "2S23 9AB"
        val result          = form.bind(validData.updated("postCode", invalidPostCode))
        result.errors("postCode").map(_.message) shouldBe Seq("ukAddress.error.postCode.format")
      }
    }

    "country" - {
      "cannot be empty" in {
        val result = form.bind(validData.updated("country", ""))
        result.errors("country").map(_.message) shouldBe Seq("ukAddress.error.country.required")
      }
    }
  }

}
