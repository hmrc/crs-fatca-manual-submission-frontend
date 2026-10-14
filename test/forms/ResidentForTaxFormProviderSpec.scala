package forms

import forms.behaviours.StringFieldBehaviours
import forms.manual.cpso.ResidentForTaxFormProvider
import play.api.data.FormError

class ResidentForTaxFormProviderSpec extends StringFieldBehaviours {

  val form = new ResidentForTaxFormProvider()()

  ".country" - {

    val fieldName = "country"
    val requiredKey = "residentForTax.error.country.required"
    val lengthKey = "residentForTax.error.country.length"
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

  ".deleteme" - {

    val fieldName = "deleteme"
    val requiredKey = "residentForTax.error.deleteme.required"
    val lengthKey = "residentForTax.error.deleteme.length"
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
