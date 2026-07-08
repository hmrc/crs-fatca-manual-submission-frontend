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
import models.requests.{DataRequest, ReportIdRequest}
import models.{ReportId, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import scala.concurrent.Future

class ReportIdRequiredActionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionRepository: SessionRepository) extends ReportIdRequiredActionImpl(sessionRepository) {

    def callRefine[A](request: DataRequest[A]): Future[Either[Result, ReportIdRequest[A]]] =
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

  private def dataRequest(userAnswers: UserAnswers): DataRequest[_] =
    DataRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId
    )

  "ReportIdRequiredAction" - {

    "must return a ReportIdRequest when ReportIdPage exists in user answers" in {
      val userAnswers    = emptyUserAnswers.set(ReportIdPage, reportId).success.value
      val mockRepository = mock[SessionRepository]

      val action = new Harness(mockRepository)
      when(mockRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

      val result = action.callRefine(dataRequest(userAnswers)).futureValue

      result match {
        case Right(request) =>
          request.userId mustBe userId
          request.userAnswers mustBe userAnswers
          request.fatcaId mustBe fatcaId
          request.reportId mustBe reportId

        case Left(_) =>
          fail("Expected ReportIdRequiredAction to return Right, but got Left")
      }
    }

    "must redirect to Journey Recovery when ReportIdPage does not exist in user answers" in {
      val mockRepository = mock[SessionRepository]
      val action         = new Harness(mockRepository)
      when(mockRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

      val result = action.callRefine(dataRequest(emptyUserAnswers)).futureValue

      result match {
        case Left(redirectResult) =>
          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header.headers(LOCATION) mustBe
            routes.JourneyRecoveryController.onPageLoad().url

        case Right(_) =>
          fail("Expected ReportIdRequiredAction to return Left, but got Right")
      }
    }
    "must redirect to Journey Recovery when FE Repository returns Future[None]" in {
      val mockRepository = mock[SessionRepository]
      val action         = new Harness(mockRepository)
      when(mockRepository.get(any())).thenReturn(Future.successful(None))

      val result = action.callRefine(dataRequest(emptyUserAnswers)).futureValue

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
