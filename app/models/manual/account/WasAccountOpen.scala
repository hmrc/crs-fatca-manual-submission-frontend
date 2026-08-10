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

package models.manual.account

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait WasAccountOpen

object WasAccountOpen extends Enumerable.Implicits {

  case object Yes extends WithName("yes") with WasAccountOpen
  case object No extends WithName("no") with WasAccountOpen
  case object NotApplicable extends WithName("na") with WasAccountOpen

  val allValues: Seq[WasAccountOpen] = Seq(
    Yes,
    No,
    NotApplicable
  )

  def options(isBefore2025: Boolean)(implicit messages: Messages): Seq[RadioItem] = {
    val content = if (isBefore2025) allValues else allValues.dropRight(1)

    content.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"wasAccountOpen.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }
  }

  implicit val enumerable: Enumerable[WasAccountOpen] =
    Enumerable(
      allValues.map(
        v => v.toString -> v
      ): _*
    )
}
