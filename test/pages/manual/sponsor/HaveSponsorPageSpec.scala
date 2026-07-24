package pages.manual.sponsor

import base.SpecBase
import models.ReportId
import models.SubmissionsConstants.{CRS, FATCA}
import models.response.Address
import models.response.Country.GB
import pages.QuestionPage

class HaveSponsorPageSpec extends SpecBase {

  private val strng                   = "somestring"
  private val addrss                  = Address(None, strng, None, strng, None, None, GB)
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
