/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels.checkAnswers.manual.sponsor

import base.SpecBase
import models.SubmissionsConstants.CRS
import models.response.Country
import models.{ReportId, UkAddress}
import org.scalatest.freespec.AnyFreeSpec
import pages.ReportIdPage
import pages.manual.FINamePage
import pages.manual.sponsor.*
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class CheckAnswersSummarySpec extends SpecBase {

  ".apply" - {

    implicit val messages: Messages = stubMessages()
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "testFIID")
    val fiName                      = "TestFiName"

    "must return SummaryList with basic Only" in {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(HaveSponsorPage(), false)
        .withPage(FINamePage(), fiName)

      val checkAnswersSummary = CheckAnswersSummary(ua)

      val rows = checkAnswersSummary.basic.rows
      rows.size mustBe 1
      checkAnswersSummary.taxResidentCountriesSummary.isDefined mustBe false
    }

    "must return SummaryList with basic & taxResidentCountries" in {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(HaveSponsorPage(), true)
        .withPage(SponsorNamePage(), "Test Sponsor")
        .withPage(WhatIsGIINForSponsorPage(), "alphvA.zDSJH.HV.255")
        .withPage(UkAddressPage(), UkAddress("address line 1", None, "city", None, "postcode", "country"))
        .withPage(TaxResidentCountriesListPage(), Seq(Country("XX", "Test Country")))
        .withPage(FINamePage(), fiName)

      val checkAnswersSummary = CheckAnswersSummary(ua)

      val rows = checkAnswersSummary.basic.rows
      rows.size mustBe 4
      checkAnswersSummary.taxResidentCountriesSummary.isDefined mustBe true
    }

  }
}
