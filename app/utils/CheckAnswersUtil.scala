/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import models.NumberType.{Iban, Semp}
import models.SubmissionsConstants.{CRS, FATCA}
import models.{ReportId, UserAnswers}
import pages.manual.account.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.manual.account.*
import viewmodels.checkAnswers.manual.filercategory.{WhatTypeOfFilerIsSponsorSummary, WhatTypeOfFilerSummary}
import viewmodels.checkAnswers.{CrsOrFatcaSummary, ReportingYearSummary, TypeOfReportSummary}
import viewmodels.govuk.all.SummaryListViewModel
import viewmodels.govuk.summarylist.FluentSummaryList

import javax.inject.{Inject, Singleton}

@Singleton
class CheckAnswersUtil @Inject() {

  def getReportDetailsRows(ua: UserAnswers)(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(CrsOrFatcaSummary.row(ua), ReportingYearSummary.row(ua), TypeOfReportSummary.row(ua)).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  def getFilerCategoryRows(ua: UserAnswers)(implicit messages: Messages, reportId: ReportId): SummaryList =
    SummaryListViewModel(
      rows = Seq(WhatTypeOfFilerIsSponsorSummary.row(ua), WhatTypeOfFilerSummary.row(ua)).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  def getAccountRows(ua: UserAnswers)(implicit messages: Messages, reportId: ReportId): SummaryList = {

    val regime = reportId.regime

    val rows: Seq[SummaryListRow] =
      (for {
        currentAccountId   <- ua.get(CurrentAccountIdPage()(reportId))
        haveOfficialNumber <- ua.get(HaveNumberPage(currentAccountId)(reportId))
        isClosed           <- ua.get(AccountClosedPage(currentAccountId)(reportId))
        hasJointAccounts   <- Some(ua.get(IsJointAccountPage(currentAccountId)(reportId)).contains(true))
        isIbanOrSemp       <- ua.get(NumberTypePage(currentAccountId)(reportId)).map(Seq(Iban, Semp).contains)
      } yield {
        val haveNumberSummary       = Seq(HaveNumberSummary.row(ua)).flatten
        val unofficialNumberSummary = if (!haveOfficialNumber) Seq(IdentifierSummary.row(ua)).flatten else Seq.empty
        val officialNumberSummaries = if (haveOfficialNumber) Seq(NumberTypeSummary.row(ua), WhatIsAccountNumberSummary.row(ua)).flatten else Seq.empty

        val jointAccountSummaries =
          if (hasJointAccounts) Seq(IsJointAccountSummary.row(ua), HowManyJointAccountHoldersSummary.row(ua)).flatten
          else Seq(IsJointAccountSummary.row(ua)).flatten
        val accountTypeSummary =
          if (!isIbanOrSemp) Seq(WhatAccountTypeSummary.row(ua)).flatten
          else Seq.empty
        val accountClosedSummary = Seq(AccountClosedSummary.row(ua)).flatten
        val currSummary          = if (isClosed && regime == CRS) Seq(WhatWasTheAccountCurrencySummary.row(ua)).flatten else Seq.empty
        val balanceSummary =
          if (regime == FATCA || (regime == CRS && !isClosed)) Seq(WhatWasTheAccountBalanceSummary.row(ua)).flatten
          else Seq.empty

        val crsOnlySummaries =
          if (regime == CRS) {
            Seq(IsUndocumentedAccountSummary.row(ua),
                IsDormantAccountSummary.row(ua),
                WasAccountOpenSummary.row(ua)
            ).flatten ++ jointAccountSummaries ++ accountTypeSummary
          } else Seq.empty

        val paymentsSummary = Seq(HavePaymentsSummary.row(ua)).flatten

        haveNumberSummary ++
          unofficialNumberSummary ++
          officialNumberSummaries ++
          accountClosedSummary ++
          currSummary ++
          balanceSummary ++
          crsOnlySummaries ++
          paymentsSummary
      }).getOrElse {
        Seq.empty
      }

    SummaryListViewModel(rows = rows).withCssClass("govuk-!-margin-bottom-3")
  }

}
