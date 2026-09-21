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
import models.manual.cpso.IndividualPlaceOfBirth

class IndividualPlaceOfBirthFormProviderSpec extends StringFieldBehaviours {

  val form = new IndividualPlaceOfBirthFormProvider()()

  private val validData = Map(
    "city"    -> "1 Test Street",
    "region"  -> "Test Building",
    "country" -> "FR"
  )

  private def errorMessages(
    fieldName: String,
    value: String
  ): Seq[String] =
    form
      .bind(validData.updated(fieldName, value))
      .errors(fieldName)
      .map(_.message)

  "must bind valid data" in {

    val result = form.bind(validData)

    result.errors mustBe empty

    result.value mustBe Some(
      IndividualPlaceOfBirth(
        city = Some("1 Test Street"),
        region = Some("Test Building"),
        country = "FR"
      )
    )
  }

  ".city" - {

    "must bind an empty value as None" in {

      val result =
        form.bind(validData.updated("city", ""))

      result.errors mustBe empty
      result.value.value.city mustBe None
    }

    "must fail when longer than 200 characters" in {
      errorMessages("city", "a" * 201) must contain(
        "cpso.individualPlaceOfBirth.error.city.length"
      )
    }

    "must fail when invalid characters are entered" in {
      errorMessages("city", "Test Building!") must contain(
        "cpso.individualPlaceOfBirth.error.city.invalid"
      )
    }

    "must fail when it contains a double dash" in {
      errorMessages("city", "Test--Building") must contain(
        "cpso.individualPlaceOfBirth.error.city.invalidCombination"
      )
    }
  }

  ".region" - {

    "must bind an empty value as None" in {

      val result =
        form.bind(validData.updated("region", ""))

      result.errors mustBe empty
      result.value.value.region mustBe None
    }

    "must fail when longer than 200 characters" in {
      errorMessages("region", "a" * 201) must contain(
        "cpso.individualPlaceOfBirth.error.region.length"
      )
    }

    "must fail when invalid characters are entered" in {
      errorMessages("region", "Test Building!") must contain(
        "cpso.individualPlaceOfBirth.error.region.invalid"
      )
    }

    "must fail when it contains a double dash" in {
      errorMessages("region", "Test--Building") must contain(
        "cpso.individualPlaceOfBirth.error.region.invalidCombination"
      )
    }
  }

  ".country" - {

    "must fail when empty" in {
      errorMessages("country", "") must contain(
        "cpso.individualPlaceOfBirth.error.country.required"
      )
    }
  }

}
