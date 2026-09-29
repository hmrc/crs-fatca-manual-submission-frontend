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
import forms.manual.accountHolders.SelfCertificationFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import models.{NormalMode, ReportId}
import models.manual.accountHolders.SelfCertification
import models.manual.accountHolders.IndividualName
import models.viewModels.AccountHolderId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.accountHolders.SelfCertificationView
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.ReportIdPage
import pages.manual.accountHolders.{AccountHolderIndividualNamePage, CurrentAccountHolderIdPage, SelfCertificationPage}

import scala.concurrent.Future

class SelfCertificationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val selfCertificationRoute = controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(NormalMode).url
  val reportingPeriod             = 2025
  val accountHolderName           = "Some Name"
  val currentId                   = AccountHolderId("acc-id")
  val formProvider                = new SelfCertificationFormProvider()
  val form                        = formProvider()
  implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  "SelfCertification Controller" - {
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
      .withPage(AccountHolderIndividualNamePage(currentId), IndividualName("Some", "Name"))
      .withPage(CurrentAccountHolderIdPage(), currentId)

    "must redirect to journey recovery when fatca is used for a GET " in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(FATCA, 2025, None, "TestfiID"))
        .withPage(AccountHolderIndividualNamePage(currentId), IndividualName("Some", "Name"))
        .withPage(CurrentAccountHolderIdPage(), currentId)

      val application = applicationBuilder(maybeUserAnswers = Some(answer)).build()

      running(application) {
        val request = FakeRequest(GET, selfCertificationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, selfCertificationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelfCertificationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, reportingPeriod, accountHolderName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")
      val userAnswers       = ua.set(SelfCertificationPage(currentId, reportId), SelfCertification.allValidValues.head).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, selfCertificationRoute)

        val view = application.injector.instanceOf[SelfCertificationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(SelfCertification.allValidValues.head), NormalMode, reportingPeriod, accountHolderName)(
          request,
          messages(application)
        ).toString
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
          FakeRequest(POST, selfCertificationRoute)
            .withFormUrlEncodedBody(("value", SelfCertification.allValidValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, selfCertificationRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SelfCertificationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, reportingPeriod, accountHolderName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, selfCertificationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if account holder is not present" in {
      val answers = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), currentId)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, selfCertificationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, selfCertificationRoute)
            .withFormUrlEncodedBody(("value", SelfCertification.allValidValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if account holder is not present" in {
      val answers = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
        .withPage(CurrentAccountHolderIdPage(), currentId)

      val application = applicationBuilder(maybeUserAnswers = Some(answers)).build()

      running(application) {
        val request =
          FakeRequest(POST, selfCertificationRoute)
            .withFormUrlEncodedBody(("value", SelfCertification.allValidValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to journey recovery if FATCA regime is used for a submit" in {
      val answer = emptyUserAnswers
        .withPage(ReportIdPage, ReportId(FATCA, 2025, None, "TestfiID"))
        .withPage(AccountHolderIndividualNamePage(currentId), IndividualName("Some", "Name"))
        .withPage(CurrentAccountHolderIdPage(), currentId)

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(answer))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, selfCertificationRoute)
            .withFormUrlEncodedBody(("value", SelfCertification.allValidValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
