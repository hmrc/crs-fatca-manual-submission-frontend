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

package models.manual.account

import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.manual.account.PaymentType.crsValues
import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait PaymentType

object PaymentType extends Enumerable.Implicits {

  case object CRSDividends extends WithName("CRS501") with PaymentType
  case object CRSInterest extends WithName("CRS502") with PaymentType
  case object CRSGrossProceedsOrRedemptions extends WithName("CRS503") with PaymentType
  case object CRSOther extends WithName("CRS504") with PaymentType

  case object FATCADividends extends WithName("FATCA501") with PaymentType
  case object FATCAInterest extends WithName("FATCA502") with PaymentType
  case object FATCAGrossProceedsOrRedemptions extends WithName("FATCA503") with PaymentType
  case object FATCAOther extends WithName("FATCA504") with PaymentType

  val crsValues = Seq(CRSDividends, CRSInterest, CRSGrossProceedsOrRedemptions, CRSOther)
  val crsNonCashValueInsuranceValues = Seq(CRSGrossProceedsOrRedemptions, CRSOther)
  val fataValues = Seq(FATCADividends, FATCAInterest, FATCAGrossProceedsOrRedemptions, FATCAOther)

  val values: Seq[PaymentType] = Seq(
    CRSDividends, CRSInterest, CRSGrossProceedsOrRedemptions, CRSOther,
    FATCADividends, FATCAInterest, FATCAGrossProceedsOrRedemptions, FATCAOther
  )

  def values(regimeType: RegimeType, showCrsNonCashValueInsuranceValues: Boolean) = regimeType match {
    case FATCA => fataValues
    case CRS  if showCrsNonCashValueInsuranceValues => crsValues
    case CRS   =>  crsNonCashValueInsuranceValues
    case _     => Seq()
  }

  def options(regimeType: RegimeType, showCrsNonCashValueInsuranceValues: Boolean)(implicit messages: Messages): Seq[RadioItem] = values(regimeType, showCrsNonCashValueInsuranceValues).zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"account.paymentType.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[PaymentType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
