package forms

import forms.behaviours.StringFieldBehaviours
import forms.manual.accountHolders.ResidentTaxFormProvider
import play.api.data.FormError

class ResidentTaxFormProviderSpec extends StringFieldBehaviours {

  val form = new ResidentTaxFormProvider()()

  ".country" - {

    val fieldName = "country"
    val requiredKey = "residentTax.error.country.required"
    val lengthKey = "residentTax.error.country.length"
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

  ".blank" - {

    val fieldName = "blank"
    val requiredKey = "residentTax.error.blank.required"
    val lengthKey = "residentTax.error.blank.length"
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
