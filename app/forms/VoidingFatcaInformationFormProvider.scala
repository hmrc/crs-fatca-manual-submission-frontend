package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class VoidingFatcaInformationFormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("voidingFatcaInformation.error.required")
    )
}
