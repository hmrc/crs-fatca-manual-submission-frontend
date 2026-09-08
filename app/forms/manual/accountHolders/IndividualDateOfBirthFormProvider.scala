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

import forms.mappings.Mappings
import models.manual.accountHolders.IndividualDateOfBirth
import play.api.data.Form

import javax.inject.Inject

class IndividualDateOfBirthFormProvider @Inject() extends Mappings {

  def apply(): Form[IndividualDateOfBirth] =
    Form(
      "value" ->
        dateOfBirth(
          requiredKey = "individualDateOfBirth.error.required",
          invalidCharactersKey = "individualDateOfBirth.error.invalidCharacters",
          dayRequiredKey = "individualDateOfBirth.error.day.required",
          monthRequiredKey = "individualDateOfBirth.error.month.required",
          yearRequiredKey = "individualDateOfBirth.error.year.required",
          dayMonthRequiredKey = "individualDateOfBirth.error.dayMonth.required",
          dayYearRequiredKey = "individualDateOfBirth.error.dayYear.required",
          monthYearRequiredKey = "individualDateOfBirth.error.monthYear.required",
          realDateKey = "individualDateOfBirth.error.real",
          pastKey = "individualDateOfBirth.error.past",
          futureKey = "individualDateOfBirth.error.future"
        )
          .transform[IndividualDateOfBirth](
            IndividualDateOfBirth.apply,
            _.dateOfBirth
          )
    )
}