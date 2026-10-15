package forms

import forms.behaviours.StringFieldBehaviours
import forms.manual.cpso.ResidentForTaxFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import models.elections.RegimeType
import play.api.data.FormError

class ResidentForTaxFormProviderSpec extends StringFieldBehaviours {

  val form = new ResidentForTaxFormProvider()(CRS)

  ".country crs" - {
    val form        = new ResidentForTaxFormProvider()(CRS)
    val fieldName   = "country"
    val requiredKey = "cpso.crs.residentForTax.error.country.required"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".country fatca" - {
    val form        = new ResidentForTaxFormProvider()(FATCA)
    val fieldName   = "country"
    val requiredKey = "cpso.fatca.residentForTax.error.country.required"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
