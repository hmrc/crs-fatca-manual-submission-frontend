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

package utils

import base.SpecBase
import models.SubmissionsConstants.CRS
import models.TypeOfReport.Information
import models.manual.filercategory.{WhatTypeOfFiler, WhatTypeOfFilerIsSponsor}
import models.{CrsOrFatca, ReportId}
import org.scalatest.freespec.AnyFreeSpec
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class CheckAnswersUtilSpec extends SpecBase {

  val util = new CheckAnswersUtil()

  implicit val messages: Messages = stubMessages()

  "ReportDetailsCheckAnswersUtil" - {

    "getReportDetailsRows" - {

      "must return an empty SummaryList when no relevant answers exist in UserAnswers" in {
        val result = util.getReportDetailsRows(emptyUserAnswers)

        result.rows mustBe Seq.empty
      }

      "must return a populated SummaryList when matching data is present in UserAnswers" in {
        val ua = emptyUserAnswers
          .withPage(CrsOrFatcaPage, CrsOrFatca.Crs)
          .withPage(ReportingYearPage, 2026)
          .withPage(TypeOfReportPage, Information)

        val result = util.getReportDetailsRows(ua)

        result.rows.size mustBe 3
      }
    }

    "getFilerCategoryRows" - {

      implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

      "must return an empty SummaryList when no relevant answers exist in UserAnswers" in {
        val result = util.getFilerCategoryRows(emptyUserAnswers)

        result.rows mustBe Seq.empty
      }

      "must return a populated SummaryList when matching data is present in UserAnswers" in {
        val ua = emptyUserAnswers
          .withPage(WhatTypeOfFilerIsSponsorPage(), WhatTypeOfFilerIsSponsor.Trustee)
          .withPage(WhatTypeOfFilerPage(), WhatTypeOfFiler.Foreign)

        val result = util.getFilerCategoryRows(ua)

        result.rows.size mustBe 2
      }
    }
  }
}
