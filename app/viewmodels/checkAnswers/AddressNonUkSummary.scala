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
import pages.manual.sponsor.AddressNonUkPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AddressNonUkSummary {

  def row(
    answers: UserAnswers
  )(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    answers.get(AddressNonUkPage()).map {
      answer =>

        def formatLine(line: String): String =
          s"""<div class="govuk-margin-bottom-0">${HtmlFormat.escape(line)}</div>"""

        val addressHtml: String =
          formatLine(answer.addressLine1) concat
            answer.addressLine2.fold("")(formatLine) concat
            formatLine(answer.addressLine3) concat
            answer.addressLine4.fold("")(formatLine) concat
            answer.postcode.fold("")(formatLine) concat
            formatLine(answer.country)

        SummaryListRowViewModel(
          key = "addressNonUk.checkYourAnswersLabel",
          value = ValueViewModel(
            HtmlContent(addressHtml)
          ),
          actions = Seq(
            ActionItemViewModel(
              content = HtmlContent(
                s"""
                 |<span aria-hidden="true">${messages("site.change")}</span>
                 |<span class="govuk-visually-hidden">${messages("addressNonUk.change.hidden")}</span>
                 |""".stripMargin
              ),
              href = controllers.manual.sponsor.routes.AddressNonUkController
                .onPageLoad(CheckMode)
                .url
            ).withAttribute(
              "id" -> "addressNonUk"
            )
          )
        )
    }
}
