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

package models.manual.accountHolders

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait OrganisationCrs

object OrganisationCrs extends Enumerable.Implicits {

  case object PassiveWithMultiplePersons extends WithName("passiveWithMultiplePersons") with OrganisationCrs
  case object CrsReportable extends WithName("crsReportable") with OrganisationCrs
  case object PassiveCrsPerson extends WithName("passiveCrsPerson") with OrganisationCrs

  val values: Seq[OrganisationCrs] = Seq(
    PassiveWithMultiplePersons,
    CrsReportable,
    PassiveCrsPerson
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"organisationCrs.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[OrganisationCrs] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
