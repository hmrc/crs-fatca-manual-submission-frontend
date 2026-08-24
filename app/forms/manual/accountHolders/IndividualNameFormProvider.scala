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
import models.ErrorValidation
import models.manual.accountHolders.IndividualName
import play.api.data.Form
import play.api.data.Forms.*
import utils.RegexConstants

import javax.inject.Inject

class IndividualNameFormProvider @Inject() extends Mappings {

  def apply(): Form[IndividualName] = Form(
    mapping(
      "FirstName" -> defaultStringFieldFormat(
        "individualName.error.FirstName.required",
        200,
        "individualName.error.FirstName.length",
        Seq(
          ErrorValidation(RegexConstants.DEFAULT_STRING_FIELD_VALID, "individualName.error.FirstName.invalid"),
          ErrorValidation(RegexConstants.DOUBLE_DASH_INVALID, "individualName.error.FirstName.doubledash")
        )
      ),
      "LastName" -> defaultStringFieldFormat(
        "individualName.error.LastName.required",
        200,
        "individualName.error.LastName.length",
        Seq(
          ErrorValidation(RegexConstants.DEFAULT_STRING_FIELD_VALID, "individualName.error.LastName.invalid"),
          ErrorValidation(RegexConstants.DOUBLE_DASH_INVALID, "individualName.error.LastName.doubledash")
        )
      )
    )(IndividualName.apply)(
      x => Some((x.FirstName, x.LastName))
    )
  )
}
