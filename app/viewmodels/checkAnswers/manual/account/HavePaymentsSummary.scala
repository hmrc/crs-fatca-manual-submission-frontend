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

package viewmodels.checkAnswers.manual.account

import controllers.manual.account.routes
import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.account.{CurrentAccountIdPage, HavePaymentsPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object HavePaymentsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    for {
      accountId <- answers.get(CurrentAccountIdPage())
      answer    <- answers.get(HavePaymentsPage(accountId))
    } yield {
      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = "havePayments.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel("site.change", routes.HavePaymentsController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("havePayments.change.hidden"))
        )
      )
    }
}
