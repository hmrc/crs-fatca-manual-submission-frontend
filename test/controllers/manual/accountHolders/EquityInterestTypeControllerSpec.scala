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
import forms.manual.accountHolders.EquityInterestTypeFormProvider
import models.SubmissionsConstants.CRS
import models.manual.accountHolders.{EquityInterestType, EquityInterestTypes, IndividualName}
import models.manual.accountHolders.IndividualOrOrganisation.Individual
import models.viewModels.AccountHolderId
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.accountHolders.{AccountHolderIndividualNamePage, CurrentAccountHolderIdPage, EquityInterestTypePage, IndividualOrOrganisationPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.accountHolders.EquityInterestTypeView

import scala.concurrent.Future

class EquityInterestTypeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val form = new EquityInterestTypeFormProvider()()

  private lazy val equityInterestTypeRoute = controllers.manual.accountHolders.routes.EquityInterestTypeController.onPageLoad(NormalMode).url

  "EquityInterestType Controller" - {

    val reportId               = ReportId(CRS, 2025, None, "TestfiID")
    val currentAccountHolderId = AccountHolderId("testid")
    val individualName         = IndividualName("John", "Smith")
    val accountHolderName      = individualName.fullName

    val validAnswer = EquityInterestTypes(
      Seq(
        EquityInterestType.SettlorOfTrust,
        EquityInterestType.TrusteeOfTrust
      )
    )

    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentAccountHolderIdPage()(reportId), currentAccountHolderId)
      .withPage(IndividualOrOrganisationPage(currentAccountHolderId)(reportId), Individual)
      .withPage(AccountHolderIndividualNamePage(currentAccountHolderId)(reportId), individualName)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, equityInterestTypeRoute)

        val view = application.injector.instanceOf[EquityInterestTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, accountHolderName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.withPage(EquityInterestTypePage(currentAccountHolderId, reportId), validAnswer)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, equityInterestTypeRoute)

        val view = application.injector.instanceOf[EquityInterestTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, accountHolderName)(request, messages(application)).toString
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
          FakeRequest(POST, equityInterestTypeRoute)
            .withFormUrlEncodedBody(
              ("types[0]", EquityInterestType.SettlorOfTrust.toString),
              ("types[1]", EquityInterestType.TrusteeOfTrust.toString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, equityInterestTypeRoute)
            .withFormUrlEncodedBody(("types[0]", "invalid value"))

        val boundForm = form.bind(Map("types[0]" -> "invalid value"))

        val view = application.injector.instanceOf[EquityInterestTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, accountHolderName)(request, messages(application)).toString
      }
    }

    "must return a Bad Request when no checkboxes are selected" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(POST, equityInterestTypeRoute)

        val boundForm = form.bind(Map.empty[String, String])

        val view = application.injector.instanceOf[EquityInterestTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, accountHolderName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, equityInterestTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, equityInterestTypeRoute)
            .withFormUrlEncodedBody(("types[0]", EquityInterestType.SettlorOfTrust.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
