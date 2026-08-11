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

package viewmodels.checkAnswers.manual.sponsor

import models.{ReportId, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.{AddressNonUkSummary, UkAddressSummary}
import viewmodels.govuk.summarylist.*

case class CheckAnswersSummary(basic: SummaryList, taxResidentCountriesSummary: Option[SummaryList])

object CheckAnswersSummary {

  def apply(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): CheckAnswersSummary =
    CheckAnswersSummary(
      basic = SummaryListViewModel(
        rows = Seq(
          HaveSponsorSummary.row(answers),
          SponsorNameSummary.row(answers),
          WhatIsGIINForSponsorSummary.row(answers),
          WhatIsAddressForSponsorSummary.row(answers),
          UkAddressSummary.row(answers),
          AddressNonUkSummary.row(answers)
        ).flatten
      ),
      taxResidentCountriesSummary = TaxResidentCountriesSummary.row(answers)
    )
}
