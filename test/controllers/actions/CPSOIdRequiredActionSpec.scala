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
import models.requests.{CPSOIdRequest, ReportIdRequest}
import models.viewModels.manual.cpso.CPSOId
import models.{ReportId, UserAnswers}
import pages.ReportIdPage
import pages.manual.cpso.CurrentCPSOIdPage
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION

import scala.concurrent.Future

class CPSOIdRequiredActionSpec extends SpecBase {

  class Harness extends CPSOIdRequiredActionImpl {

    def callRefine[A](request: ReportIdRequest[A]): Future[Either[Result, CPSOIdRequest[A]]] =
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

  "CpsoIdRequiredAction" - {
    "must return a cpsoIdRequest when CPSOId exists in user answers" in {
      val cpsoId = CPSOId("TestAccountId")
      val userAnswers =
        emptyUserAnswers
          .set(ReportIdPage, reportId)
          .success
          .value
          .set(CurrentCPSOIdPage()(reportId), cpsoId)
          .success
          .value

      val action = new Harness

      val result = action.callRefine(reportIdRequest(userAnswers)).futureValue

      result match {
        case Right(request) =>
          request.userId mustBe userId
          request.userAnswers mustBe userAnswers
          request.fatcaId mustBe fatcaId
          request.reportId mustBe reportId
          request.cpsoId mustBe cpsoId

        case Left(_) =>
          fail("Expected ReportIdRequiredAction to return Right, but got Left")
      }

    }

    "must redirect to Journey Recovery when CurrentCPSOIdPage does not exist in user answers" in {
      val action = new Harness

      val result =
        action.callRefine(reportIdRequest(emptyUserAnswers)).futureValue

      result match {
        case Left(redirectResult) =>
          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header.headers(LOCATION) mustBe
            routes.JourneyRecoveryController.onPageLoad().url

        case Right(_) =>
          fail("Expected ReportIdRequiredAction to return Left, but got Right")
      }
    }
  }

}
