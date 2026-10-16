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
import models.SubmissionsConstants.CRS
import models.requests.{CPSONameRequest, TaxResidentCountryIdForCPSORequest}
import models.viewModels.manual.cpso.CPSOId
import models.{ReportId, UserAnswers}
import pages.manual.cpso.CurrentTaxResidentCountryIndexPage
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class TaxResidentCountryIdRequiredForCPSOActionSpec extends SpecBase {

  class Harness extends TaxResidentCountryIdRequiredForCPSOActionImpl() {

    def callRefine[A](request: CPSONameRequest[A]): Future[Either[Result, TaxResidentCountryIdForCPSORequest[A]]] =
      refine(request)
  }

  private val userId   = "user-id"
  private val fatcaId  = "FATCAID"
  private val cpsoId   = CPSOId("cpso-id")
  private val cpsoName = "Test Cpso"

  private val reportId = ReportId(
    regime = CRS,
    reportingYear = 2025,
    uploadedTime = None,
    fiId = "TestfiID"
  )

  private val currentIndex = 2

  private def cpsoNameRequest(userAnswers: UserAnswers): CPSONameRequest[_] =
    CPSONameRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId,
      cpsoId = cpsoId,
      cpsoName = cpsoName
    )

  private def answersWithIndex(index: Int, forCpsoId: CPSOId = cpsoId, forReportId: ReportId = reportId): UserAnswers =
    emptyUserAnswers.withPage(CurrentTaxResidentCountryIndexPage(forCpsoId, forReportId), index)

  private def journeyRecovery(result: Either[Result, TaxResidentCountryIdForCPSORequest[_]]): Unit =
    result match {
      case Left(redirect) =>
        redirect.header.status mustBe SEE_OTHER
        redirect.header.headers(LOCATION) mustBe routes.JourneyRecoveryController.onPageLoad().url

      case Right(_) =>
        fail("Expected TaxResidentCountryIdRequiredForCPSOAction to return Left, but got Right")
    }

  "TaxResidentCountryIdRequiredForCPSOAction" - {

    "when a current tax resident country index exists in the user answers" - {

      "must return a tax resident country request carrying the stored index" in {
        val result = new Harness().callRefine(cpsoNameRequest(answersWithIndex(currentIndex))).futureValue

        result match {
          case Right(request) => request.currentIndex mustBe currentIndex
          case Left(_)        => fail("Expected TaxResidentCountryIdRequiredForCPSOAction to return Right, but got Left")
        }
      }

      "must carry the incoming request through unchanged" in {
        val userAnswers = answersWithIndex(currentIndex)
        val incoming    = cpsoNameRequest(userAnswers)

        new Harness().callRefine(incoming).futureValue match {
          case Right(request) =>
            request.request mustBe incoming.request
            request.userId mustBe userId
            request.userAnswers mustBe userAnswers
            request.fatcaId mustBe fatcaId
            request.reportId mustBe reportId
            request.cpsoId mustBe cpsoId
            request.cpsoName mustBe cpsoName

          case Left(_) =>
            fail("Expected TaxResidentCountryIdRequiredForCPSOAction to return Right, but got Left")
        }
      }
    }

    "when no current tax resident country index exists in the user answers" - {

      "must redirect to Journey Recovery" in {
        val result = new Harness().callRefine(cpsoNameRequest(emptyUserAnswers)).futureValue

        journeyRecovery(result)
      }

    }
  }
}
