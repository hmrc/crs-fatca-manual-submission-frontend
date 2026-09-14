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
import models.manual.cpso.CpsoOrganisationName
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class CpsoOrganisationNameFormProvider @Inject() extends Mappings {

   def apply(): Form[CpsoOrganisationName] = Form(
     mapping(
      "organizationName" -> text("cpsoOrganisationName.error.organizationName.required")
        .verifying(maxLength(200, "cpsoOrganisationName.error.organizationName.length"))
    )(CpsoOrganisationName.apply)(x => (Some(x.organizationName)))
   )
 }
