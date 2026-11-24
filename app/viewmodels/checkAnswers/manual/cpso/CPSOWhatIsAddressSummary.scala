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
import models.response.Address
import models.viewModels.manual.cpso.CPSOId
import models.{AddressNonUk, CheckMode, Countries, ReportId, UkAddress, UserAnswers}
import pages.manual.cpso.*
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CPSOWhatIsAddressSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    for {
      currentCPSOID <- answers.get(CurrentCPSOIdPage()(reportId))
      addressHtml <-
        answers
          .get(WhatIsAddressPage(currentCPSOID, reportId))
          .map(formatAddress)
          .orElse(answers.get(AddressUkPage(currentCPSOID, reportId)).map(formatUkAddress))
          .orElse(answers.get(CPSOAddressNonUkPage(currentCPSOID, reportId)).map(formatNonUkAddress))
          .orElse(answers.get(SelectAddressPage(currentCPSOID, reportId)).map(formatAddress))
    } yield {
      val regime = reportId.regime.toString.toLowerCase

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

  private def formatLine(line: String): String =
    s"""<span class="govuk-!-margin-bottom-0">${HtmlFormat.escape(line)}</span><br>"""

  private def formatLastLine(line: String): String =
    s"""<span>${HtmlFormat.escape(line)}</span><br>"""

  private def formatAddress(address: Address): String =
    formatLine(address.addressLine1) +
      address.addressLine2.fold("")(formatLine) +
      address.addressLine3.fold("")(formatLine) +
      address.addressLine4.fold("")(formatLine) +
      formatLine(address.town) +
      address.postCode.fold("")(formatLine) +
      formatLastLine(address.country.description)

  private def formatUkAddress(address: UkAddress)(implicit reportId: ReportId): String =
    formatLine(address.addressLine1) +
      address.addressLine2.fold("")(formatLine) +
      formatLine(address.city) +
      address.county.fold("")(formatLine) +
      formatLine(address.postcode) +
      formatLastLine(getCountryDescription(address.country))

  private def formatNonUkAddress(address: AddressNonUk)(implicit reportId: ReportId): String =
    formatLine(address.addressLine1) +
      address.addressLine2.fold("")(formatLine) +
      formatLine(address.addressLine3) +
      address.addressLine4.fold("")(formatLine) +
      address.postcode.fold("")(formatLine) +
      formatLastLine(getCountryDescription(address.country))

}
