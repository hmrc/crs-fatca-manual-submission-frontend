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

import models.{CheckMode, ReportId, UserAnswers}
import pages.SponsorResidentForTaxPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SponsorResidentForTaxSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    answers.get(SponsorResidentForTaxPage()).map {
      answer =>
        SummaryListRowViewModel(
          key = "sponsorResidentForTax.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape("answer").toString),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("sponsorResidentForTax.change.hidden"))
          )
        )
    }
}
