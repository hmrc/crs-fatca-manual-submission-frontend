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
import forms.manual.accountHolders.HaveTaxIdenticationNumberFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.accountHolders.IndividualName
import models.manual.accountHolders.IndividualOrOrganisation.Individual
import models.response.Country
import models.viewModels.AccountHolderId
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.{
  AccountHolderIndividualNamePage,
  CurrentAccountHolderIdPage,
  CurrentTaxResidentCountryIndexPage,
  HaveTaxIdenticationNumberPage,
  IndividualOrOrganisationPage,
  ResidentTaxPage
}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.accountHolders.HaveTaxIdenticationNumberView

import scala.concurrent.Future

class HaveTaxIdenticationNumberControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val currentAccountHolderId = AccountHolderId("acc-id")
  private val currentIndex           = 0
  private val individualName         = IndividualName("Some", "Name")
  private val country                = Country("GB", "United Kingdom")

  implicit private val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private val formProvider = new HaveTaxIdenticationNumberFormProvider()
  private val form         = formProvider()

  private lazy val haveTaxIdenticationNumberRoute =
    controllers.manual.accountHolders.routes.HaveTaxIdenticationNumberController.onPageLoad(NormalMode).url

  "HaveTaxIdenticationNumber Controller" - {

    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentAccountHolderIdPage(), currentAccountHolderId)
      .withPage(IndividualOrOrganisationPage(currentAccountHolderId), Individual)
      .withPage(AccountHolderIndividualNamePage(currentAccountHolderId), individualName)
      .withPage(CurrentTaxResidentCountryIndexPage(currentAccountHolderId, reportId), currentIndex)
      .withPage(ResidentTaxPage(currentIndex, currentAccountHolderId, reportId), country)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdenticationNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HaveTaxIdenticationNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, individualName.fullName, country)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.set(HaveTaxIdenticationNumberPage(currentIndex, currentAccountHolderId, reportId), true).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdenticationNumberRoute)

        val view = application.injector.instanceOf[HaveTaxIdenticationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, individualName.fullName, country)(request, messages(application)).toString
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
          FakeRequest(POST, haveTaxIdenticationNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, haveTaxIdenticationNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[HaveTaxIdenticationNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, individualName.fullName, country)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdenticationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, haveTaxIdenticationNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if the account holder name is not present" in {

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountHolderIdPage(), currentAccountHolderId)
        .withPage(IndividualOrOrganisationPage(currentAccountHolderId), Individual)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdenticationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if the account holder name is not present" in {

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountHolderIdPage(), currentAccountHolderId)
        .withPage(IndividualOrOrganisationPage(currentAccountHolderId), Individual)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request =
          FakeRequest(POST, haveTaxIdenticationNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if the tax resident country index is not present" in {

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountHolderIdPage(), currentAccountHolderId)
        .withPage(IndividualOrOrganisationPage(currentAccountHolderId), Individual)
        .withPage(AccountHolderIndividualNamePage(currentAccountHolderId), individualName)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdenticationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if the resident tax country is not present" in {

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountHolderIdPage(), currentAccountHolderId)
        .withPage(IndividualOrOrganisationPage(currentAccountHolderId), Individual)
        .withPage(AccountHolderIndividualNamePage(currentAccountHolderId), individualName)
        .withPage(CurrentTaxResidentCountryIndexPage(currentAccountHolderId, reportId), currentIndex)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdenticationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if the resident tax country is not present" in {

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(CurrentAccountHolderIdPage(), currentAccountHolderId)
        .withPage(IndividualOrOrganisationPage(currentAccountHolderId), Individual)
        .withPage(AccountHolderIndividualNamePage(currentAccountHolderId), individualName)
        .withPage(CurrentTaxResidentCountryIndexPage(currentAccountHolderId, reportId), currentIndex)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request =
          FakeRequest(POST, haveTaxIdenticationNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if the regime is not CRS" in {

      val fatcaReportId = ReportId(FATCA, 2025, None, "TestfiID")

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, fatcaReportId)
        .withPage(CurrentAccountHolderIdPage()(fatcaReportId), currentAccountHolderId)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, haveTaxIdenticationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if the regime is not CRS" in {

      val fatcaReportId = ReportId(FATCA, 2025, None, "TestfiID")

      val answers = emptyUserAnswers
        .withPage(ReportIdPage, fatcaReportId)
        .withPage(CurrentAccountHolderIdPage()(fatcaReportId), currentAccountHolderId)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request =
          FakeRequest(POST, haveTaxIdenticationNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
