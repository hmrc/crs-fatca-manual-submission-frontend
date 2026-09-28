package forms.manual.cpso

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class IsThisTheAddressFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "isThisTheAddress.error.required"
  val invalidKey = "error.boolean"

  val form = new IsThisTheAddressFormProvider()()

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
