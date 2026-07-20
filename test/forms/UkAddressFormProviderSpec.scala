package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class UkAddressFormProviderSpec extends StringFieldBehaviours {

  val form = new UkAddressFormProvider()()

  ".addressLine1" - {

    val fieldName   = "addressLine1"
    val requiredKey = "ukAddress.error.addressLine1.required"
    val lengthKey   = "ukAddress.error.addressLine1.length"
    val maxLength   = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".addressLine2" - {

    val fieldName   = "addressLine2"
    val requiredKey = "ukAddress.error.addressLine2.required"
    val lengthKey   = "ukAddress.error.addressLine2.length"
    val maxLength   = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
