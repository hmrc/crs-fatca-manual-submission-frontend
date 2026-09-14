package forms

import forms.behaviours.StringFieldBehaviours
import forms.manual.cpso.CpsoOrganisationNameFormProvider
import play.api.data.FormError

class CpsoOrganisationNameFormProviderSpec extends StringFieldBehaviours {

  val form = new CpsoOrganisationNameFormProvider()()

  ".organizationName" - {

    val fieldName = "organizationName"
    val requiredKey = "cpsoOrganisationName.error.organizationName.required"
    val lengthKey = "cpsoOrganisationName.error.organizationName.length"
    val maxLength = 200

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

  ".some-name" - {

    val fieldName = "some-name"
    val requiredKey = "cpsoOrganisationName.error.some-name.required"
    val lengthKey = "cpsoOrganisationName.error.some-name.length"
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
