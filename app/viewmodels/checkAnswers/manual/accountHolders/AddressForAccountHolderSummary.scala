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

import models.ServiceErrors.CountryLookup_Error
import models.{AddressNonUk, CheckMode, Countries, ReportId, UkAddress, UserAnswers}
import models.response.Address
import pages.manual.accountHolders.{AccountHolderAddressNonUkPage, CurrentAccountHolderIdPage, UkAddressPage, WhatIsAddressForAccountHolderPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AddressForAccountHolderSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    for {
      currentAccountHolderId <- answers.get(CurrentAccountHolderIdPage()(reportId))
      addressHtml <- answers
        .get(WhatIsAddressForAccountHolderPage(currentAccountHolderId, reportId))
        .map(formatAddress)
        .orElse(answers.get(UkAddressPage(currentAccountHolderId, reportId)).map(formatUkAddress))
        .orElse(answers.get(AccountHolderAddressNonUkPage(currentAccountHolderId, reportId)).map(formatNonUkAddress))
    } yield SummaryListRowViewModel(
      key = "whatIsAddressForAccountHolder.checkYourAnswersLabel",
      value = ValueViewModel(
        HtmlContent(addressHtml)
      ),
      actions = Seq(
        ActionItemViewModel("site.change", controllers.manual.accountHolders.routes.WhereAreTheyBasedController.onPageLoad(CheckMode).url)
          .withVisuallyHiddenText(messages("whatIsAddressForAccountHolder.change.hidden"))
      )
    )

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
      formatLastLine(countryDescription(address.country))

  private def formatNonUkAddress(address: AddressNonUk)(implicit reportId: ReportId): String =
    formatLine(address.addressLine1) +
      address.addressLine2.fold("")(formatLine) +
      formatLine(address.addressLine3) +
      address.addressLine4.fold("")(formatLine) +
      address.postcode.fold("")(formatLine) +
      formatLastLine(countryDescription(address.country))

  private def countryDescription(code: String)(implicit reportId: ReportId): String =
    Countries
      .allCountries(reportId.regime)
      .find(_.code == code)
      .map(_.description)
      .getOrElse(throw CountryLookup_Error)
}
