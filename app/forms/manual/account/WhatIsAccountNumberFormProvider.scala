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
import models.NumberType.{Iban, Isin}
import models.SubmissionsConstants.RegimeType
import models.{Countries, ErrorValidation, NumberType}
import play.api.data.Forms.single
import play.api.data.{Form, Mapping}
import utils.RegexConstants

import javax.inject.Inject

class WhatIsAccountNumberFormProvider @Inject() extends Mappings {

  def apply(numType: NumberType, regimeType: RegimeType): Form[String] = {

    val numberType = if (Set(Isin, Iban).contains(numType)) numType.toString.toUpperCase else "OTHER"
    val maxLength = numType match {
      case Isin => 12
      case Iban => 34
      case _    => 200
    }

    def isValidCountryCode(input: String, regime: RegimeType): Boolean =
      input.length >= 2 && Countries.allCountries(regime).map(_.code).toSet.contains(input.take(2).toUpperCase)

    val isinValidations = Seq(
      ErrorValidation(RegexConstants.ISINlength, "whatIsAccountNumber.error.length.ISIN"),
        ErrorValidation(RegexConstants.ISINinvalidcharacters, "whatIsAccountNumber.error.invalid.characters.ISIN"),
        ErrorValidation(RegexConstants.ISINinvalidformat, "whatIsAccountNumber.error.invalid.format.ISIN"),
    )
    val ibanValidations = Seq(
      ErrorValidation(RegexConstants.IBANlength, "whatIsAccountNumber.error.length.IBAN"),
        ErrorValidation(RegexConstants.IBANinvalidcharacters, "whatIsAccountNumber.error.invalid.characters.IBAN"),
        ErrorValidation(RegexConstants.IBANinvalidformat, "whatIsAccountNumber.error.invalid.format.IBAN"),
    )
    val otherValidations = Seq(
      ErrorValidation(RegexConstants.OTHERvalidcharacters, "whatIsAccountNumber.error.invalid.characters.OTHER")
    )

    val errorValidations = numType match {
      case NumberType.Iban => ibanValidations
      case NumberType.Isin => isinValidations
      case _               => otherValidations
    }

    val field = defaultStringFieldFormat(
      "whatIsAccountNumber.error.required",
      maxLength,
      s"whatIsAccountNumber.error.length.$numberType",
      errorValidations
    )

    val withCountryCode: Mapping[String] = numType match {
      case NumberType.Iban => field.verifying("whatIsAccountNumber.error.invalid.countryCode.IBAN", v => isValidCountryCode(v, regimeType))
      case NumberType.Isin => field.verifying("whatIsAccountNumber.error.invalid.countryCode.ISIN", v => isValidCountryCode(v, regimeType))
      case _               => field
    }

    Form(single("value" -> withCountryCode))
  }
}
