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

package viewmodels.manual.accountHolders

import models.manual.accountHolders.EquityInterestType
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.{CheckboxItem, Text}

object EquityInterestTypeViewModel {

  def items(form: Form[_])(implicit messages: Messages): Seq[CheckboxItem] = {
    val selected = form.data.values.toSet

    EquityInterestType.values.zipWithIndex.map {
      case (equityInterestType, index) =>
        CheckboxItem(
          name = Some(s"types[$index]"),
          content = Text(messages(s"accountHolders.equityInterestType.${equityInterestType.toString}")),
          value = equityInterestType.toString,
          checked = selected.contains(equityInterestType.toString)
        )
    }
  }
}
