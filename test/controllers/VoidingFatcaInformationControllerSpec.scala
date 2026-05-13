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
import forms.VoidingFatcaInformationFormProvider
import models.SubmissionsConstants.*
import models.{FatcaVoidCardDetail, FatcaVoidCardModel, SubmissionsConstants, SubmittedReport, VoidReportDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.SubmissionsHistoryPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.VoidService
import utils.DateTimeFormats.*
import views.html.VoidingFatcaInformationView

import java.time.LocalDateTime
import scala.concurrent.Future

class VoidingFatcaInformationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val formProvider      = new VoidingFatcaInformationFormProvider()
  private val form              = formProvider()
  private val originalMessageId = "Some-OMId"

  private lazy val voidingFatcaInformationRoute     = routes.VoidingFatcaInformationController.onPageLoad(originalMessageId).url
  private lazy val voidingFatcaInformationPostRoute = routes.VoidingFatcaInformationController.onSubmit(originalMessageId).url

  "VoidingFatcaInformation Controller" - {
    val fiName          = "ABC Bank plc"
    val year            = "2027"
    val uploadDateTime1 = LocalDateTime.of(2027, 5, 30, 11, 59)
    val uploadDateTime2 = LocalDateTime.of(2027, 5, 28, 9, 25)

    val report1 =
      submittedReport.copy(
        uploadDateTime = uploadDateTime1,
        fiName = fiName,
        messageRefId = "GB2026GB-ABC1234567890-FATCA_003",
        submissionFileType = FATCA1,
        originalMessageRefId = Some(originalMessageId)
      )

    val report2 =
      submittedReport.copy(
        uploadDateTime = uploadDateTime2,
        fiName = fiName,
        messageRefId = "GB2026GB-ABC1234567890-FATCA_003_2",
        originalMessageRefId = Some(originalMessageId),
        submissionFileType = FATCA4
      )

    val fatcaCardDetail1   = FatcaVoidCardDetail("GB2026GB-ABC1234567890-FATCA_003", uploadDateTime1.formatTimeSent, FATCA1)
    val fatcaCardDetail2   = FatcaVoidCardDetail("GB2026GB-ABC1234567890-FATCA_003_2", uploadDateTime2.formatTimeSent, FATCA4)
    val fatcaVoidCardModel = FatcaVoidCardModel(Seq(fatcaCardDetail1, fatcaCardDetail2))

    val submissions = List(report1, report2)

    val userAnswersWithSubmissions = emptyUserAnswers.withPage(SubmissionsHistoryPage, submissions)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userData = Some(userAnswersWithSubmissions)).build()

      running(application) {
        val request = FakeRequest(GET, voidingFatcaInformationRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[VoidingFatcaInformationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, report1.fiName, fatcaVoidCardModel, originalMessageId)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no matching submissions are found" in {

      val application = applicationBuilder(userData = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, voidingFatcaInformationRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when user submits true" in {

      val mockVoidService = mock[VoidService]

      when(mockVoidService.fatcaVoid(any(), any())(any())) thenReturn Future.successful(())
      when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any())) thenReturn Some(
        VoidReportDetails(fatcaVoidCardModel, fiName, report1.fiId, year)
      )

      val application =
        applicationBuilder(userData = Some(userAnswersWithSubmissions))
          .overrides(
            bind[VoidService].toInstance(mockVoidService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, voidingFatcaInformationPostRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/crs-fatca-manual-submission-frontend/fatca-void/information-voided?originalMessageRefId=Some-OMId"

        verify(mockVoidService).fatcaVoid(eqTo(originalMessageId), eqTo(report1.fiId))(any())
      }
    }

    "must redirect to back to manage-reports when user submits false" in {

      val mockVoidService = mock[VoidService]

      when(mockVoidService.fatcaVoid(any(), any())(any())) thenReturn Future.successful(())
      when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any())) thenReturn Some(
        VoidReportDetails(fatcaVoidCardModel, fiName, report1.fiId, year)
      )

      val application =
        applicationBuilder(userData = Some(userAnswersWithSubmissions))
          .overrides(
            bind[VoidService].toInstance(mockVoidService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, voidingFatcaInformationPostRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/crs-fatca-manual-submission-frontend/manage-reports-for-2025?fiId=id&fiName=ABC+Bank+plc"

        verify(mockVoidService, never()).fatcaVoid(eqTo(originalMessageId), eqTo(report1.fiId))(any())
      }
    }

    "must return a Bad Request and error when invalid data is submitted" in {

      val application = applicationBuilder(userData = Some(userAnswersWithSubmissions)).build()

      running(application) {
        val request =
          FakeRequest(POST, voidingFatcaInformationPostRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[VoidingFatcaInformationView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, report1.fiName, fatcaVoidCardModel, originalMessageId)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userData = None).build()

      running(application) {
        val request =
          FakeRequest(POST, voidingFatcaInformationPostRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
