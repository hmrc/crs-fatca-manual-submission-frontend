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

import controllers.manual.cpso.routes
import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.cpso.{CurrentCPSOIdPage, RemoveTaxResidentCountryPage, TaxResidentCountriesListPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.CardTitle
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object TaxResidentCountriesSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryList] =
    for {
      currentAccountHolderId <- answers.get(CurrentCPSOIdPage()(reportId))
      answer                 <- answers.get(TaxResidentCountriesListPage(currentAccountHolderId, reportId))
    } yield {

      val countryList = answer.map(_.country.description).mkString("<br>")

      SummaryListViewModel(
        rows = Seq(
          SummaryListRowViewModel(
            key = "taxResidentCountries.checkYourAnswersLabel",
            value = ValueViewModel(
              HtmlContent(countryList)
            )
          )
        ),
        card = CardViewModel(
          title = CardTitle(
            content = "taxResidentCountries.checkYourAnswersSummaryHeading"
          ),
          actions = Seq(
            ActionItemViewModel(
              href = controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(CheckMode).url,
              content = "taxResidentCountries.checkYourAnswersLink"
            )
          )
        ).withCssClass("govuk-!-margin-bottom-6")
      )
    }
}
