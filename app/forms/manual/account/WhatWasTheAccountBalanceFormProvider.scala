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

import forms.mappings.Mappings
import models.SubmissionsConstants.{FATCA, RegimeType}
import models.{AccountBalance, Currencies, Currency}
import play.api.data.Form
import play.api.data.Forms.*
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import javax.inject.Inject

class WhatWasTheAccountBalanceFormProvider @Inject() extends Mappings {

  def apply(regime: RegimeType): Form[AccountBalance] = {

    val isFATCA: Boolean = regime == FATCA

    val crsAmountFormatRegex   = "^[0-9.]+$".r
    val fatcaAmountFormatRegex = "^[-0-9.]+$".r
    val minusPositionRegex     = "^-?[^-]*$".r
    val decimalFormatRegex     = "^-?[0-9]+(\\.[0-9]{1,2})?$".r

    val fatcaAmountConstraint: Constraint[String] = Constraint("Amount") {
      value =>
        if (!fatcaAmountFormatRegex.matches(value)) {
          Invalid(ValidationError("whatWasTheAccountBalance.error.invalid.FATCA"))
        } else if (!minusPositionRegex.matches(value)) {
          Invalid(ValidationError("whatWasTheAccountBalance.error.minus.FATCA"))
        } else if (!decimalFormatRegex.matches(value)) {
          Invalid(ValidationError("whatWasTheAccountBalance.error.decimalPlaces"))
        } else {
          Valid
        }
    }

    val crsAmountConstraint: Constraint[String] = Constraint("Amount") {
      value =>
        if (!crsAmountFormatRegex.matches(value)) {
          Invalid(ValidationError("whatWasTheAccountBalance.error.invalid.CRS"))
        } else if (!decimalFormatRegex.matches(value)) {
          Invalid(ValidationError("whatWasTheAccountBalance.error.decimalPlaces"))
        } else {
          Valid
        }
    }

    Form(
      mapping(
        "currency" -> text("whatWasTheAccountBalance.error.required.currency")
          .transform[Currency](
            code => Currencies.all.find(_.code == code).get,
            currency => currency.code
          ),
        "amount" -> text("whatWasTheAccountBalance.error.required.amount")
          .verifying(if (isFATCA) fatcaAmountConstraint else crsAmountConstraint)
      )(AccountBalance.apply)(
        ab => Some((ab.currency, ab.amount))
      )
    )
  }
}
