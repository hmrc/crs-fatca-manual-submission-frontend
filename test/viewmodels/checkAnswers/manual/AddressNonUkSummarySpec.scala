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

package viewmodels.checkAnswers.manual

import base.SpecBase
import models.SubmissionsConstants.CRS
import models.{AddressNonUk, CheckMode, ReportId}
import org.scalatest.freespec.AnyFreeSpec
import pages.ReportIdPage
import pages.manual.FINamePage
import pages.manual.sponsor.*
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.AddressNonUkSummary
import viewmodels.govuk.all.*

class AddressNonUkSummarySpec extends SpecBase {

  ".row" - {

    implicit val messages: Messages = stubMessages()
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "testFIID")
    val fiName                      = "TestFiName"

    "must return SummaryListRow with value" in {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(
          AddressNonUkPage(),
          AddressNonUk(addressLine1 = "TestAddress", addressLine2 = None, addressLine3 = "address line 3", addressLine4 = None, postcode = None, country = "GB")
        )
        .withPage(FINamePage(), fiName)

      val addressText =
        """<span class="govuk-margin-bottom-0">TestAddress</span><br><span class="govuk-margin-bottom-0">address line 3</span><br><span>United Kingdom</span><br>"""

      val summary = AddressNonUkSummary.row(ua)

      summary.isDefined mustBe true

      summary.get mustBe SummaryListRowViewModel(
        key = KeyViewModel("addressNonUk.checkYourAnswersLabel"),
        value = ValueViewModel(HtmlContent(addressText)),
        actions = Seq(
          ActionItemViewModel("site.change", controllers.manual.sponsor.routes.IsSponsorBasedInUKController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("addressNonUk.change.hidden"))
        )
      )
    }

    "must return None" in {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(FINamePage(), fiName)

      val summary = AddressNonUkSummary.row(ua)

      summary.isDefined mustBe false
    }

  }
}
