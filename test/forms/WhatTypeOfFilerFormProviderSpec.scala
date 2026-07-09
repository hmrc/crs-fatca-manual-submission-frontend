package forms.manual

import forms.behaviours.OptionFieldBehaviours
import models.WhatTypeOfFiler
import play.api.data.FormError

class WhatTypeOfFilerFormProviderSpec extends OptionFieldBehaviours {

  val form = new WhatTypeOfFilerFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "whatTypeOfFiler.error.required"

    behave like optionsField[WhatTypeOfFiler](
      form,
      fieldName,
      validValues  = WhatTypeOfFiler.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
