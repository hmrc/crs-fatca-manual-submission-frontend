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

import models.{ReportId, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
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

}
