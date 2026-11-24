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

package viewmodels.checkAnswers.manual.cpso

import models.{CheckMode, ReportId, UserAnswers}
import models.viewModels.AccountHolders
import pages.manual.cpso.{AccountHolderIdsPage, CurrentCPSOIdPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CPSOLinkedAccountHoldersSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] = {
    val regime = reportId.regime.toString.toLowerCase
    for {
      cpsoId      <- answers.get(CurrentCPSOIdPage()(reportId))
      selectedIds <- answers.get(AccountHolderIdsPage(cpsoId, reportId)).filter(_.nonEmpty)
      holders = AccountHolders
        .existingAccountHolders(answers)
        .filter {
          case (id, _) => selectedIds.contains(id)
        }
        .sortBy {
          case (id, holder) => (holder.getName(), id.value)
        }
      if holders.nonEmpty
    } yield SummaryListRowViewModel(
      key = s"cpso.accountHolders.checkYourAnswersLabel.$regime",
      value = ValueViewModel(htmlList(holders.map(_._2.getName()))),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          controllers.manual.cpso.routes.AccountHolderController.onPageLoad(CheckMode).url
        ).withVisuallyHiddenText(messages(s"cpso.accountHolders.change.hidden.$regime"))
      )
    )
  }

  private def htmlList(values: Seq[String]): HtmlContent =
    HtmlContent(values.map(HtmlFormat.escape(_).toString).mkString("<br>"))
}
