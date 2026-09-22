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
import forms.manual.cpso.CpsoOrganisationNameFormProvider
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.data.FormError

class CpsoOrganisationNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "cpsoOrganisationName.error.organizationName.required"
  val lengthKey   = "cpsoOrganisationName.error.organizationName.length"
  val maxLength   = 200

  val form = new CpsoOrganisationNameFormProvider()()

  private val validData = Map(
    "value" -> "Organization Name"
  )

  ".CpsoOrganisationNameFormProvider" - {

    "bind valid data" in {
      val result = form.bind(validData)
      result.errors shouldBe empty
    }

    "value" - {
      "cannot be empty" in {
        val result = form.bind(validData.updated("value", ""))
        result.errors("value").map(_.message) shouldBe Seq("cpso.organisationName.error.required")
      }

      "must fail when longer than 200 characters" in {
        val orgName = "A" * 201
        val result  = form.bind(validData.updated("value", orgName))
        result.errors("value").map(_.message) shouldBe Seq("cpso.organisationName.error.length")
      }

      "must fail when invalid characters are entered" in {
        val orgName = "org name!"
        val result  = form.bind(validData.updated("value", orgName))
        result.errors("value").map(_.message) shouldBe Seq("cpso.organisationName.error.invalid")
      }

      "must fail when it contains a double dash" in {
        val orgName = "orgname--name"
        val result  = form.bind(validData.updated("value", orgName))
        result.errors("value").map(_.message) shouldBe Seq("cpso.organisationName.error.invalid-combination")
      }
    }
  }
}
