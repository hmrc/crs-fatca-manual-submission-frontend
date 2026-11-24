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
import forms.behaviours.BooleanFieldBehaviours
import models.SubmissionsConstants.FATCA
import models.viewModels.manual.cpso.CPSOId
import models.{CheckMode, NormalMode, ReportId, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import pages.ReportIdPage
import pages.manual.cpso.CurrentCPSOIdPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.CpsoService

import scala.concurrent.Future

class CurrentCpsoIdControllerSpec extends SpecBase {

  private val cpsoId = "cpso-1"

  private def changeRoute(cpsoId: String): String =
    controllers.manual.cpso.routes.CurrentCpsoIdController.onChangeRedirect(cpsoId).url

  private def removeRoute(cpsoId: String): String =
    controllers.manual.cpso.routes.CurrentCpsoIdController.onRemoveRedirect(cpsoId).url

  private def changeCpsoNotCompleteRoute: String =
    controllers.manual.cpso.routes.AccountHolderController.onPageLoad(CheckMode).url

  private def changeCpsoCompleteRoute: String =
    controllers.manual.cpso.routes.CPSOCheckAnswersController.onPageLoad().url

  private def removeCPSOUrl: String =
    controllers.manual.cpso.routes.RemoveCPSOController.onPageLoad(NormalMode).url

  private def answersWithCurrentCpsoId(cpsoId: CPSOId): UserAnswers =
    emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentCPSOIdPage(), cpsoId)

  private def mockRepository: DatabaseConnector = {
    val repository = mock[DatabaseConnector]
    when(repository.set(any())(any())) thenReturn Future.successful(())
    repository
  }

  private def mockCpsoService(result: Boolean): CpsoService = {
    val cpsoService = mock[CpsoService]
    when(cpsoService.isCpsoComplete(any(), any())(any())).thenReturn(result)

    cpsoService
  }

  "CurrentCpsoIdController" - {

    "onChangeRedirect" - {

      "must store the CPSo id and redirect to the accounts page because cpso is incomplete" in {
        val cpsoIsIncomplete = false
        val repository       = mockRepository
        val cpsoService      = mockCpsoService(cpsoIsIncomplete)
        val application = applicationBuilder(Some(answersWithCurrentCpsoId(CPSOId("old-cpso"))))
          .overrides(
            bind[DatabaseConnector].toInstance(repository),
            bind[CpsoService].toInstance(cpsoService)
          )
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, changeRoute(cpsoId))).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual changeCpsoNotCompleteRoute

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(captor.capture())(any())

          captor.getValue.get(CurrentCPSOIdPage()) mustBe Some(CPSOId(cpsoId))
        }
      }

      "must store the CPSo id and redirect to check answers page because cpso is complete" in {
        val cpsoIsComplete = true
        val repository     = mockRepository
        val cpsoService    = mockCpsoService(cpsoIsComplete)
        val application = applicationBuilder(Some(answersWithCurrentCpsoId(CPSOId("old-cpso"))))
          .overrides(
            bind[DatabaseConnector].toInstance(repository),
            bind[CpsoService].toInstance(cpsoService)
          )
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, changeRoute(cpsoId))).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual changeCpsoCompleteRoute

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(captor.capture())(any())

          captor.getValue.get(CurrentCPSOIdPage()) mustBe Some(CPSOId(cpsoId))
        }
      }

      "must keep the user answers for other reports unchanged" in {
        val otherReportId = ReportId(FATCA, 2024, None, "other-fi")
        val otherCpsoPage = CurrentCPSOIdPage()(otherReportId)
        val answers = answersWithCurrentCpsoId(CPSOId("old-cpso"))
          .withPage(otherCpsoPage, CPSOId("cpso-other"))

        val repository = mockRepository
        val application = applicationBuilder(Some(answers))
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, changeRoute(cpsoId))).value

          status(result) mustEqual SEE_OTHER

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(captor.capture())(any())

          val saved = captor.getValue
          saved.get(CurrentCPSOIdPage()) mustBe Some(CPSOId(cpsoId))
          saved.get(otherCpsoPage) mustBe Some(CPSOId("cpso-other"))
        }
      }
    }

    "onRemoveRedirect" - {

      "must store the CPSo id and redirect to the remove CPSo page" in {
        val repository = mockRepository
        val application = applicationBuilder(Some(answersWithCurrentCpsoId(CPSOId("old-cpso"))))
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, removeRoute(cpsoId))).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual removeCPSOUrl

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(captor.capture())(any())

          captor.getValue.get(CurrentCPSOIdPage()) mustBe Some(CPSOId(cpsoId))
        }
      }

      "must persist the id exactly as supplied in the request" in {
        val suppliedId = "cpso-42"
        val repository = mockRepository
        val application = applicationBuilder(Some(answersWithCurrentCpsoId(CPSOId("old-cpso"))))
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, removeRoute(suppliedId))).value

          status(result) mustEqual SEE_OTHER

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(captor.capture())(any())

          captor.getValue.get(CurrentCPSOIdPage()) mustBe Some(CPSOId(suppliedId))
        }
      }
    }

    "when user answers cannot be retrieved" - {

      "must redirect to Journey Recovery and not save on change" in {
        val repository = mockRepository
        val application = applicationBuilder(None)
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, changeRoute(cpsoId))).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          verify(repository, never()).set(any())(any())
        }
      }

      "must redirect to Journey Recovery and not save on remove" in {
        val repository = mockRepository
        val application = applicationBuilder(None)
          .overrides(bind[DatabaseConnector].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, FakeRequest(GET, removeRoute(cpsoId))).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          verify(repository, never()).set(any())(any())
        }
      }
    }
  }
}
