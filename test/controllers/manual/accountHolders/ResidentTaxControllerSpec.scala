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
import forms.manual.accountHolders.ResidentTaxFormProvider
import models.SubmissionsConstants.CRS
import models.manual.accountHolders.IndividualName
import models.response.Country
import models.response.Country.GB
import models.viewModels.AccountHolderId
import models.{Countries, NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.{AccountHolderIndividualNamePage, CurrentAccountHolderIdPage, ResidentTaxPage, TaxResidentCountriesListPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.accountHolders.ResidentTaxView

import scala.concurrent.Future

class ResidentTaxControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val currentAccountHolderId = AccountHolderId("testid")
  private val reportId               = ReportId(CRS, 2025, None, "TestfiID")
  private val individualName         = IndividualName("test", "last")

  private val formProvider = new ResidentTaxFormProvider()
  private val form         = formProvider()

  private lazy val residentTaxRoute = controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(NormalMode).url

  "ResidentTax Controller" - {

    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentAccountHolderIdPage()(reportId), currentAccountHolderId)
      .withPage(AccountHolderIndividualNamePage(currentAccountHolderId)(reportId), individualName)
      .withPage(TaxResidentCountriesListPage(currentAccountHolderId, reportId), Seq(GB))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, residentTaxRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ResidentTaxView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, individualName.fullName, Countries.allCountries(reportId.regime))(request,
                                                                                                                                   messages(application)
        ).toString
      }
    }

    "must redirect to journey recovery when account holder name is not present for a GET" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountHolderIdPage()(reportId), currentAccountHolderId)

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request = FakeRequest(GET, residentTaxRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, residentTaxRoute)
            .withFormUrlEncodedBody(("country", "GB"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, residentTaxRoute)
            .withFormUrlEncodedBody(("country", ""))

        val boundForm = form.bind(Map("country" -> ""))

        val view = application.injector.instanceOf[ResidentTaxView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, individualName.fullName, Countries.allCountries(reportId.regime))(request,
                                                                                                                                        messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, residentTaxRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, residentTaxRoute)
            .withFormUrlEncodedBody(("country", "GB"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no account holder name is found" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountHolderIdPage()(reportId), currentAccountHolderId)

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request =
          FakeRequest(POST, residentTaxRoute)
            .withFormUrlEncodedBody(("country", "GB"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
