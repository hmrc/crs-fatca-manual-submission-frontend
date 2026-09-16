package controllers.actions

import base.SpecBase
import controllers.routes
import models.{ReportId, UserAnswers}
import models.SubmissionsConstants.FATCA
import models.requests.{CPSOIdRequest, ReportIdRequest}
import models.viewModels.AccountId
import models.viewModels.manual.cpso.CPSOId
import pages.ReportIdPage
import pages.manual.account.CurrentAccountIdPage
import pages.manual.cpso.CurrentCPSOIdPage
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION

import scala.concurrent.Future

class CpsoIdRequiredActionSpec extends SpecBase {

  class Harness extends CpsoIdRequiredActionImpl {

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
