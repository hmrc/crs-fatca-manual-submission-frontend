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

package utils

import models.SubmissionsConstants.*
import models.response.FIDetail
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import viewmodels.govuk.all.SummaryListViewModel

class CheckDetailsForFinancialInstitutionUtil {

  def getRows(fIDetail: FIDetail, regime: RegimeType)(implicit messages: Messages): SummaryList = {

    val addressRow =
      toRow(messages("CheckDetailsForFinancialInstitution.value.address"), Some(fIDetail.AddressDetails.formatAddressDetails)) // todo fix format

    val giinRows: Seq[SummaryListRow] = if (regime == FATCA) {
      fIDetail.GIIN match {
        case Some(giin) => toRow(messages("CheckDetailsForFinancialInstitution.key.giin"), fIDetail.GIIN)
        case None       => toRow(messages("CheckDetailsForFinancialInstitution.key.noGiin"), Some(messages("CheckDetailsForFinancialInstitution.value.noGiin")))
      }
    } else { Seq.empty }

    val tinRows: Seq[SummaryListRow] = {
      val noTIN = toRow(messages("CheckDetailsForFinancialInstitution.key.noTin"), Some(messages("CheckDetailsForFinancialInstitution.value.noTin")))

      if (regime == CRS) {
        fIDetail.TINDetails
          .map(
            _.flatMap(
              tin => toRow(tin.TINType.toString, Some(tin.TIN))
            )
          )
          .getOrElse(noTIN)
      } else { Seq.empty }
    }

    SummaryListViewModel(
      rows = toRow(messages("CheckDetailsForFinancialInstitution.key.fiName"), Some(fIDetail.FIName)) ++
        addressRow ++
        giinRows ++ tinRows
    )
  }

  private def toRow(label: String, value: Option[String]): Seq[SummaryListRow] =
    value
      .map(
        v => SummaryListRow(Key(HtmlContent(label)), Value(HtmlContent(v)))
      )
      .toSeq
}
