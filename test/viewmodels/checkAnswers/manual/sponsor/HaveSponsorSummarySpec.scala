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
import models.{CheckMode, ReportId}
import org.scalatest.freespec.AnyFreeSpec
import pages.ReportIdPage
import pages.manual.FINamePage
import pages.manual.sponsor.*
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.govuk.all.*

class HaveSponsorSummarySpec extends SpecBase {

  ".row" - {

    implicit val messages: Messages = stubMessages()
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "testFIID")
    val fiName                      = "TestFiName"

    "must return SummaryListRow with yes" in {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(HaveSponsorPage(), true)
        .withPage(FINamePage(), fiName)

      val haveSponsorSummary = HaveSponsorSummary.row(ua)

      haveSponsorSummary.isDefined mustBe true

      haveSponsorSummary.get mustBe SummaryListRowViewModel(
        key = KeyViewModel("haveSponsor.checkYourAnswersLabel"),
        value = ValueViewModel("site.yes"),
        actions = Seq(
          ActionItemViewModel("site.change", controllers.manual.sponsor.routes.HaveSponsorController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("haveSponsor.change.hidden"))
        )
      )
    }

    "must return SummaryListRow with no" in {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(HaveSponsorPage(), false)
        .withPage(FINamePage(), fiName)

      val haveSponsorSummary = HaveSponsorSummary.row(ua)

      haveSponsorSummary.isDefined mustBe true

      haveSponsorSummary.get mustBe SummaryListRowViewModel(
        key = KeyViewModel("haveSponsor.checkYourAnswersLabel"),
        value = ValueViewModel("site.no"),
        actions = Seq(
          ActionItemViewModel("site.change", controllers.manual.sponsor.routes.HaveSponsorController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("haveSponsor.change.hidden"))
        )
      )
    }

    "must return None" in {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(FINamePage(), fiName)

      val haveSponsorSummary = HaveSponsorSummary.row(ua)

      haveSponsorSummary.isDefined mustBe false
    }

  }
}
