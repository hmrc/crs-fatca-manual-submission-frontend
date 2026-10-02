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
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.manual.cpso.{IndividualName, IndividualOrOrganisation}
import models.requests.{CPSOIdRequest, CPSONameRequest}
import models.viewModels.manual.cpso.CPSOId
import models.{ReportId, UserAnswers}
import pages.ReportIdPage
import pages.manual.cpso.{CpsoOrganisationNamePage, IndividualNamePage, IndividualOrOrganisationPage}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class CPSONameRequiredActionSpec extends SpecBase {

  class Harness extends CPSONameRequiredActionImpl {

    def callRefine[A](request: CPSOIdRequest[A]): Future[Either[Result, CPSONameRequest[A]]] =
      refine(request)
  }

  private val userId  = "user-id"
  private val fatcaId = "FATCAID"
  private val cpsoId  = CPSOId("TestAccountId")

  private def reportId(regime: RegimeType): ReportId =
    ReportId(
      regime = regime,
      reportingYear = 2025,
      uploadedTime = None,
      fiId = "FIID"
    )

  private def cpsoIdRequest(reportId: ReportId, userAnswers: UserAnswers): CPSOIdRequest[_] =
    CPSOIdRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId,
      cpsoId = cpsoId
    )

  private def assertJourneyRecovery(result: Either[Result, CPSONameRequest[_]]): Unit =
    result match {
      case Left(redirectResult) =>
        redirectResult.header.status mustBe SEE_OTHER
        redirectResult.header.headers(LOCATION) mustBe
          routes.JourneyRecoveryController.onPageLoad().url

      case Right(_) =>
        fail("Expected CPSONameRequiredAction to return Left, but got Right")
    }

  "CPSONameRequiredAction" - {

    "when the regime is CRS" - {

      "must return a CPSONameRequest with the individual's full name when IndividualName exists in user answers" in {
        implicit val rid: ReportId = reportId(CRS)
        val individualName         = IndividualName("John", "Smith")

        val userAnswers =
          emptyUserAnswers
            .set(ReportIdPage, rid)
            .success
            .value
            .set(IndividualNamePage(cpsoId), individualName)
            .success
            .value

        val action = new Harness

        val result = action.callRefine(cpsoIdRequest(rid, userAnswers)).futureValue

        result match {
          case Right(request) =>
            request.userId mustBe userId
            request.userAnswers mustBe userAnswers
            request.fatcaId mustBe fatcaId
            request.reportId mustBe rid
            request.cpsoId mustBe cpsoId
            request.cpsoName mustBe individualName.fullName

          case Left(_) =>
            fail("Expected CPSONameRequiredAction to return Right, but got Left")
        }
      }

      "must redirect to Journey Recovery when IndividualName does not exist in user answers" in {
        implicit val rid: ReportId = reportId(CRS)

        val userAnswers =
          emptyUserAnswers
            .set(ReportIdPage, rid)
            .success
            .value

        val action = new Harness

        val result = action.callRefine(cpsoIdRequest(rid, userAnswers)).futureValue

        assertJourneyRecovery(result)
      }
    }

    "when the regime is FATCA" - {

      "must return a CPSONameRequest with the individual's full name when IndividualOrOrganisation is Individual and IndividualName exists in user answers" in {
        implicit val rid: ReportId = reportId(FATCA)
        val individualName         = IndividualName("Jane", "Doe")

        val userAnswers =
          emptyUserAnswers
            .set(ReportIdPage, rid)
            .success
            .value
            .set(IndividualOrOrganisationPage(cpsoId), IndividualOrOrganisation.Individual)
            .success
            .value
            .set(IndividualNamePage(cpsoId), individualName)
            .success
            .value

        val action = new Harness

        val result = action.callRefine(cpsoIdRequest(rid, userAnswers)).futureValue

        result match {
          case Right(request) =>
            request.userId mustBe userId
            request.userAnswers mustBe userAnswers
            request.fatcaId mustBe fatcaId
            request.reportId mustBe rid
            request.cpsoId mustBe cpsoId
            request.cpsoName mustBe individualName.fullName

          case Left(_) =>
            fail("Expected CPSONameRequiredAction to return Right, but got Left")
        }
      }

      "must redirect to Journey Recovery when IndividualOrOrganisation is Individual and IndividualName does not exist in user answers" in {
        implicit val rid: ReportId = reportId(FATCA)

        val userAnswers =
          emptyUserAnswers
            .set(ReportIdPage, rid)
            .success
            .value
            .set(IndividualOrOrganisationPage(cpsoId), IndividualOrOrganisation.Individual)
            .success
            .value

        val action = new Harness

        val result = action.callRefine(cpsoIdRequest(rid, userAnswers)).futureValue

        assertJourneyRecovery(result)
      }

      "must return a CPSONameRequest with the organisation name when IndividualOrOrganisation is Organisation and the organisation name exists in user answers" in {
        implicit val rid: ReportId = reportId(FATCA)
        val organisationName       = "Test Organisation"

        val userAnswers =
          emptyUserAnswers
            .set(ReportIdPage, rid)
            .success
            .value
            .set(IndividualOrOrganisationPage(cpsoId), IndividualOrOrganisation.Organisation)
            .success
            .value
            .set(CpsoOrganisationNamePage(cpsoId, rid), organisationName)
            .success
            .value

        val action = new Harness

        val result = action.callRefine(cpsoIdRequest(rid, userAnswers)).futureValue

        result match {
          case Right(request) =>
            request.userId mustBe userId
            request.userAnswers mustBe userAnswers
            request.fatcaId mustBe fatcaId
            request.reportId mustBe rid
            request.cpsoId mustBe cpsoId
            request.cpsoName mustBe organisationName

          case Left(_) =>
            fail("Expected CPSONameRequiredAction to return Right, but got Left")
        }
      }

      "must redirect to Journey Recovery when IndividualOrOrganisation is Organisation and the organisation name does not exist in user answers" in {
        implicit val rid: ReportId = reportId(FATCA)

        val userAnswers =
          emptyUserAnswers
            .set(ReportIdPage, rid)
            .success
            .value
            .set(IndividualOrOrganisationPage(cpsoId), IndividualOrOrganisation.Organisation)
            .success
            .value

        val action = new Harness

        val result = action.callRefine(cpsoIdRequest(rid, userAnswers)).futureValue

        assertJourneyRecovery(result)
      }

      "must redirect to Journey Recovery when IndividualOrOrganisation does not exist in user answers" in {
        implicit val rid: ReportId = reportId(FATCA)

        val userAnswers =
          emptyUserAnswers
            .set(ReportIdPage, rid)
            .success
            .value

        val action = new Harness

        val result = action.callRefine(cpsoIdRequest(rid, userAnswers)).futureValue

        assertJourneyRecovery(result)
      }
    }
  }
}
