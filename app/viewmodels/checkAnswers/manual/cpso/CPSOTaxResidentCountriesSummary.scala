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

import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.cpso.{CurrentCPSOIdPage, TaxIdentificationNumberPage, TaxResidentCountriesListPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CPSOTaxResidentCountriesSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Seq[SummaryList] =
    for {
      currentCPSOId <- answers.get(CurrentCPSOIdPage()(reportId)).toSeq
      countries     <- answers.get(TaxResidentCountriesListPage(currentCPSOId, reportId)).filter(_.nonEmpty).toSeq
    } yield {
      val countryDescriptions = countries.map(_.country.description).distinct
      val taxIdsByCountry = countries.zipWithIndex
        .flatMap {
          case (country, index) =>
            answers
              .get(TaxIdentificationNumberPage(index, currentCPSOId, reportId))
              .map(country.country.description -> _)
        }
        .groupMap(_._1)(_._2)

      val countriesRow = SummaryListRowViewModel(
        key = "cpso.taxResidentCountries.checkYourAnswersLabel",
        value = ValueViewModel(htmlList(countryDescriptions))
      )

      val identifierRows = for {
        country <- countryDescriptions
        ids     <- taxIdsByCountry.get(country).filter(_.nonEmpty)
      } yield SummaryListRowViewModel(
        key = if (ids.size > 1) messages("cpso.taxResidentCountries.taxIdentifiers", country)
        else messages("cpso.taxResidentCountries.taxIdentifier", country),
        value = ValueViewModel(htmlList(ids))
      )

      SummaryListViewModel(
        rows = countriesRow +: identifierRows,
        card = CardViewModel(
          title = CardTitle(content = "cpso.taxResidentCountries.checkYourAnswersSummaryHeading"),
          actions = Seq(
            ActionItemViewModel(
              href = controllers.manual.cpso.routes.TaxResidentCountriesController.onPageLoad(CheckMode).url,
              content = "cpso.taxResidentCountries.checkYourAnswersLink"
            )
          )
        ).withCssClass("govuk-!-margin-bottom-6")
      )
    }

  private def htmlList(values: Seq[String]): HtmlContent =
    HtmlContent(values.map(HtmlFormat.escape(_).toString).mkString("<br>"))
}
