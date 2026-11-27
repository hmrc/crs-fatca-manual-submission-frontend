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

package models.manual.filercategory

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait WhatTypeOfFiler extends FilerCategoryTypes

sealed trait FilerCategoryTypes {
  val xmlValue: String
}

object FilerCategoryTypes {

  def isSponsorType(filer: FilerCategoryTypes): Boolean =
    if (filer == WhatTypeOfFilerIsSponsor) true else false
}
sealed trait WhatTypeOfFilerIsSponsor extends FilerCategoryTypes

object WhatTypeOfFilerIsSponsor extends Enumerable.Implicits {

  case object ForeignFI extends WithName("foreignFI") with WhatTypeOfFilerIsSponsor {
    val xmlValue = "FATCA607"
  }

  case object DirectNonFinancial extends WithName("directNonFinancial") with WhatTypeOfFilerIsSponsor {
    val xmlValue = "FATCA608"
  }

  case object Trustee extends WithName("trustee") with WhatTypeOfFilerIsSponsor {
    val xmlValue = "FATCA609"
  }

  val values: Seq[WhatTypeOfFilerIsSponsor] = Seq(
    ForeignFI,
    DirectNonFinancial,
    Trustee
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"whatTypeOfFilerIsSponsor.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[WhatTypeOfFilerIsSponsor] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}

object WhatTypeOfFiler extends Enumerable.Implicits {

  case object Foreign extends WithName("foreign") with WhatTypeOfFiler {
    val xmlValue = "FATCA601"
  }

  case object Registered extends WithName("registered") with WhatTypeOfFiler {
    val xmlValue = "FATCA602"
  }

  case object Withholding extends WithName("withholding") with WhatTypeOfFiler {
    val xmlValue = "FATCA610"
  }

  val values: Seq[WhatTypeOfFiler] = Seq(
    Foreign,
    Registered,
    Withholding
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"whatTypeOfFiler.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[WhatTypeOfFiler] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
