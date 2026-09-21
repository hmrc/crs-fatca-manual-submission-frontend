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

import forms.mappings.Mappings
import models.SubmissionsConstants.RegimeType
import models.manual.accountHolders.IndividualDateOfBirth
import play.api.data.Form

import javax.inject.Inject

class CPSOIndividualDateOfBirthFormProvider @Inject() extends Mappings {

  def apply(regime: RegimeType): Form[IndividualDateOfBirth] =
    Form(
      "value" ->
        dateOfBirth(
          requiredKey = s"cpso.individualDateOfBirth.error.required.$regime",
          invalidCharactersKey = "cpso.individualDateOfBirth.error.invalidCharacters",
          dayRequiredKey = s"cpso.individualDateOfBirth.error.day.required.$regime",
          monthRequiredKey = s"cpso.individualDateOfBirth.error.month.required.$regime",
          yearRequiredKey = s"cpso.individualDateOfBirth.error.year.required.$regime",
          dayMonthRequiredKey = s"cpso.individualDateOfBirth.error.dayMonth.required.$regime",
          dayYearRequiredKey = s"cpso.individualDateOfBirth.error.dayYear.required.$regime",
          monthYearRequiredKey = s"cpso.individualDateOfBirth.error.monthYear.required.$regime",
          realDateKey = "cpso.individualDateOfBirth.error.real",
          pastKey = "cpso.individualDateOfBirth.error.past",
          futureKey = "cpso.individualDateOfBirth.error.future"
        )
          .transform[IndividualDateOfBirth](
            IndividualDateOfBirth.apply,
            _.dateOfBirth
          )
    )
}
