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

import controllers.manual.accountHolders.routes
import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.account.AccountsPage
import pages.manual.accountHolders.{AccountPage, CurrentAccountHolderIdPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AccountsSummary {

  def row(ua: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    for {
      currentAccountHolderId <- ua.get(CurrentAccountHolderIdPage()(reportId))
      accountsModel          <- ua.get(AccountsPage(reportId))
      selectedAccountIds     <- ua.get(AccountPage(reportId, currentAccountHolderId))
    } yield {

      val accountNumbers: Seq[String] =
        accountsModel.accounts.values
          .filter(
            account => selectedAccountIds.contains(account.accountId)
          )
          .flatMap(_.accountNumber)
          .toSeq

      val value = ValueViewModel(
        HtmlContent(
          accountNumbers
            .map(HtmlFormat.escape)
            .mkString(",<br>")
        )
      )

      SummaryListRowViewModel(
        key = "accountHolder.account.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.AccountController.onPageLoad(CheckMode).url
          )
        )
      )
    }
}
