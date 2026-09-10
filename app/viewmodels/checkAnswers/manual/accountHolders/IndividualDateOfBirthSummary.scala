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

package viewmodels.checkAnswers.manual.accountHolders

import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.accountHolders.{CurrentAccountHolderIdPage, IndividualDateOfBirthPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.format.DateTimeFormatter
import java.util.Locale

object IndividualDateOfBirthSummary {

  private val dateFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    for {
      currentAccountHolderId <- answers.get(CurrentAccountHolderIdPage()(reportId))
      answer                 <- answers.get(IndividualDateOfBirthPage(currentAccountHolderId)(reportId))
    } yield SummaryListRowViewModel(
      key = "individualDateOfBirth.checkYourAnswersLabel",
      value = ValueViewModel(answer.dateOfBirth.format(dateFormatter)),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          controllers.manual.accountHolders.routes.IndividualDateOfBirthController.onPageLoad(CheckMode).url
        )
          .withVisuallyHiddenText(messages("individualDateOfBirth.change.hidden"))
      )
    )
}
