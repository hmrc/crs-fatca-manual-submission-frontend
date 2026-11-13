package forms.manual.cpso

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class ControllingPersonsOrSubstantialOwnersFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "controllingPersonsOrSubstantialOwners.error.required"
  val invalidKey  = "error.boolean"

  val form = new ControllingPersonsOrSubstantialOwnersFormProvider()()

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
