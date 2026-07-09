package models.manual

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait WhatTypeOfFiler

object WhatTypeOfFiler extends Enumerable.Implicits {

  case object Foreign extends WithName("foreign") with WhatTypeOfFiler
  case object Registered extends WithName("registered") with WhatTypeOfFiler
  case object Withholding extends WithName("withholding") with WhatTypeOfFiler

  val values: Seq[WhatTypeOfFiler] = Seq(
    Foreign, Registered
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"whatTypeOfFiler.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[WhatTypeOfFiler] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
