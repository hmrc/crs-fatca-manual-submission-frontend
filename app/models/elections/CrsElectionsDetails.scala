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

import models.elections
import models.elections.YesNoNa.*
import play.api.i18n.Messages
import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.InputWidth
import viewmodels.govuk.all.{FluentKey, SummaryListRowViewModel}
import utils.ReportingConstants.REPORTING_THRESHOLD_YEAR

case class CrsElectionsDetails(
  hasCARF: Option[YesNoNa],
  hasContracts: Option[YesNoNa],
  hasDormantAccounts: Option[YesNoNa],
  hasThresholds: Option[YesNoNa]
)

object CrsElectionsDetails:
  given OFormat[CrsElectionsDetails] = Json.format[CrsElectionsDetails]

  def rows(details: CrsElectionsDetails, selectedYear: Int)(using messages: Messages): Seq[SummaryListRow] = {
    def maybeCarfRow =
      if (selectedYear >= REPORTING_THRESHOLD_YEAR)
        Seq(
          SummaryListRowViewModel(
            key = Key(content = Text(messages("manageElections.crs.hasCARF", selectedYear.toString))).withCssClass(InputWidth.ThreeQuarters.toString),
            value = Value(content = Text(details.hasCARF.fold("Not Provided")(_.toString)))
          )
        )
      else Seq.empty

    def getValue: Option[Value] =
      details.hasCARF match {
        case Some(Yes) => Some(Value(Text(Yes.toString)))
        case Some(No)  => Some(Value(Text(No.toString)))
        case _         => None
      }

    def maybeGrossProceedsRow =
      getValue.fold(Seq.empty) {
        value =>
          Seq(
            SummaryListRowViewModel(
              key = Key(content = Text(messages("manageElections.crs.grossProceeds", selectedYear.toString))).withCssClass(InputWidth.ThreeQuarters.toString),
              value = value
            )
          )
      }

    Seq(
      SummaryListRowViewModel(
        key = Key(content = Text(messages("manageElections.crs.hasContracts"))).withCssClass(InputWidth.ThreeQuarters.toString),
        value = Value(content = Text(details.hasContracts.fold("Not Provided")(_.toString)))
      ),
      SummaryListRowViewModel(
        key = Key(content = Text(messages("manageElections.crs.hasDormantAccounts"))).withCssClass(InputWidth.ThreeQuarters.toString),
        value = Value(content = Text(details.hasDormantAccounts.fold("Not Provided")(_.toString)))
      ),
      SummaryListRowViewModel(
        key = Key(content = Text(messages("manageElections.crs.hasThresholds"))).withCssClass(InputWidth.ThreeQuarters.toString),
        value = Value(content = Text(details.hasThresholds.fold("Not Provided")(_.toString)))
      )
    ) ++ maybeCarfRow ++ maybeGrossProceedsRow

  }
