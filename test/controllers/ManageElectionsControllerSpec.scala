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
import services.{ElectionsRows, ElectionsService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ManageElectionsView

import java.time.LocalDate
import scala.concurrent.Future

class ManageElectionsControllerSpec extends SpecBase {

  val year   = 2027
  val fiid   = "654321"
  val finame = "testFi"

  val currentYear: Int = LocalDate.now().getYear
  val years: Seq[Int]  = currentYear - 12 to currentYear

  val emptyElectionsRows = ElectionsRows(
    crsRows = SummaryList(rows = Seq.empty),
    fatcaRows = SummaryList(rows = Seq.empty)
  )
  val mockService: ElectionsService   = mock[ElectionsService]
  val mockHeaerCarrier: HeaderCarrier = mock[HeaderCarrier]

  "ManageElections Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockService.getElectionsRows(any(), any())(any(), any()))
        .thenReturn(Future.successful(emptyElectionsRows))

      val application = applicationBuilder(userData = Some(emptyUserAnswers))
        .overrides(bind[ElectionsService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ManageElectionsController.onPageLoad(year, fiid, finame).url)

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
  }

}
