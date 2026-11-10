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

import base.SpecBase
import models.ReportId
import models.SubmissionsConstants.CRS
import models.manual.TaxResidentInformation
import models.response.Country
import models.viewModels.AccountHolderId
import pages.manual.accountHolders.TaxResidentCountriesListPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.all.*

class AccountHolderTaxResidentCountriesSummarySpec extends SpecBase {

  ".row" - {

    implicit val messages: Messages               = stubMessages()
    implicit val reportId: ReportId               = ReportId(CRS, 2025, None, "testFIID")
    implicit val accountHolderId: AccountHolderId = AccountHolderId("holder-id")

    "must group multiple identifiers by country" in {
      val spain = Country("ES", "Spain")
      val ua = emptyUserAnswers.withPage(
        TaxResidentCountriesListPage(accountHolderId, reportId),
        Seq(
          TaxResidentInformation(spain, Some(true), Some("123456789")),
          TaxResidentInformation(spain, Some(true), Some("65342324")),
          TaxResidentInformation(Country("FR", "France"), Some(false), None)
        )
      )

      val identRows = AccountHolderTaxResidentCountriesSummary.row(ua).head.rows.drop(1)

      identRows mustBe Seq(
        SummaryListRowViewModel(
          key = "accountHolders.taxResidentCountries.taxIdentifiers",
          value = ValueViewModel(HtmlContent("123456789<br>65342324"))
        )
      )
    }

    "must use the singular key when a country has one identifier" in {
      val ua = emptyUserAnswers.withPage(
        TaxResidentCountriesListPage(accountHolderId, reportId),
        Seq(TaxResidentInformation(Country("ES", "Spain"), Some(true), Some("123456789")))
      )

      val identRows = AccountHolderTaxResidentCountriesSummary.row(ua).head.rows.drop(1)

      identRows mustBe Seq(
        SummaryListRowViewModel(
          key = "accountHolders.taxResidentCountries.taxIdentifier",
          value = ValueViewModel(HtmlContent("123456789"))
        )
      )
    }

    "must not create rows for countries without identifiers" in {
      val ua = emptyUserAnswers.withPage(
        TaxResidentCountriesListPage(accountHolderId, reportId),
        Seq(TaxResidentInformation(Country("FR", "France"), Some(false), None))
      )

      val identRows = AccountHolderTaxResidentCountriesSummary.row(ua).head.rows.drop(1)

      identRows mustBe empty
    }
  }
}
