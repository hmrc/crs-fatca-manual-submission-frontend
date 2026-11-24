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

import models.Countries.getCountryDescription
import models.{CheckMode, Countries, ReportId, UserAnswers}
import pages.manual.cpso.{CurrentCPSOIdPage, IndividualPlaceOfBirthPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object IndividualPlaceOfBirthSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] = {
    val regime = reportId.regime.toString.toLowerCase
    for {
      currentId <- answers.get(CurrentCPSOIdPage()(reportId))
      answer    <- answers.get(IndividualPlaceOfBirthPage(currentId, reportId))
    } yield

      def formatLine(line: String): String =
        s"""<span class="govuk-!-margin-bottom-0">${HtmlFormat.escape(line)}</span><br>"""

      def formatLastLine(line: String): String = s"""<span>${HtmlFormat.escape(line)}</span><br>"""

      val addressHtml: String =
        answer.city.fold("")(formatLine) concat
          answer.region.fold("")(formatLine) concat
          formatLastLine(getCountryDescription(answer.country))

      SummaryListRowViewModel(
        key = s"cpso.individualPlaceOfBirth.checkYourAnswersLabel.$regime",
        value = ValueViewModel(HtmlContent(addressHtml)),
        actions = Seq(
          ActionItemViewModel("site.change", controllers.manual.cpso.routes.IndividualPlaceOfBirthController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages(s"cpso.individualPlaceOfBirth.change.hidden.$regime"))
        )
      )
  }
}
