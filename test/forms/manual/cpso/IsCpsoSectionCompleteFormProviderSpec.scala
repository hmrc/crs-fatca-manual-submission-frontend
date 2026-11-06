package forms.manual.cpso

import forms.behaviours.BooleanFieldBehaviours
import models.SubmissionsConstants.{CRS, FATCA}
import play.api.data.FormError

class IsCpsoSectionCompleteFormProviderSpec extends BooleanFieldBehaviours {

  val invalidKey = "error.boolean"

  "crs.value" - {
    val form        = new IsCpsoSectionCompleteFormProvider()(CRS)
    val requiredKey = "cpso.crs.isCpsoSectionComplete.error.required"
    val fieldName   = "value"

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

  "fatca .value" - {

    val form        = new IsCpsoSectionCompleteFormProvider()(FATCA)
    val requiredKey = "cpso.fatca.isCpsoSectionComplete.error.required"
    val fieldName   = "value"

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
