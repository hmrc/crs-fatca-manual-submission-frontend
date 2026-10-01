package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class WhatIsAccountNumberFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("whatIsAccountNumber.error.required")
        .verifying(maxLength(100, "whatIsAccountNumber.error.length"))
    )
}
