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
import models.manual.cpso.{ControllingPersonType, ControllingPersonTypes}

class ControllingPersonTypeFormProviderSpec extends StringFieldBehaviours {

  val form = new ControllingPersonTypeFormProvider()()

  private val validData = Map(
    "types[0]" -> "ownershipOfLegalPerson",
    "types[1]" -> "trusteeOfTrust"
  )

  "must bind valid data with multiple selections" in {

    val result = form.bind(validData)

    result.errors mustBe empty
    result.value mustBe Some(
      ControllingPersonTypes(
        Seq(
          ControllingPersonType.OwnershipOfLegalPerson,
          ControllingPersonType.TrusteeOfTrust
        )
      )
    )
  }

  "must bind valid data with a single selection" in {

    val result = form.bind(Map("types[0]" -> "settlorOfTrust"))

    result.errors mustBe empty
    result.value mustBe Some(
      ControllingPersonTypes(Seq(ControllingPersonType.SettlorOfTrust))
    )
  }

  "must bind NotReported as a valid selection" in {

    val result = form.bind(Map("types[0]" -> "notReported"))

    result.errors mustBe empty
    result.value mustBe Some(
      ControllingPersonTypes(Seq(ControllingPersonType.NotReported))
    )
  }

  "must fail when no option is selected" in {

    val result = form.bind(Map.empty[String, String])

    result.errors("types").map(_.message) must contain(
      "cpso.controllingPersonType.error.required"
    )
  }

  "must fail when an invalid option is submitted" in {

    val result = form.bind(Map("types[0]" -> "invalidType"))

    result.hasErrors mustBe true
    result.value mustBe None
  }

  "must populate the form correctly when previously answered" in {

    val answer = ControllingPersonTypes(
      Seq(
        ControllingPersonType.OwnershipOfLegalPerson,
        ControllingPersonType.TrusteeOfTrust
      )
    )

    val filledForm = form.fill(answer)

    filledForm.errors mustBe empty
    filledForm.value mustBe Some(answer)
    filledForm.data("types[0]") mustBe "ownershipOfLegalPerson"
    filledForm.data("types[1]") mustBe "trusteeOfTrust"
  }
}
