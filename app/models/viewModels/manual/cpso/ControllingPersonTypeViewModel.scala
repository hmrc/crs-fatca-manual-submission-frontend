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

package models.viewModels.manual.cpso

import models.manual.cpso.ControllingPersonType
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

object ControllingPersonTypeViewModel {

  def items(form: Form[_], reportingYear: Int)(implicit messages: Messages): Seq[CheckboxItem] = {
    val selected = form.data.values.toSet

    ControllingPersonType.values
      .filter(
        personType => reportingYear <= 2025 || personType != ControllingPersonType.NotReported
      )
      .zipWithIndex
      .map {
        case (personType, index) =>
          CheckboxItem(
            name = Some(s"types[$index]"),
            content = Text(messages(s"cpso.controllingPersonType.${personType.toString}")),
            value = personType.toString,
            checked = selected.contains(personType.toString)
          )
      }
  }
}
