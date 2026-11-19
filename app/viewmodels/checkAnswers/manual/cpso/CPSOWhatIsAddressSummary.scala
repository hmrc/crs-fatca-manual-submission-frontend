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

import models.{CheckMode, ReportId, UkAddress, UserAnswers}
import models.viewModels.manual.cpso.CPSOId
import pages.manual.cpso.{AddressUkPage, CurrentCPSOIdPage, SelectAddressPage, WhatIsAddressPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CPSOWhatIsAddressSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] = {
    val regime = reportId.regime.toString.toLowerCase
    for {
      currentCPSOID <- answers.get(CurrentCPSOIdPage()(reportId))
      answer        <- resolveUkAddress(answers, currentCPSOID)
    } yield {

      def formatLine(line: String): String =
        s"""<span class="govuk-!-margin-bottom-0">${HtmlFormat.escape(line)}</span><br>"""

      def formatLastLine(line: String): String =
        s"""<span>${HtmlFormat.escape(line)}</span><br>"""

      val addressHtml: String =
        formatLine(answer.addressLine1) concat
          answer.addressLine2.fold("")(formatLine) concat
          answer.county.fold("")(formatLine) concat
          formatLine(answer.city) concat
          answer.postcode concat
          formatLastLine(answer.country)

      SummaryListRowViewModel(
        key = "cpso.whatIsAddress.checkYourAnswersLabel",
        value = ValueViewModel(
          HtmlContent(addressHtml)
        ),
        actions = Seq(
          ActionItemViewModel("site.change", controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages(s"cpso.whatIsAddress.change.hidden.$regime"))
        )
      )
    }
  }

  private def resolveUkAddress(userAnswers: UserAnswers, cpsoId: CPSOId)(implicit reportId: ReportId): Option[UkAddress] =
    userAnswers
      .get(WhatIsAddressPage(cpsoId, reportId))
      .map(_.ukAddress)
      .orElse(userAnswers.get(AddressUkPage(cpsoId, reportId)))
      .orElse(userAnswers.get(SelectAddressPage(cpsoId, reportId)).map(_.ukAddress))
}
