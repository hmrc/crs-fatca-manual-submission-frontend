package forms.manual.sponsor

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RemoveTaxResidentCountryFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "removeTaxResidentCountry.error.required"
  val invalidKey  = "error.boolean"

  val form = new RemoveTaxResidentCountryFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
