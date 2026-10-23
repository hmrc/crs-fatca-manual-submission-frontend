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

package forms.manual.cpso.taxIdentificationNumber

sealed trait TaxIdentificationNumberError { def key: String }

object TaxIdentificationNumberError {

  case object EmptyCrs extends TaxIdentificationNumberError {
    val key = "cpso.crs.taxIdentificationNumber.error.tin.required"
  }

  case object EmptyFatca extends TaxIdentificationNumberError {
    val key = "cpso.fatca.taxIdentificationNumber.error.tin.required"
  }

  case object TooLong extends TaxIdentificationNumberError {
    val key = "cpso.taxIdentificationNumber.error.tin.length"
  }

  case object InvalidChars extends TaxIdentificationNumberError {
    val key = "cpso.taxIdentificationNumber.error.tin.invalidChars"
  }

  case object InvalidGIINOrSSN extends TaxIdentificationNumberError {
    val key = "cpso.taxIdentificationNumber.error.tin.giinOrSsn.invalid"
  }

  case object OrganizationTinBlacklisted extends TaxIdentificationNumberError {
    val key = "cpso.taxIdentificationNumber.error.tin.organization.is.blacklisted"
  }

  case object IndividualTinBlacklisted extends TaxIdentificationNumberError {
    val key = "cpso.taxIdentificationNumber.error.tin.individual.is.blacklisted"
  }

}
