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
import connectors.DatabaseConnector
import models.SubmissionsConstants.FATCA
import models.requests.{CPSOIdRequest, ReportIdRequest}
import models.viewModels.manual.cpso.CPSOId
import models.{ReportId, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.cpso.CurrentCPSOIdPage
import play.api.test.FakeRequest

import scala.concurrent.Future

class CpsoIdCreationActionSpec extends SpecBase with MockitoSugar {

  class Harness(userDataConnector: DatabaseConnector) extends CpsoIdCreationActionImpl(userDataConnector) {
    def callTransform[A](request: ReportIdRequest[A]): Future[CPSOIdRequest[A]] = transform(request)
  }

  private val userId  = "user-id"
  private val fatcaId = "FATCAID"

  private val reportId = ReportId(
    regime = FATCA,
    reportingYear = 2025,
    uploadedTime = None,
    fiId = "FIID"
  )

  val userAnswers =
    emptyUserAnswers
      .set(ReportIdPage, reportId)
      .success
      .value

  private def reportIdRequest(userAnswers: UserAnswers): ReportIdRequest[_] =
    ReportIdRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId
    )

  "CpsoIdCreationAction" - {

    "when there is no currentCPSOId in the userAnswer" - {

      "must create CPSOId & set in userAnswers & request" in {
        val connector = mock[DatabaseConnector]
        when(connector.set(any())(any())) thenReturn Future(())
        val action = new Harness(connector)

        val result = action.callTransform(reportIdRequest(userAnswers)).futureValue

        result.userId mustBe userId
        result.userAnswers mustBe userAnswers
        result.fatcaId mustBe fatcaId
        result.reportId mustBe reportId
        result.cpsoId.value.isEmpty mustBe false
      }
    }

    "when there is currentCPSOId in the userAnswer" - {

      "must use existing CPSOId & set in userAnswers & request" in {
        val connector = mock[DatabaseConnector]
        when(connector.set(any())(any())) thenReturn Future(())
        val action = new Harness(connector)
        val id     = CPSOId("TestId")
        val updatedUA = userAnswers
          .withPage(CurrentCPSOIdPage()(reportId), id)

        val result = action.callTransform(reportIdRequest(updatedUA)).futureValue

        result.userId mustBe userId
        result.userAnswers mustBe updatedUA
        result.fatcaId mustBe fatcaId
        result.reportId mustBe reportId
        result.cpsoId mustBe id
      }
    }

  }
}
