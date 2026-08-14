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

package pages.manual.sponsor

import base.SpecBase
import models.ReportId
import models.SubmissionsConstants.{CRS, FATCA}
import models.response.Address
import models.response.Country.GB
import pages.QuestionPage

class HaveSponsorPageSpec extends SpecBase {

  private val strng                   = "somestring"
  private val addrss                  = Address(None, strng, None, Some(strng), None,strng,None, GB)
  private val reportId: ReportId      = ReportId(CRS, 2023, None, "test1")
  private val otherReportId: ReportId = ReportId(FATCA, 2024, None, "test2")

  "cleanup" - {
    "must remove answers for the given report only" in {

      val userAnswers = emptyUserAnswers
        .withPage(HaveSponsorPage()(reportId), true)
        .withPage(HaveSponsorPage()(otherReportId), true)
        .withPage(IsThisAddressForSponsorPage()(reportId), true)
        .withPage(WhatIsAddressForSponsorPage()(reportId), addrss)
        .withPage(IsThisAddressForSponsorPage()(otherReportId), true)
        .withPage(WhatIsAddressForSponsorPage()(otherReportId), addrss)

      val result = HaveSponsorPage()(reportId).cleanupWithReportId(Some(false), userAnswers)(reportId).success.value

      mustBeRemoved(
        result,
        IsThisAddressForSponsorPage()(reportId),
        WhatIsAddressForSponsorPage()(reportId)
      )

      mustBeUnaffected(
        result,
        IsThisAddressForSponsorPage()(otherReportId),
        WhatIsAddressForSponsorPage()(otherReportId)
      )
    }

  }

}
