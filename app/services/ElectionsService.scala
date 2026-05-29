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

package services

import connectors.ElectionsConnector
import models.elections.{CrsElectionsDetails, ElectionDetails, FatcaElectionsDetails}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ElectionsService @Inject() (connector: ElectionsConnector)(using ec: ExecutionContext) {

  def getElectionsRows(fiId: String, year: Int)(using hc: HeaderCarrier, messages: Messages): Future[ElectionsRows] =
    connector
      .viewElections(fiId, Some(year))
      .map(_.headOption)
      .map {
        case Some(elections) => prepareElectionsRows(elections, year)
        case None =>
          ElectionsRows(
            crsRows = SummaryList(rows = Seq.empty),
            fatcaRows = SummaryList(rows = Seq.empty)
          )
      }

  private def prepareElectionsRows(electionsDetails: ElectionDetails, year: Int)(using messages: Messages): ElectionsRows = {
    val crsRows = electionsDetails.crs.fold(Seq.empty[SummaryListRow])(
      details => CrsElectionsDetails.rows(details, year)
    )
    val fatcaRows = electionsDetails.fatca.fold(Seq.empty[SummaryListRow])(
      details => FatcaElectionsDetails.rows(details)
    )

    ElectionsRows(
      crsRows = SummaryList(rows = crsRows),
      fatcaRows = SummaryList(rows = fatcaRows)
    )
  }
}

case class ElectionsRows(
  crsRows: SummaryList,
  fatcaRows: SummaryList
)
