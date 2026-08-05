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
import controllers.routes
import models.SubmissionsConstants.FATCA
import models.requests.{ReportIdRequest, SponsorNameRequest}
import models.{ReportId, UserAnswers}
import pages.ReportIdPage
import pages.manual.sponsor.SponsorNamePage
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class SponsorNameRequiredActionSpec extends SpecBase {

  class Harness extends SponsorNameRequiredActionImpl {

    def callRefine[A](request: ReportIdRequest[A]): Future[Either[Result, SponsorNameRequest[A]]] =
      refine(request)
  }

  private val userId  = "user-id"
  private val fatcaId = "FATCAID"

  private val reportId = ReportId(
    regime = FATCA,
    reportingYear = 2025,
    uploadedTime = None,
    fiId = "FIID"
  )

  private def reportIdRequest(userAnswers: UserAnswers): ReportIdRequest[_] =
    ReportIdRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId
    )

  "SponsorNameRequiredAction" - {

    val sponsorName = "Test Sponsor"
    "must return a SponsorNameRequest when SponsorName exists in user answers" in {
      val userAnswers =
        emptyUserAnswers
          .set(ReportIdPage, reportId)
          .success
          .value
          .set(SponsorNamePage()(reportId), sponsorName)
          .success
          .value

      val action = new Harness

      val result =
        action.callRefine(reportIdRequest(userAnswers)).futureValue

      result match {
        case Right(request) =>
          request.userId mustBe userId
          request.userAnswers mustBe userAnswers
          request.fatcaId mustBe fatcaId
          request.reportId mustBe reportId
          request.sponsorName mustBe sponsorName

        case Left(_) =>
          fail("Expected SponsorNameRequiredAction to return Right, but got Left")
      }
    }

    "must redirect to Journey Recovery when SponsorName does not exist in user answers" in {
      val action = new Harness

      val result =
        action.callRefine(reportIdRequest(emptyUserAnswers)).futureValue

      result match {
        case Left(redirectResult) =>
          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header.headers(LOCATION) mustBe
            routes.JourneyRecoveryController.onPageLoad().url

        case Right(_) =>
          fail("Expected SponsorNameRequiredAction to return Left, but got Right")
      }
    }
  }
}
