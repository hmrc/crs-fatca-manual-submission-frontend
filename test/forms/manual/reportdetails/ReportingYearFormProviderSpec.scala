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

package forms.manual.reportdetails

import forms.behaviours.IntFieldBehaviours
import forms.manual.reportdetails
import play.api.data.FormError

import java.time.Year

class ReportingYearFormProviderSpec extends IntFieldBehaviours {

  val form = new ReportingYearFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 2014
    val maximum = Year.now.getValue

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "reportingYear.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "reportingYear.error.wholeNumber")
    )

    behave like intFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      expectedError = FormError(fieldName, "reportingYear.error.minimum", Seq(minimum))
    )

    behave like intFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      expectedError = FormError(fieldName, "reportingYear.error.maximum", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "reportingYear.error.required")
    )
  }
}
