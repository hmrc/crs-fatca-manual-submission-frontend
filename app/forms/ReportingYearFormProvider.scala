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

import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form
import utils.ReportingConstants.REPORTING_START_YEAR

import java.time.Year

class ReportingYearFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int("reportingYear.error.required", "reportingYear.error.wholeNumber", "reportingYear.error.nonNumeric")
        .verifying(
          minimumValue(REPORTING_START_YEAR, "reportingYear.error.minimum"),
          maximumValue(Year.now.getValue, "reportingYear.error.maximum")
        )
    )
}
