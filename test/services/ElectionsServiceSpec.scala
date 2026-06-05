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

package services

import base.SpecBase
import connectors.ElectionsConnector
import models.FiIdentifiers
import models.requests.ElectionsSubmissionRequest
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{reset, times, verify, when}
import pages.FiDetailsPage
import pages.elections.{CRSContractsPage, CRSDormantAccountsPage, CRSThresholdsPage}
import repositories.SessionRepository
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.Future

class ElectionsServiceSpec extends SpecBase {
  private val mockConnector  = mock[ElectionsConnector]
  private val mockRepository = mock[SessionRepository]
  private val service        = new ElectionsService(mockConnector, mockRepository)

  given HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit =
    reset(mockConnector, mockRepository)
    super.beforeEach()

  "ElectionService" - {
    "should fail the future when fiId not present" in {
      val userAnswers = emptyUserAnswers
        .withPage(CRSContractsPage, true)
        .withPage(CRSDormantAccountsPage, true)
        .withPage(CRSThresholdsPage, true)

      val exception = service.submitAndDeleteElectionData(userAnswers, 2026).failed.futureValue

      exception mustBe a[InternalServerException]
      exception.getMessage mustBe "Unable to find FI Details"
    }

    "should return future failed when connector returns failure" in {
      val userAnswers = emptyUserAnswers
        .withPage(FiDetailsPage, FiIdentifiers("testId", "Test FI"))
        .withPage(CRSContractsPage, true)
        .withPage(CRSDormantAccountsPage, true)
        .withPage(CRSThresholdsPage, true)
      when(mockConnector.submit(any())(using any[HeaderCarrier])).thenReturn(Future.failed(RuntimeException("Failed")))

      val exception = service.submitAndDeleteElectionData(userAnswers, 2026).failed.futureValue

      exception mustBe a[RuntimeException]
      exception.getMessage mustBe "Failed"
    }

    "should return Future failed when repository set failed" in {
      val userAnswers = emptyUserAnswers
        .withPage(FiDetailsPage, FiIdentifiers("testId", "Test FI"))
        .withPage(CRSContractsPage, true)
        .withPage(CRSDormantAccountsPage, true)
        .withPage(CRSThresholdsPage, true)
      when(mockConnector.submit(any())(using any[HeaderCarrier])).thenReturn(Future.successful(()))
      when(mockRepository.set(any())).thenReturn(Future.failed(RuntimeException("Failed")))

      val exception = service.submitAndDeleteElectionData(userAnswers, 2026).failed.futureValue

      exception mustBe a[RuntimeException]
      exception.getMessage mustBe "Failed"
    }

    "should return Future success when Request went through successfully" in {
      val userAnswers = emptyUserAnswers
        .withPage(FiDetailsPage, FiIdentifiers("testId", "Test FI"))
        .withPage(CRSContractsPage, true)
        .withPage(CRSDormantAccountsPage, true)
        .withPage(CRSThresholdsPage, true)
      when(mockConnector.submit(any())(using any[HeaderCarrier])).thenReturn(Future.successful(()))
      when(mockRepository.set(any())).thenReturn(Future.successful(true))

      service.submitAndDeleteElectionData(userAnswers, 2026).futureValue mustBe ()

      verify(mockConnector, times(1)).submit(any[ElectionsSubmissionRequest])(using any[HeaderCarrier])
    }
  }

}
