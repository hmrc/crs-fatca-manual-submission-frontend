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
import models.requests.{DataRequest, ElectionIdRequest}
import models.{ElectionsId, UserAnswers}
import pages.*
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.Future

class ElectionIdRequiredActionSpec extends SpecBase {

  class Harness extends ElectionIdRequiredActionImpl {

    def callRefine[A](request: DataRequest[A]): Future[Either[Result, ElectionIdRequest[A]]] =
      refine(request)
  }

  private val userId  = "user-id"
  private val fatcaId = "FATCAID"

  private val electionId = ElectionsId(
    reportingYear = 2025,
    fiId = "FIID"
  )

  private def dataRequest(userAnswers: UserAnswers): DataRequest[_] =
    DataRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId
    )

  "ElectionIdRequired" - {

    "must return a ElectionIdRequest when ElectionsIdPage exists in user answers" in {
      val userAnswers =
        emptyUserAnswers.set(ElectionsIdPage, electionId).success.value

      val action = new Harness

      val result =
        action.callRefine(dataRequest(userAnswers)).futureValue

      result match {
        case Right(request) =>
          request.userId mustBe userId
          request.userAnswers mustBe userAnswers
          request.fatcaId mustBe fatcaId
          request.electionsId mustBe electionId
        case _ =>
          fail("Expected a Right(ElectionIdRequest) but got a Left(Result)")
      }
    }

    "must redirect to Journey Recovery when ElectionsIdPage does not exist in user answers" in {
      val userAnswers = emptyUserAnswers

      val action = new Harness

      val result =
        action.callRefine(dataRequest(userAnswers)).futureValue

      result match {
        case Left(redirectResult) =>
          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header.headers("Location") mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
