package forms.manual.account

import forms.behaviours.IntFieldBehaviours
import forms.manual.account.HowManyJoinAccountHoldersFormProvider
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
      nonNumericError  = FormError(fieldName, "howManyJoinAccountHolders.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "howManyJoinAccountHolders.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "howManyJoinAccountHolders.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "howManyJoinAccountHolders.error.required")
    )
  }
}
