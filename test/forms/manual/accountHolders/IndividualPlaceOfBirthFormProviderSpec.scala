package forms.manual.accountHolders

import forms.behaviours.StringFieldBehaviours
import forms.manual.accountHolders.IndividualPlaceOfBirthFormProvider
import play.api.data.FormError

class IndividualPlaceOfBirthFormProviderSpec extends StringFieldBehaviours {

  val form = new IndividualPlaceOfBirthFormProvider()()

  ".City" - {

    val fieldName   = "City"
    val requiredKey = "individualPlaceOfBirth.error.City.required"
    val lengthKey   = "individualPlaceOfBirth.error.City.length"
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

  ".Region" - {

    val fieldName   = "Region"
    val requiredKey = "individualPlaceOfBirth.error.Region.required"
    val lengthKey   = "individualPlaceOfBirth.error.Region.length"
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
