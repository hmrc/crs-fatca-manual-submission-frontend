package forms.manual.accountHolders

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class AccountHolderListFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "accountHolderList.error.required"
  val invalidKey = "error.boolean"

  val form = new AccountHolderListFormProvider()()

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
