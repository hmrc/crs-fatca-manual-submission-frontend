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

package viewmodels.checkAnswers

import models.{ElectionsId, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist.*

object CheckYourAnswersElections {

  def apply(answers: UserAnswers, year: Int)(implicit messages: Messages, electionsId: ElectionsId): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        CRSContractsSummary.row(answers, year),
        CRSDormantAccountsSummary.row(answers, year),
        CRSThresholdsSummary.row(answers, year),
        CarfGrossProceedsSummary.row(answers, year),
        CrsGrossProceedsSummary.row(answers, year),
        IsUsTreasuryRegulatedSummary.row(answers, year),
        IsApplyingThresholdsSummary.row(answers, year)
      ).flatten
    )
}
