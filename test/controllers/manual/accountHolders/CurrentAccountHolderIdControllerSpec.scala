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
import models.{NormalMode, UserAnswers}
import models.viewModels.AccountHolderId
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.CurrentAccountHolderIdPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class CurrentAccountHolderIdControllerSpec extends SpecBase with MockitoSugar {

  private val accountHolderId = "21"

  private def changeRoute(accountHolderId: String): String =
    controllers.manual.accountHolders.routes.CurrentAccountHolderIdController.onChangeRedirect(accountHolderId).url

  private def removeRoute(accountHolderId: String): String =
    controllers.manual.accountHolders.routes.CurrentAccountHolderIdController.onRemoveRedirect(accountHolderId).url

  private def applicationWith(userAnswers: Option[UserAnswers], repository: DatabaseConnector) =
    applicationBuilder(maybeUserAnswers = userAnswers)
      .overrides(bind[DatabaseConnector].toInstance(repository))
      .build()

  private def userAnswersWithCurrentId(currentId: Option[AccountHolderId] = None): UserAnswers = {
    val answers = emptyUserAnswers.withPage(ReportIdPage, reportId)
    currentId.fold(answers)(
      id => answers.withPage(CurrentAccountHolderIdPage(), id)
    )
  }

  "CurrentAccountHolderId Controller" - {

    "changing an account holder" - {

      "must remember the account holder as the current one and redirect to the change destination" in {

        val mockRepository = mock[DatabaseConnector]
        when(mockRepository.set(any())(any())) thenReturn Future.successful(())

        val application = applicationWith(Some(userAnswersWithCurrentId()), mockRepository)

        running(application) {
          val request = FakeRequest(GET, changeRoute(accountHolderId))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.UnderConstructionController.onPageLoad().url

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockRepository).set(captor.capture())(any())

          captor.getValue.get(CurrentAccountHolderIdPage()) mustBe Some(AccountHolderId(accountHolderId))
        }
      }

      "must keep the rest of the user answers when remembering the account holder" in {

        val mockRepository = mock[DatabaseConnector]
        when(mockRepository.set(any())(any())) thenReturn Future.successful(())

        val application = applicationWith(Some(userAnswersWithCurrentId()), mockRepository)

        running(application) {
          val request = FakeRequest(GET, changeRoute(accountHolderId))

          route(application, request).value.futureValue

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockRepository).set(captor.capture())(any())

          captor.getValue.get(ReportIdPage) mustBe Some(reportId)
        }
      }

      "must replace a previously remembered account holder" in {

        val mockRepository = mock[DatabaseConnector]
        when(mockRepository.set(any())(any())) thenReturn Future.successful(())

        val answers     = userAnswersWithCurrentId(Some(AccountHolderId("99")))
        val application = applicationWith(Some(answers), mockRepository)

        running(application) {
          val request = FakeRequest(GET, changeRoute(accountHolderId))

          route(application, request).value.futureValue

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockRepository).set(captor.capture())(any())

          captor.getValue.get(CurrentAccountHolderIdPage()) mustBe Some(AccountHolderId(accountHolderId))
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {

        val application = applicationBuilder(maybeUserAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, changeRoute(accountHolderId))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "removing an account holder" - {

      "must remember the account holder as the current one and redirect to the remove destination" in {

        val mockRepository = mock[DatabaseConnector]
        when(mockRepository.set(any())(any())) thenReturn Future.successful(())

        val application = applicationWith(Some(userAnswersWithCurrentId()), mockRepository)

        running(application) {
          val request = FakeRequest(GET, removeRoute(accountHolderId))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.manual.accountHolders.routes.RemoveAccountHolderController.onPageLoad(NormalMode).url

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockRepository).set(captor.capture())(any())

          captor.getValue.get(CurrentAccountHolderIdPage()) mustBe Some(AccountHolderId(accountHolderId))
        }
      }

      "must redirect to Journey Recovery when no existing data is found" in {

        val application = applicationBuilder(maybeUserAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removeRoute(accountHolderId))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
