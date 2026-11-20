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

package controllers.manual.cpso

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import models.SubmissionsConstants.FATCA
import models.viewModels.manual.cpso.CPSOId
import models.{CheckMode, Mode, NormalMode, ReportId, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import pages.ReportIdPage
import pages.manual.cpso.{CurrentCPSOIdPage, DoYouWantToAddAnotherCpsoPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class ClearCpsoIdAndRedirectToAddCpsoControllerSpec extends SpecBase {

  private def redirectRoute(mode: Mode): String =
    controllers.manual.cpso.routes.ClearCpsoIdAndRedirectToAddCpsoController.onRedirect(mode).url

  private def accountHolderRoute(mode: Mode): String =
    controllers.manual.cpso.routes.AccountHolderController.onPageLoad(mode).url

  private def answersWithCurrentCpsoId: UserAnswers =
    emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), CPSOId("cpso-1"))
      .withPage(DoYouWantToAddAnotherCpsoPage(), true)

  private def mockRepository: DatabaseConnector = {
    val repository = mock[DatabaseConnector]
    when(repository.set(any())(any())) thenReturn Future.successful(())
    repository
  }

  "ClearCpsoIdAndRedirectToAddCpsoController" - {

    "onRedirect" - {

      "must remove the current CPSo id, save the answers and redirect to the account holder page" in {
        val repository = mockRepository
        val application = applicationBuilder(Some(answersWithCurrentCpsoId))
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, redirectRoute(NormalMode))).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual accountHolderRoute(NormalMode)

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(captor.capture())(any())

          val saved = captor.getValue
          saved.get(CurrentCPSOIdPage()) mustBe None
          saved.get(DoYouWantToAddAnotherCpsoPage()) mustBe Some(true)
          saved.get(ReportIdPage) mustBe Some(reportId)
        }
      }

      "must redirect to the check-mode account holder page when called in CheckMode" in {
        val repository = mockRepository
        val application = applicationBuilder(Some(answersWithCurrentCpsoId))
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, redirectRoute(CheckMode))).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual accountHolderRoute(CheckMode)

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(captor.capture())(any())

          captor.getValue.get(CurrentCPSOIdPage()) mustBe None
        }
      }

      "must remove only the CPSo id for the current report" in {
        val otherReportId = ReportId(FATCA, 2024, None, "other-fi")
        val otherCpsoPage = CurrentCPSOIdPage()(otherReportId)
        val answers       = answersWithCurrentCpsoId.withPage(otherCpsoPage, CPSOId("cpso-other"))

        val repository = mockRepository
        val application = applicationBuilder(Some(answers))
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, redirectRoute(NormalMode))).value

          status(result) mustEqual SEE_OTHER

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(captor.capture())(any())

          val saved = captor.getValue
          saved.get(CurrentCPSOIdPage()) mustBe None
          saved.get(otherCpsoPage) mustBe Some(CPSOId("cpso-other"))
        }
      }

      "must still save and redirect when there is no current CPSo id to remove" in {
        val answers = emptyUserAnswers.withPage(ReportIdPage, reportId)

        val repository = mockRepository
        val application = applicationBuilder(Some(answers))
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, redirectRoute(NormalMode))).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual accountHolderRoute(NormalMode)

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(captor.capture())(any())

          captor.getValue.get(CurrentCPSOIdPage()) mustBe None
        }
      }

      "must redirect to Journey Recovery and not save when there are no user answers" in {
        val repository = mockRepository
        val application = applicationBuilder(None)
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, redirectRoute(NormalMode))).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          verify(repository, never()).set(any())(any())
        }
      }
    }
  }
}
