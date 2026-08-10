package forms.manual.account

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class HowManyJoinAccountHoldersFormProviderSpec extends IntFieldBehaviours {

  val form = new HowManyJoinAccountHoldersFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 1
    val maximum = 200

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "howManyJoinAccountHolders.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "howManyJoinAccountHolders.error.wholeNumber")
    )

    behave like intFieldWithMinimum(
      form,
      fieldName,
      minimum = minimum,
      expectedError = FormError(fieldName, "howManyJoinAccountHolders.error.min", Seq(minimum))
    )

    behave like intFieldWithMaximum(
      form,
      fieldName,
      maximum = maximum,
      expectedError = FormError(fieldName, "howManyJoinAccountHolders.error.min", Seq(maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "howManyJoinAccountHolders.error.required")
    )
  }
}
