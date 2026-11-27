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

package models

import models.SubmissionsConstants.{CRS, RegimeType}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait NumberType {
  val xmlValue: String
}

object NumberType extends Enumerable.Implicits {

  case object Iban extends WithName("iBAN") with NumberType {
    val xmlValue = "OECD601"
  }

  case object Oban extends WithName("oBAN") with NumberType {
    val xmlValue = "OECD602"
  }

  case object Isin extends WithName("iSIN") with NumberType {
    val xmlValue = "OECD603"
  }

  case object Osin extends WithName("oSIN") with NumberType {
    val xmlValue = "OECD604"
  }

  case object Semp extends WithName("sEMP") with NumberType {
    val xmlValue = "OECD606"
  }

  case object Other extends WithName("oTHER") with NumberType {
    val xmlValue = "OECD605"
  }

  val values: Seq[NumberType] = Seq(
    Iban,
    Oban,
    Isin,
    Osin,
    Semp,
    Other
  )

  def options(regimeType: RegimeType)(implicit messages: Messages): Seq[RadioItem] = valuesFor(regimeType).zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"numberType.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  private def valuesFor(regimeType: RegimeType): Seq[NumberType] =
    regimeType match {
      case CRS => values
      case _   => values.filterNot(_ == Semp)
    }

  implicit val enumerable: Enumerable[NumberType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
