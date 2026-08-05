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

package controllers.actions

import base.SpecBase
import models.SubmissionsConstants.FATCA
import models.manual.sponsor.TaxResidentCountry
import models.requests.SponsorNameRequest
import models.{ReportId, UserAnswers}
import pages.ReportIdPage
import pages.manual.sponsor.{SponsorNamePage, TaxResidentCountriesListPage}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class TaxResidentCountryIdCheckActionSpec extends SpecBase {

  class Harness extends TaxResidentCountryIdCheckActionImpl {
    def call[A](request: SponsorNameRequest[A]): Future[Option[Result]] = filter(request)
  }

  private val userId  = "user-id"
  private val fatcaId = "FATCAID"

  private val reportId = ReportId(
    regime = FATCA,
    reportingYear = 2025,
    uploadedTime = None,
    fiId = "FIID"
  )
  val sponsorName = "Test Sponsor"

  private def sponsorNameRequest(userAnswers: UserAnswers): SponsorNameRequest[_] =
    SponsorNameRequest(
      request = FakeRequest(GET, "/some?id=1"),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId,
      sponsorName = sponsorName
    )

  "TaxResidentCountryIdCheckAction" - {

    "must return a SponsorNameRequest when id is valid" in {
      val userAnswers =
        emptyUserAnswers
          .withPage(ReportIdPage, reportId)
          .withPage(SponsorNamePage()(reportId), sponsorName)
          .withPage(TaxResidentCountriesListPage()(reportId), Seq(TaxResidentCountry("UK")))

      val action = new Harness

      val result = action.call(sponsorNameRequest(userAnswers)).futureValue

      result.isDefined mustBe false
    }

    "must reject  when id is not valid" in {
      val userAnswers =
        emptyUserAnswers
          .withPage(ReportIdPage, reportId)
          .withPage(SponsorNamePage()(reportId), sponsorName)

      val action = new Harness

      val result = action.call(sponsorNameRequest(userAnswers)).futureValue

      result.isDefined mustBe true
    }

  }
}
