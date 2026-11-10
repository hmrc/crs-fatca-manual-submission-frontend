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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AccountHolderTaxResidentCountriesSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId, accountHolderId: AccountHolderId): Seq[SummaryList] =
    answers
      .get(TaxResidentCountriesListPage(accountHolderId = accountHolderId, reportId = reportId))
      .filter(_.nonEmpty)
      .fold(Seq.empty[SummaryList]) {
        countries =>

          val countryDescriptions: Seq[String] = countries.map(_.country.description).distinct
          val taxIdsByCountry: Map[String, Seq[String]] =
            countries.groupMapReduce(_.country.description)(_.taxIdentifierNumber.toSeq)(_ ++ _)

          val countriesRow = SummaryListRowViewModel(
            key = "accountHolders.taxResidentCountries.checkYourAnswersLabel",
            value = ValueViewModel(htmlList(countryDescriptions))
          )

          val identRows: Seq[SummaryListRow] = for {
            country <- countryDescriptions
            ids     <- taxIdsByCountry.get(country).filter(_.nonEmpty)
          } yield SummaryListRowViewModel(
            key = if (ids.size > 1) { messages("accountHolders.taxResidentCountries.taxIdentifiers", country) }
            else { messages("accountHolders.taxResidentCountries.taxIdentifier", country) },
            value = ValueViewModel(htmlList(ids))
          )

          Seq(
            SummaryListViewModel(
              rows = countriesRow +: identRows,
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

  private def htmlList(values: Seq[String]): HtmlContent =
    HtmlContent(values.map(HtmlFormat.escape(_).toString).mkString("<br>"))

}
