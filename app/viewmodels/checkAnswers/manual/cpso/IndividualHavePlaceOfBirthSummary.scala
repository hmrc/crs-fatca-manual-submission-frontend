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

import controllers.manual.cpso.routes
import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.cpso.{CurrentCPSOIdPage, IndividualHavePlaceOfBirthPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object IndividualHavePlaceOfBirthSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    for {
      currentId <- answers.get(CurrentCPSOIdPage()(reportId))
      answer    <- answers.get(IndividualHavePlaceOfBirthPage(currentId, reportId))
    } yield
      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = "individualHavePlaceOfBirth.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel("site.change", routes.IndividualHavePlaceOfBirthController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("individualHavePlaceOfBirth.change.hidden"))
        )
      )
}
