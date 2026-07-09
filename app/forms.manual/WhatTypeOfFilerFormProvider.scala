package forms.manual

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.WhatTypeOfFiler

class WhatTypeOfFilerFormProvider @Inject() extends Mappings {

  def apply(): Form[WhatTypeOfFiler] =
    Form(
      "value" -> enumerable[WhatTypeOfFiler]("whatTypeOfFiler.error.required")
    )
}
