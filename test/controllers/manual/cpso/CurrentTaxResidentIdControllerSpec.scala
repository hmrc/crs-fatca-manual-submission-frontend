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
import models.SubmissionsConstants.CRS
import models.manual.cpso.{IndividualName, IndividualOrOrganisation}
import models.response.Country
import models.viewModels.manual.cpso.CPSOId
import models.{CheckMode, ReportId, TaxResidentInformation}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.FINamePage
import pages.manual.cpso.*
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class CurrentTaxResidentIdControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val GB    = Country("GB", "United Kingdom")
  val OTHER = Country("Other", "Other country")

  "CurrentTaxResidentId Controller" - {
    val testFiName        = "TestFI"
    implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")
    val ua                = emptyUserAnswers.withPage(ReportIdPage, reportId).withPage(FINamePage(), testFiName)

    "must populate the redirect to change URL" in {

      lazy val currentTaxResidentRoute = controllers.manual.cpso.routes.CurrentTaxResidentIdController.onChangeRedirect(1).url

      val currentCPSOId = CPSOId("testid")
      val userAnswers = ua
        .withPage(CurrentCPSOIdPage(), currentCPSOId)
        .withPage(IndividualOrOrganisationPage(currentCPSOId), IndividualOrOrganisation.Organisation)
        .withPage(IndividualNamePage(currentCPSOId), IndividualName("firstName", "lastName"))
        .withPage(TaxResidentCountriesListPage(currentCPSOId, reportId), Seq(TaxResidentInformation(GB, None, None), TaxResidentInformation(OTHER, None, None)))

      val mockDBConnector = mock[DatabaseConnector]

      when(mockDBConnector.set(any())(any())) thenReturn Future.successful(())

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(
          bind[DatabaseConnector].toInstance(mockDBConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, currentTaxResidentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(CheckMode).url
      }
    }

    "must populate the redirect to Remove URL" in {

      lazy val currentTaxResidentRoute = controllers.manual.cpso.routes.CurrentTaxResidentIdController.onRemoveRedirect(1).url

      val currentCPSOId = CPSOId("testid")
      val userAnswers = ua
        .withPage(CurrentCPSOIdPage(), currentCPSOId)
        .withPage(IndividualOrOrganisationPage(currentCPSOId), IndividualOrOrganisation.Organisation)
        .withPage(IndividualNamePage(currentCPSOId), IndividualName("firstName", "lastName"))
        .withPage(TaxResidentCountriesListPage(currentCPSOId, reportId), Seq(TaxResidentInformation(GB, None, None), TaxResidentInformation(OTHER, None, None)))

      val mockDBConnector = mock[DatabaseConnector]

      when(mockDBConnector.set(any())(any())) thenReturn Future.successful(())

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(
          bind[DatabaseConnector].toInstance(mockDBConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, currentTaxResidentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.cpso.routes.RemoveTaxResidentCountryController.onPageLoad(CheckMode).url
      }
    }

  }

}
