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

import models.{CheckMode, ElectionsId, UserAnswers}
import pages.CrsGrossProceedsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.InputWidth
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

object CrsGrossProceedsSummary {

  def row(answers: UserAnswers, year: Int)(implicit messages: Messages, electionsId: ElectionsId): Option[SummaryListRow] =
    answers.get(CrsGrossProceedsPage()).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = Key(messages("crsGrossProceeds.checkYourAnswersLabel")).withCssClass(InputWidth.TwoThirds.toString),
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel(
              HtmlContent(s"""<span aria-hidden="true">${messages("site.change")}</span>"""),
              controllers.elections.routes.CrsGrossProceedsController.onPageLoad(CheckMode, year).url
            ).withVisuallyHiddenText(messages("crsGrossProceeds.change.hidden"))
          )
        )
    }
}
