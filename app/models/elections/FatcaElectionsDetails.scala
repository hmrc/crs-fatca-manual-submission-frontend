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

package models.elections

import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.all.SummaryListRowViewModel

case class FatcaElectionsDetails(
  hasThresholds: Option[YesNoNa],
  hasTreasuryRegulations: Option[YesNoNa]
)

object FatcaElectionsDetails:
  given OFormat[FatcaElectionsDetails] = Json.format[FatcaElectionsDetails]

  def rows(details: FatcaElectionsDetails): Seq[SummaryListRow] =
    Seq(
      details.hasThresholds.map(
        value =>
          SummaryListRowViewModel(
            key = Key(content = Text("Has CARF")),
            value = Value(content = Text(value.toString))
          )
      ),
      details.hasTreasuryRegulations.map(
        value =>
          SummaryListRowViewModel(
            key = Key(content = Text("Has Contracts")),
            value = Value(content = Text(value.toString))
          )
      )
    ).flatten
