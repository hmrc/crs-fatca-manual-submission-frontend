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

package utils.manual.accountholders

import models.{ReportId, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.manual.accountHolders.{
  AccountsSummary,
  IndividualNameSummary,
  IndividualOrOrganisationSummary,
  OrganisationCrsSummary,
  OrganisationNameSummary
}
import viewmodels.checkAnswers.manual.sponsor.WhatIsAddressForAccountHolderSummary
import viewmodels.govuk.all.SummaryListViewModel
import viewmodels.govuk.summarylist.FluentSummaryList

import javax.inject.{Inject, Singleton}

@Singleton
class CheckAnswersUtil @Inject() {

  def rows(ua: UserAnswers)(implicit messages: Messages, reportId: ReportId): SummaryList = {

    val rows: Seq[SummaryListRow] = Seq(
      AccountsSummary.row(ua),
      IndividualOrOrganisationSummary.row(ua),
      IndividualNameSummary.row(ua),
      OrganisationNameSummary.row(ua),
      OrganisationCrsSummary.row(ua),
      WhatIsAddressForAccountHolderSummary.row(ua)
    ).flatten

    SummaryListViewModel(rows = rows).withCssClass("govuk-!-margin-bottom-3")
  }

}
