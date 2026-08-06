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
import models.requests.{SponsorNameRequest, SponsorTaxResidentIdRequest}
import models.{ReportId, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.sponsor.{CurrentTaxResidentCountryIndexPage, SponsorNamePage}
import play.api.test.FakeRequest

import scala.concurrent.Future

class TaxResidentCountryIdCreationActionSpec extends SpecBase with MockitoSugar {

  class Harness extends TaxResidentCountryIdCreationActionImpl() {
    def callTransform[A](request: SponsorNameRequest[A]): Future[SponsorTaxResidentIdRequest[A]] = transform(request)
  }

  private val userId  = "user-id"
  private val fatcaId = "FATCAID"

  private val reportId = ReportId(
    regime = FATCA,
    reportingYear = 2025,
    uploadedTime = None,
    fiId = "FIID"
  )

  val sponsorName = "TestSponsor"
  val userAnswers = emptyUserAnswers.withPage(ReportIdPage, reportId).withPage(SponsorNamePage()(reportId), sponsorName)

  private def sponsorNameRequest(userAnswers: UserAnswers): SponsorNameRequest[_] =
    SponsorNameRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId,
      sponsorName = sponsorName
    )

  "TaxResidentCountryIdCreationAction" - {

    "when there is no currentTaxResidentIndex in the userAnswer" - {

      "must create new Id and set in request" in {
        val action = new Harness()

        val result = action.callTransform(sponsorNameRequest(userAnswers)).futureValue

        result.userId mustBe userId
        result.userAnswers mustBe userAnswers
        result.fatcaId mustBe fatcaId
        result.reportId mustBe reportId
        result.sponsorName mustBe sponsorName
        result.currentId mustBe 0
      }
    }

    "when there is currentTaxResidentIndex in the userAnswer" - {

      "must use existing id and set in request" in {
        val action = new Harness()
        val updatedUA = userAnswers
          .withPage(CurrentTaxResidentCountryIndexPage()(reportId), 2)

        val result = action.callTransform(sponsorNameRequest(updatedUA)).futureValue

        result.userId mustBe userId
        result.userAnswers mustBe updatedUA
        result.fatcaId mustBe fatcaId
        result.reportId mustBe reportId
        result.sponsorName mustBe sponsorName
        result.currentId mustBe 2
      }
    }

  }
}
