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

import models.{Enumerable, NumberType, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.ReportingConstants.REPORTING_THRESHOLD_YEAR

sealed trait WhatAccountType

object WhatAccountType extends Enumerable.Implicits {

  case object Depository extends WithName("CRS1101") with WhatAccountType
  case object Custodial extends WithName("CRS1102") with WhatAccountType
  case object InsuranceOrAnnuityContract extends WithName("CRS1103") with WhatAccountType
  case object InvestmentEntity extends WithName("CRS1104") with WhatAccountType
  case object NotReported extends WithName("CRS1100") with WhatAccountType

  val baseValues: Seq[WhatAccountType] = Seq(
    Depository,
    Custodial,
    InvestmentEntity
  )

  val allValidValues: Seq[WhatAccountType] = Seq(
    Depository,
    Custodial,
    InvestmentEntity,
    InsuranceOrAnnuityContract,
    NotReported
  )

  def options(numberType: NumberType, reportingPeriod: Int)(implicit messages: Messages): Seq[RadioItem] = {
    val values =
      baseValues
        .appendedAll(if (numberType == NumberType.Other) Seq(InsuranceOrAnnuityContract) else Seq.empty)
        .appendedAll(if (reportingPeriod < REPORTING_THRESHOLD_YEAR) Seq(NotReported) else Seq.empty)

    values.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"whatAccountType.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }
  }

  implicit val enumerable: Enumerable[WhatAccountType] =
    Enumerable(
      allValidValues.map(
        v => v.toString -> v
      ): _*
    )
}
