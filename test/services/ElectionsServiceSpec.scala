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
import models.ServiceErrors.Elections_Error
import models.elections.{CrsElectionsDetails, ElectionDetails, FatcaElectionsDetails, YesNoNa}
import models.requests.ElectionsSubmissionRequest
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{reset, times, verify, when}
import pages.FiDetailsPage
import pages.elections.{CRSContractsPage, CRSDormantAccountsPage, CRSThresholdsPage}
import play.api.inject.bind
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.Future

class ElectionsServiceSpec extends SpecBase {
  private val mockConnector  = mock[ElectionsConnector]
  private val mockRepository = mock[SessionRepository]
  private val fiId: String   = "testFiId"
  private val year: Int      = 2024
  private val service        = new ElectionsService(mockConnector, mockRepository)

  given HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit =
    reset(mockConnector, mockRepository)
  super.beforeEach()

  val testCrsDetails: CrsElectionsDetails = CrsElectionsDetails(
    hasCARF = Some(YesNoNa.Yes),
    hasContracts = Some(YesNoNa.No),
    hasDormantAccounts = Some(YesNoNa.NA),
    hasThresholds = Some(YesNoNa.Yes)
  )

  val testFatcaDetails: FatcaElectionsDetails = FatcaElectionsDetails(
    hasThresholds = Some(YesNoNa.No),
    hasTreasuryRegulations = Some(YesNoNa.NA)
  )

  "ElectionsService" - {

    "getElectionsRows" - {

      "return populated SummaryLists when connector returns elections with both CRS and FATCA details" in withService {
        service =>
          val testElectionDetails = mock[ElectionDetails]
          when(testElectionDetails.crs).thenReturn(Some(testCrsDetails))
          when(testElectionDetails.fatca).thenReturn(Some(testFatcaDetails))

          when(mockConnector.viewElections(eqTo(fiId), eqTo(Some(year)))(any()))
            .thenReturn(Future.successful(Seq(testElectionDetails)))

          val result = service.getElectionsRows(fiId, year)

          val electionsRows = result.futureValue
          electionsRows.crsRows mustBe a[SummaryList]
          electionsRows.fatcaRows mustBe a[SummaryList]
      }

      "return empty CRS rows and populated FATCA rows when only FATCA details are present" in withService {
        service =>
          val testElectionDetails = mock[ElectionDetails]
          when(testElectionDetails.crs).thenReturn(None)
          when(testElectionDetails.fatca).thenReturn(Some(testFatcaDetails))

          when(mockConnector.viewElections(eqTo(fiId), eqTo(Some(year)))(any()))
            .thenReturn(Future.successful(Seq(testElectionDetails)))

          val result = service.getElectionsRows(fiId, year)

          val electionsRows = result.futureValue
          electionsRows.crsRows.rows mustBe empty
          electionsRows.fatcaRows mustBe a[SummaryList]
      }

      "return populated CRS rows and empty FATCA rows when only CRS details are present" in withService {
        service =>
          val testElectionDetails = mock[ElectionDetails]
          when(testElectionDetails.crs).thenReturn(Some(testCrsDetails))
          when(testElectionDetails.fatca).thenReturn(None)

          when(mockConnector.viewElections(eqTo(fiId), eqTo(Some(year)))(any()))
            .thenReturn(Future.successful(Seq(testElectionDetails)))

          val result = service.getElectionsRows(fiId, year)

          val electionsRows = result.futureValue
          electionsRows.crsRows mustBe a[SummaryList]
          electionsRows.fatcaRows.rows mustBe empty
      }

      "return empty SummaryLists when connector returns an empty sequence" in withService {
        service =>
          when(mockConnector.viewElections(eqTo(fiId), eqTo(Some(year)))(any()))
            .thenReturn(Future.successful(Seq.empty[ElectionDetails]))

          val result = service.getElectionsRows(fiId, year)

          val electionsRows = result.futureValue
          electionsRows.crsRows.rows mustBe empty
          electionsRows.fatcaRows.rows mustBe empty
      }

      "return failed response if the call to BE fails" in withService {
        service =>
          when(mockConnector.viewElections(eqTo(fiId), eqTo(Some(year)))(any()))
            .thenReturn(Future.failed(Elections_Error))

          val result = service.getElectionsRows(fiId, year)

          result.failed.futureValue mustBe Elections_Error
      }
    }

    "submitAndDeleteElectionData" - {
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

  private def withService(testService: ElectionsService => Any): Any = {
    val app = applicationBuilder()
      .overrides(bind[ElectionsConnector].toInstance(mockConnector))
      .overrides(bind[SessionRepository].toInstance(mockRepository))
      .build()

    running(app) {
      val service = app.injector.instanceOf[ElectionsService]
      testService(service)
    }
  }
}
