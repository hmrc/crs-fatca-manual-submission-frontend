package forms.manual.cpso

import forms.behaviours.StringFieldBehaviours
import forms.manual.cpso.UkPostCodeFormProvider
import play.api.data.FormError

class UkPostCodeFormProviderSpec extends StringFieldBehaviours {

  val form = new UkPostCodeFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "ukPostCode.error.value.required"
    val lengthKey = "ukPostCode.error.value.length"
    val maxLength = 100

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

  ".value2" - {

    val fieldName = "value2"
    val requiredKey = "ukPostCode.error.value2.required"
    val lengthKey = "ukPostCode.error.value2.length"
    val maxLength = 100

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
