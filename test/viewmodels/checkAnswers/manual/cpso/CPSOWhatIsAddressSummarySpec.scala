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
import models.SubmissionsConstants.CRS
import models.response.{Address, Country}
import models.viewModels.manual.cpso.CPSOId
import models.{AddressNonUk, ReportId, UkAddress}
import pages.manual.cpso.*
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist.ValueViewModel

class CPSOWhatIsAddressSummarySpec extends SpecBase {

  private val cpsoId = CPSOId("cpso-id")

  private val selectedAddress = Address(
    uprn = Some(1L),
    addressLine1 = "Selected address",
    addressLine2 = None,
    addressLine3 = None,
    addressLine4 = None,
    town = "London",
    postCode = Some("SW1A 1AA"),
    country = Country("GB", "United Kingdom")
  )
  private val manualAddress = UkAddress("Manual address", None, "Manchester", postcode = "M1 1AA", country = "GB")

  private val nonUkAddress = AddressNonUk(
    addressLine1 = "Non-UK address",
    addressLine2 = Some("Second line"),
    addressLine3 = "Paris",
    addressLine4 = None,
    postcode = Some("75001"),
    country = "FR"
  )

  private val lookupAddress = Address(
    uprn = Some(2L),
    addressLine1 = "Lookup address",
    addressLine2 = None,
    addressLine3 = None,
    addressLine4 = None,
    town = "Bristol",
    postCode = Some("BS1 1AA"),
    country = Country("GB", "United Kingdom")
  )

  ".row" - {

    implicit val messages: Messages = stubMessages()
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "testFIID")

    "must use WhatIsAddressPage in preference to the other address pages" in {
      val answers = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(WhatIsAddressPage(cpsoId, reportId), selectedAddress)
        .withPage(AddressUkPage(cpsoId, reportId), manualAddress)
        .withPage(SelectAddressPage(cpsoId, reportId), lookupAddress)

      CPSOWhatIsAddressSummary.row(answers).value.value mustBe ValueViewModel(
        HtmlContent(
          """<span class="govuk-!-margin-bottom-0">Selected address</span><br><span class="govuk-!-margin-bottom-0">London</span><br><span class="govuk-!-margin-bottom-0">SW1A 1AA</span><br><span>United Kingdom</span><br>"""
        )
      )
    }

    "must fall back to AddressUkPage and then SelectAddressPage" in {
      val answers = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(AddressUkPage(cpsoId, reportId), manualAddress)
        .withPage(SelectAddressPage(cpsoId, reportId), lookupAddress)

      CPSOWhatIsAddressSummary.row(answers).value.value mustBe ValueViewModel(
        HtmlContent(
          """<span class="govuk-!-margin-bottom-0">Manual address</span><br><span class="govuk-!-margin-bottom-0">Manchester</span><br><span class="govuk-!-margin-bottom-0">M1 1AA</span><br><span>United Kingdom</span><br>"""
        )
      )

      val lookupOnlyAnswers = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(SelectAddressPage(cpsoId, reportId), lookupAddress)

      CPSOWhatIsAddressSummary.row(lookupOnlyAnswers).value.value mustBe ValueViewModel(
        HtmlContent(
          """<span class="govuk-!-margin-bottom-0">Lookup address</span><br><span class="govuk-!-margin-bottom-0">Bristol</span><br><span class="govuk-!-margin-bottom-0">BS1 1AA</span><br><span>United Kingdom</span><br>"""
        )
      )
    }

    "must fall back to CPSOAddressNonUkPage" in {
      val answers = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(CPSOAddressNonUkPage(cpsoId, reportId), nonUkAddress)

      CPSOWhatIsAddressSummary.row(answers).value.value mustBe ValueViewModel(
        HtmlContent(
          """<span class="govuk-!-margin-bottom-0">Non-UK address</span><br><span class="govuk-!-margin-bottom-0">Second line</span><br><span class="govuk-!-margin-bottom-0">Paris</span><br><span class="govuk-!-margin-bottom-0">75001</span><br><span>France</span><br>"""
        )
      )
    }

    "must not create a summary when no address is saved" in {
      val answers = emptyUserAnswers.withPage(CurrentCPSOIdPage()(reportId), cpsoId)

      CPSOWhatIsAddressSummary.row(answers) mustBe empty
    }
  }
}
