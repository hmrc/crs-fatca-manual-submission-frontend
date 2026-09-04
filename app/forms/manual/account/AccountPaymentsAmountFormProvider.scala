/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.manual.account

import forms.mappings.{Mappings, Transforms}
import models.SubmissionsConstants.RegimeType
import models.manual.account.AccountPaymentsAmount
import models.{Currencies, Currency}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import javax.inject.Inject

class AccountPaymentsAmountFormProvider @Inject() extends Mappings with Transforms {

  def apply(regime: RegimeType): Form[AccountPaymentsAmount] = {

    val requiredAmountError   = "whatWasTheAccountBalance.error.required.amount"
    val invalidErrorKey       = s"whatWasTheAccountBalance.error.invalid.${regime.value}"
    val minusAmountErrorKey   = "whatWasTheAccountBalance.error.minus.FATCA"
    val decimalPlacesErrorKey = "whatWasTheAccountBalance.error.decimalPlaces"

    Form(
      mapping(
        "currency" -> text("whatWasTheAccountBalance.error.required.currency")
          .verifying(currencyConstraint(regime))
          .transform[Currency](
            code => Currencies.all(regime).find(_.code == code).get,
            currency => currency.code
          ),
        "amount" -> currencyAmount(regime, requiredAmountError, invalidErrorKey, minusAmountErrorKey, decimalPlacesErrorKey)
      )(AccountPaymentsAmount.apply)(
        ab => Some((ab.currency, ab.amount))
      )
    )
  }

  private def currencyConstraint(regime: RegimeType): Constraint[String] = Constraint("constraint.currency") {
    code =>
      if (Currencies.all(regime).exists(_.code == code)) {
        Valid
      } else {
        Invalid(ValidationError("whatWasTheAccountBalance.error.required.currency"))
      }
  }

}
