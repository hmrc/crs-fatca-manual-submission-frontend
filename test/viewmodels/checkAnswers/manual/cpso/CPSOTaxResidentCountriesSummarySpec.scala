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

import base.SpecBase
import models.{ReportId, TaxResidentInformation}
import models.SubmissionsConstants.CRS
import models.response.Country
import models.viewModels.manual.cpso.CPSOId
import pages.manual.cpso.{CurrentCPSOIdPage, TaxResidentCountriesListPage}
import pages.manual.cpso.TaxIdentificationNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.all.*

class CPSOTaxResidentCountriesSummarySpec extends SpecBase {

  private val cpsoId = CPSOId("cpso-id")

  ".row" - {

    implicit val messages: Messages = stubMessages()
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "testFIID")

    "must show countries and group multiple identifiers by country" in {
      val spain = Country("ES", "Spain")
      val ua = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(
          TaxResidentCountriesListPage(cpsoId, reportId),
          Seq(
            TaxResidentInformation(spain),
            TaxResidentInformation(spain),
            TaxResidentInformation(Country("FR", "France"))
          )
        )
        .withPage(TaxIdentificationNumberPage(0, cpsoId, reportId), "123456789")
        .withPage(TaxIdentificationNumberPage(1, cpsoId, reportId), "65342324")

      val rows = CPSOTaxResidentCountriesSummary.row(ua).head.rows

      rows mustBe Seq(
        SummaryListRowViewModel(
          key = "cpso.taxResidentCountries.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent("Spain<br>France"))
        ),
        SummaryListRowViewModel(
          key = "cpso.taxResidentCountries.taxIdentifiers",
          value = ValueViewModel(HtmlContent("123456789<br>65342324"))
        )
      )
    }

    "must use the singular label for one identifier and omit countries without identifiers" in {
      val ua = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(
          TaxResidentCountriesListPage(cpsoId, reportId),
          Seq(
            TaxResidentInformation(Country("ES", "Spain")),
            TaxResidentInformation(Country("FR", "France"))
          )
        )
        .withPage(TaxIdentificationNumberPage(0, cpsoId, reportId), "123456789")

      val rows = CPSOTaxResidentCountriesSummary.row(ua).head.rows

      rows mustBe Seq(
        SummaryListRowViewModel(
          key = "cpso.taxResidentCountries.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent("Spain<br>France"))
        ),
        SummaryListRowViewModel(
          key = "cpso.taxResidentCountries.taxIdentifier",
          value = ValueViewModel(HtmlContent("123456789"))
        )
      )
    }

    "must not create a summary when there are no resident countries" in {
      val ua = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(TaxResidentCountriesListPage(cpsoId, reportId), Seq.empty)

      CPSOTaxResidentCountriesSummary.row(ua) mustBe empty
    }
  }
}
