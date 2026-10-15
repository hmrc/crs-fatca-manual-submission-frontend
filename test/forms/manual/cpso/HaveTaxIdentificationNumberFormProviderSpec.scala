package forms.manual.cpso

import forms.behaviours.BooleanFieldBehaviours
import models.SubmissionsConstants.{CRS, FATCA}
import play.api.data.FormError

class HaveTaxIdentificationNumberFormProviderSpec extends BooleanFieldBehaviours {

  
  val invalidKey = "error.boolean"
  
  ".value crs" - {
    val form = new HaveTaxIdentificationNumberFormProvider()(CRS)
    val requiredKey = "cpso.crs.haveTaxIdentificationNumber.error.required"
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

  ".value fatca" - {
    val form = new HaveTaxIdentificationNumberFormProvider()(FATCA)

    val requiredKey = "cpso.fatca.haveTaxIdentificationNumber.error.required"
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
