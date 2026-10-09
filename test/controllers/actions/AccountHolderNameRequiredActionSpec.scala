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
import models.manual.accountHolders.IndividualName
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.requests.{AccountHolderIdRequest, AccountHolderNameRequest}
import models.viewModels.AccountHolderId
import models.{ReportId, UserAnswers}
import pages.ReportIdPage
import pages.manual.accountHolders.{AccountHolderIndividualNamePage, AccountHolderOrganisationNamePage, IndividualOrOrganisationPage}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.{ExecutionContext, Future}

class AccountHolderNameRequiredActionSpec extends SpecBase {

  implicit private val ec: ExecutionContext = ExecutionContext.global

  class Harness extends AccountHolderNameRequiredActionImpl {

    def callRefine[A](request: AccountHolderIdRequest[A]): Future[Either[Result, AccountHolderNameRequest[A]]] =
      refine(request)
  }

  private val userId          = "user-id"
  private val fatcaId         = "FATCAID"
  private val accountHolderId = AccountHolderId("account-holder-id")

  implicit private val reportId: ReportId = ReportId(
    regime = CRS,
    reportingYear = 2025,
    uploadedTime = None,
    fiId = "FIID"
  )

  private def accountHolderIdRequest(userAnswers: UserAnswers): AccountHolderIdRequest[_] =
    AccountHolderIdRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId,
      accountHolderId = accountHolderId
    )

  "AccountHolderNameRequiredAction" - {

    "must return an AccountHolderNameRequest for an Individual" in {
      val individualName = IndividualName("Some", "Name")

      val userAnswers = emptyUserAnswers
        .set(ReportIdPage, reportId)
        .success
        .value
        .set(IndividualOrOrganisationPage(accountHolderId), Individual)
        .success
        .value
        .set(AccountHolderIndividualNamePage(accountHolderId), individualName)
        .success
        .value

      val result = new Harness().callRefine(accountHolderIdRequest(userAnswers)).futureValue

      result match {
        case Right(request) =>
          request.userId mustBe userId
          request.userAnswers mustBe userAnswers
          request.fatcaId mustBe fatcaId
          request.reportId mustBe reportId
          request.accountHolderId mustBe accountHolderId
          request.accountHolderName mustBe individualName.fullName

        case Left(_) =>
          fail("Expected AccountHolderNameRequiredAction to return Right, but got Left")
      }
    }

    "must return an AccountHolderNameRequest for an Organisation" in {
      val organisationName = "Test Organisation"

      val userAnswers = emptyUserAnswers
        .set(ReportIdPage, reportId)
        .success
        .value
        .set(IndividualOrOrganisationPage(accountHolderId), Organisation)
        .success
        .value
        .set(AccountHolderOrganisationNamePage(accountHolderId), organisationName)
        .success
        .value

      val result = new Harness().callRefine(accountHolderIdRequest(userAnswers)).futureValue

      result match {
        case Right(request) =>
          request.userId mustBe userId
          request.userAnswers mustBe userAnswers
          request.fatcaId mustBe fatcaId
          request.reportId mustBe reportId
          request.accountHolderId mustBe accountHolderId
          request.accountHolderName mustBe organisationName

        case Left(_) =>
          fail("Expected AccountHolderNameRequiredAction to return Right, but got Left")
      }
    }

    "must redirect to Journey Recovery when IndividualOrOrganisation does not exist" in {
      val action = new Harness()

      val result = action.callRefine(accountHolderIdRequest(emptyUserAnswers)).futureValue

      result match {
        case Left(redirectResult) =>
          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header.headers(LOCATION) mustBe routes.JourneyRecoveryController.onPageLoad().url

        case Right(_) =>
          fail("Expected AccountHolderNameRequiredAction to return Left, but got Right")
      }
    }

    "must redirect to Journey Recovery when the Individual name does not exist" in {
      val userAnswers = emptyUserAnswers
        .set(ReportIdPage, reportId)
        .success
        .value
        .set(IndividualOrOrganisationPage(accountHolderId), Individual)
        .success
        .value

      val result = new Harness().callRefine(accountHolderIdRequest(userAnswers)).futureValue

      result match {
        case Left(redirectResult) =>
          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header.headers(LOCATION) mustBe routes.JourneyRecoveryController.onPageLoad().url

        case Right(_) =>
          fail("Expected AccountHolderNameRequiredAction to return Left, but got Right")
      }
    }

    "must redirect to Journey Recovery when the Organisation name does not exist" in {
      val userAnswers = emptyUserAnswers
        .set(ReportIdPage, reportId)
        .success
        .value
        .set(IndividualOrOrganisationPage(accountHolderId), Organisation)
        .success
        .value

      val result = new Harness().callRefine(accountHolderIdRequest(userAnswers)).futureValue

      result match {
        case Left(redirectResult) =>
          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header.headers(LOCATION) mustBe routes.JourneyRecoveryController.onPageLoad().url

        case Right(_) =>
          fail("Expected AccountHolderNameRequiredAction to return Left, but got Right")
      }
    }
  }
}
