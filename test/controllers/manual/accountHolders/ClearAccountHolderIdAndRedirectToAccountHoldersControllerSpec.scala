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

package controllers.manual.accountHolders

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import models.viewModels.AccountHolderId
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.{AccountHolderOrganisationNamePage, CurrentAccountHolderIdPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class ClearAccountHolderIdAndRedirectToAccountHoldersControllerSpec extends SpecBase with MockitoSugar {

  private val accountHolderId   = AccountHolderId("21")
  private val accountHolderName = "Alpha Holdings"

  private def clearAccountHolderIdRoute(mode: Mode): String =
    controllers.manual.accountHolders.routes.ClearAccountHolderIdAndRedirectToAccountHoldersController.onRedirect(mode).url

  private def accountHolderPageRoute(mode: Mode): String =
    controllers.manual.accountHolders.routes.AccountController.onPageLoad(mode).url

  private def userAnswersFor(currentId: Option[AccountHolderId]): UserAnswers = {
    val answers = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(AccountHolderOrganisationNamePage(accountHolderId), accountHolderName)

    currentId.fold(answers)(
      id => answers.withPage(CurrentAccountHolderIdPage(), id)
    )
  }

  private def applicationWith(userAnswers: Option[UserAnswers], repository: DatabaseConnector) =
    applicationBuilder(maybeUserAnswers = userAnswers)
      .overrides(bind[DatabaseConnector].toInstance(repository))
      .build()

  "ClearAccountHolderIdAndRedirectToAccountHolders Controller" - {

    "must clear the current account holder id, save the answers and redirect to the account holder page" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application = applicationWith(Some(userAnswersFor(Some(accountHolderId))), mockRepository)

      running(application) {
        val request = FakeRequest(GET, clearAccountHolderIdRoute(NormalMode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual accountHolderPageRoute(NormalMode)

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(CurrentAccountHolderIdPage()) mustBe None
      }
    }

    "must redirect to the account holder page in check mode" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application = applicationWith(Some(userAnswersFor(Some(accountHolderId))), mockRepository)

      running(application) {
        val request = FakeRequest(GET, clearAccountHolderIdRoute(CheckMode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual accountHolderPageRoute(CheckMode)
      }
    }

    "must only clear the current account holder id, leaving the account holder's answers alone" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application = applicationWith(Some(userAnswersFor(Some(accountHolderId))), mockRepository)

      running(application) {
        val request = FakeRequest(GET, clearAccountHolderIdRoute(NormalMode))

        route(application, request).value.futureValue

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        val saved = captor.getValue

        saved.get(AccountHolderOrganisationNamePage(accountHolderId)) mustBe Some(accountHolderName)
        saved.get(ReportIdPage) mustBe Some(reportId)
      }
    }

    "must still redirect and save when no current account holder id is set" in {

      val mockRepository = mock[DatabaseConnector]
      when(mockRepository.set(any())(any())) thenReturn Future.successful(())

      val application = applicationWith(Some(userAnswersFor(None)), mockRepository)

      running(application) {
        val request = FakeRequest(GET, clearAccountHolderIdRoute(NormalMode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual accountHolderPageRoute(NormalMode)

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockRepository).set(captor.capture())(any())

        captor.getValue.get(CurrentAccountHolderIdPage()) mustBe None
      }
    }

    "must redirect to Journey Recovery if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, clearAccountHolderIdRoute(NormalMode))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
