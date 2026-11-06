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

import models.manual.TaxResidentInformation
import models.viewModels.AccountHolderId
import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.accountHolders.TaxResidentCountriesListPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AccountHolderTaxResidentCountriesSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId, accountHolderId: AccountHolderId): Seq[SummaryList] =
    answers.get(TaxResidentCountriesListPage(accountHolderId = accountHolderId, reportId = reportId)).filter(_.nonEmpty).fold(Seq.empty[SummaryList]) {
      countries =>

        val identRows: Seq[SummaryListRow] =
          countries.map {
            country =>
              SummaryListRowViewModel(
                key = s"Identification number for ${country.country.description}",
                value = ValueViewModel(
                  HtmlContent(country.taxIdentifierNumber.mkString("<br>"))
                )
              )
          }

        Seq(
          SummaryListViewModel(
            rows = Seq(
              SummaryListRowViewModel(
                key = "accountHolders.taxResidentCountries.checkYourAnswersLabel",
                value = ValueViewModel(
                  HtmlContent(countries.map(_.country.description).mkString("<br>"))
                )
              )
            ) ++ identRows,
            card = CardViewModel(
              title = CardTitle(
                content = "accountHolders.taxResidentCountries.checkYourAnswersSummaryHeading"
              ),
              actions = Seq(
                ActionItemViewModel(
                  href = controllers.manual.accountHolders.routes.TaxResidentCountriesController.onPageLoad(CheckMode).url,
                  content = "accountHolders.taxResidentCountries.checkYourAnswersLink"
                )
              )
            ).withCssClass("govuk-!-margin-bottom-6")
          )
        )
    }
}
