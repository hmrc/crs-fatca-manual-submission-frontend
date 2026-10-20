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

import forms.manual.cpso.taxIdentificationNumber.*
import forms.manual.cpso.taxIdentificationNumber.TaxIdentificationNumberError.{
  EmptyCrs,
  EmptyFatca,
  IndividualTinBlacklisted,
  InvalidChars,
  InvalidGIINOrSSN,
  OrganizationTinBlacklisted,
  TooLong
}
import forms.manual.cpso.taxIdentificationNumber.TaxIdentificationNumberFormatter.{
  exampleGIIN,
  giinAllowedChars,
  giinFormatRegex,
  giinLength,
  individualUSA2023Blacklist,
  maxLength,
  organizationUSA2023Blacklist,
  InvalidCharsPattern,
  SsnDashed1,
  SsnDashed2,
  SsnPlain
}
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.cpso.IndividualOrOrganisation.{Individual, Organisation}
import play.api.data.FormError
import play.api.data.format.Formatter

class TaxIdentificationNumberFormatter(ctx: TaxIdentificationNumberContext) extends Formatter[String] {

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
    data.get(key).map(_.trim) match {
      case None | Some("") =>
        Left(Seq(FormError(key, if ctx.reportId.regime == CRS then EmptyCrs.key else EmptyFatca.key)))
      case Some(value) =>
        validate(value) match {
          case Right(_)  => Right(value)
          case Left(err) => Left(Seq(FormError(key, err.key)))
        }
    }

  override def unbind(key: String, value: String): Map[String, String] =
    Map(key -> value)

  private def validate(value: String): Either[TaxIdentificationNumberError, Unit] = {
    val v = Option(value).getOrElse("")

    if (v.trim.isEmpty) {
      Left(if ctx.reportId.regime == CRS then EmptyCrs else EmptyFatca)
    } else if (isCrsOrNonUsFatca && v.length > maxLength) {
      Left(TooLong)
    } else if (isCrsOrNonUsFatca && InvalidCharsPattern.findFirstIn(v).isDefined) {
      Left(InvalidChars)
    } else if (isFatcaUs && !isValidUsFormat(v)) {
      Left(InvalidGIINOrSSN)
    } else if (isValueInOrganizationBlackList(v)) {
      Left(OrganizationTinBlacklisted)
    } else if (isValueInIndividualBlackList(v)) {
      Left(IndividualTinBlacklisted)
    } else {
      Right(())
    }
  }

  private def isFatcaUs: Boolean =
    ctx.reportId.regime == FATCA && ctx.country.code == "US"

  private def isCrsOrNonUsFatca: Boolean =
    ctx.reportId.regime == CRS || (ctx.reportId.regime == FATCA && ctx.country.code != "US")

  private def isValidUsFormat(v: String): Boolean =
    (v.matches(giinAllowedChars) && v.length == giinLength && v != exampleGIIN) || (v.matches(giinFormatRegex) || SsnDashed1.matches(v) || SsnDashed2.matches(
      v
    ) || SsnPlain.matches(v))

  private def isFatcaUsAnd2023AndOnwards =
    isFatcaUs && ctx.reportId.reportingYear >= 2023

  private def isValueInIndividualBlackList(v: String) =
    isFatcaUsAnd2023AndOnwards && individualUSA2023Blacklist.contains(v) && ctx.individualOrOrganization.contains(Individual)

  private def isValueInOrganizationBlackList(v: String) =
    isFatcaUsAnd2023AndOnwards && organizationUSA2023Blacklist.contains(v) && ctx.individualOrOrganization.contains(Organisation)
}

object TaxIdentificationNumberFormatter {
  private val maxLength           = 200
  private val InvalidCharsPattern = """--|&#|/\*""".r

  private val giinLength       = 19
  private val giinAllowedChars = """^[A-Za-z0-9.]*$"""
  private val giinFormatRegex  = "^(?i:[A-NP-Z0-9]{6}\\.[A-NP-Z0-9]{5}\\.[A-NP-Z]{2}\\.[0-9]{3})$"
  private val exampleGIIN      = "98O96B.00000.LE.350"
  private val SsnDashed1       = """^\d{3}-\d{2}-\d{4}$""".r
  private val SsnDashed2       = """^\d{2}-\d{7}$""".r
  private val SsnPlain         = """^\d{9}$""".r

  private val organizationUSA2023Blacklist = Set("000000000", "222222222", "000222111", "333333333", "888888888")
  private val individualUSA2023Blacklist   = Set("000000000", "666666666", "888888888")
}
