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

package controllers

import base.SpecBase
import org.mockito.ArgumentMatchersSugar.*
import org.mockito.MockitoSugar.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{ElectionsRows, ElectionsService, ViewFIService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.ManageElectionsView

import java.time.LocalDate
import scala.concurrent.Future

class ManageElectionsControllerSpec extends SpecBase {

  private val year   = 2027
  private val fiid   = fiDetail.FIID
  private val finame = fiDetail.FIName

  private val currentYear: Int = LocalDate.now().getYear
  private val years: Seq[Int]  = currentYear - 12 to currentYear

  private val emptyElectionsRows = ElectionsRows(
    crsRows = SummaryList(rows = Seq.empty),
    fatcaRows = SummaryList(rows = Seq.empty)
  )
  private val mockElectionsService: ElectionsService   = mock[ElectionsService]
  private val mockFiService: ViewFIService             = mock[ViewFIService]
  private val mockSessionRepository: SessionRepository = mock[SessionRepository]

  "ManageElections Controller" - {
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

    "must return OK and the correct view for a GET" in {

      when(mockElectionsService.getElectionsRows(any(), any())(any(), any()))
        .thenReturn(Future.successful(emptyElectionsRows))
      when(mockFiService.getFIDetail(any(), eqTo(fiid))(using any())).thenReturn(Future.successful(fiDetail))

      val application = applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[ElectionsService].toInstance(mockElectionsService),
          bind[ViewFIService].toInstance(mockFiService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.elections.routes.ManageElectionsController.onPageLoad(year, fiid).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManageElectionsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          electionYears = years,
          electionsRows = emptyElectionsRows,
          selectedYear = year,
          fiName = finame,
          fiId = fiid
        )(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery when exception is thrown for a GET" in {

      when(mockElectionsService.getElectionsRows(any(), any())(any(), any()))
        .thenReturn(Future.successful(emptyElectionsRows))
      when(mockFiService.getFIDetail(any(), eqTo(fiid))(using any())).thenReturn(Future.failed(new RuntimeException("Failed to get FI details")))

      val application = applicationBuilder(maybeUserAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[ElectionsService].toInstance(mockElectionsService),
          bind[ViewFIService].toInstance(mockFiService),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.elections.routes.ManageElectionsController.onPageLoad(year, fiid).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
