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

class WhatIsGIINForSponsorSummarySpec extends SpecBase {

  ".row" - {

    implicit val messages: Messages = stubMessages()
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "testFIID")
    val fiName                      = "TestFiName"

    "must return SummaryListRow with value" in {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(WhatIsGIINForSponsorPage(), "alphvA.zDSJH.HV.255")
        .withPage(FINamePage(), fiName)

      val summary = WhatIsGIINForSponsorSummary.row(ua)

      summary.isDefined mustBe true

      summary.get mustBe SummaryListRowViewModel(
        key = KeyViewModel("whatIsGIINForSponsor.checkYourAnswersLabel"),
        value = ValueViewModel("alphvA.zDSJH.HV.255"),
        actions = Seq(
          ActionItemViewModel("site.change", controllers.manual.sponsor.routes.WhatIsGIINForSponsorController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("whatIsGIINForSponsor.change.hidden"))
        )
      )
    }

    "must return None" in {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(FINamePage(), fiName)

      val summary = WhatIsGIINForSponsorSummary.row(ua)

      summary.isDefined mustBe false
    }

  }
}
