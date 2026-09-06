package forms.manual.account

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RemovePaymentFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "removePayment.error.required"
  val invalidKey = "error.boolean"

  val form = new RemovePaymentFormProvider()()

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
