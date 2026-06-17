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
import models.{FatcaVoidCardDetail, FatcaVoidCardModel, FiIdentifiers, ReadSubmissionResponseDetails, UserAnswers, VoidReportDetails, VoidedReportData}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.{FiDetailsPage, VoidedReportDataPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{ConfirmationEmailRecipientsService, SubmissionHistoryService, VoidService}
import utils.DateTimeFormats.*
import views.html.VoidingFatcaInformationView

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class VoidingFatcaInformationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private val mockVoidService              = mock[VoidService]
  private val mockSubmissionHistoryService = mock[SubmissionHistoryService]
  private val formProvider                 = new VoidingFatcaInformationFormProvider()
  private val form                         = formProvider()
  private val originalMessageId            = "Some-OMId"

  private lazy val voidingFatcaInformationRoute =
    routes.VoidingFatcaInformationController.onPageLoad(originalMessageId).url

  private lazy val voidingFatcaInformationPostRoute =
    routes.VoidingFatcaInformationController.onSubmit(originalMessageId).url

  private val mockConfirmationEmailRecipientsService =
    mock[ConfirmationEmailRecipientsService]

  override def beforeEach(): Unit = {
    reset(mockSubmissionHistoryService)
    reset(mockVoidService)
    reset(mockConfirmationEmailRecipientsService)
  }

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

    val fatcaCardDetail1 =
      FatcaVoidCardDetail(
        "GB2026GB-ABC1234567890-FATCA_003",
        uploadDateTime1.formatTimeSent,
        FATCA1
      )

    val fatcaCardDetail2 =
      FatcaVoidCardDetail(
        "GB2026GB-ABC1234567890-FATCA_003_2",
        uploadDateTime2.formatTimeSent,
        FATCA4
      )

    val fatcaVoidCardModel = FatcaVoidCardModel(Seq(fatcaCardDetail1, fatcaCardDetail2))

    val submissions             = ReadSubmissionResponseDetails(List(report1, report2))
    val voidReportDetail        = VoidReportDetails(fatcaVoidCardModel, fiName, report1.fiId, year)
    val userAnswersWithFiDetail = emptyUserAnswers.withPage(FiDetailsPage, FiIdentifiers(report1.fiId, fiName))

    "GET" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
          .overrides(
            bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
            bind[VoidService].toInstance(mockVoidService)
          )
          .build()

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(submissions))

        when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), eqTo(submissions.submissionsList)))
          .thenReturn(Some(voidReportDetail))

        running(application) {
          val request = FakeRequest(GET, voidingFatcaInformationRoute)
          val result  = route(application, request).value
          val view    = application.injector.instanceOf[VoidingFatcaInformationView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, report1.fiName, fatcaVoidCardModel, originalMessageId)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no matching submissions are found for GET" in {

        val application = applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
          .overrides(
            bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService)
          )
          .build()

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.failed(new RuntimeException("something")))

        running(application) {
          val request = FakeRequest(GET, voidingFatcaInformationRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a GET if no matching void card is returned" in {

        val application = applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
          .overrides(
            bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
            bind[VoidService].toInstance(mockVoidService)
          )
          .build()

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(submissions))

        when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), eqTo(submissions.submissionsList)))
          .thenReturn(None)

        running(application) {
          val request = FakeRequest(GET, voidingFatcaInformationRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "POST" - {

      "must redirect to the next page when user submits true and save voided report data" in {

        val mockSession = mock[SessionRepository]
        val emails      = Seq("email1@test.com")

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(submissions))

        when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any()))
          .thenReturn(Some(voidReportDetail))

        when(
          mockConfirmationEmailRecipientsService.getEmailRecipients(eqTo(report1.fiId), any())(any())
        ).thenReturn(Future.successful(emails))

        when(mockVoidService.fatcaVoid(eqTo(originalMessageId), eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(()))

        when(mockSession.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
            .overrides(
              bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
              bind[SessionRepository].toInstance(mockSession),
              bind[VoidService].toInstance(mockVoidService),
              bind[ConfirmationEmailRecipientsService].toInstance(mockConfirmationEmailRecipientsService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, voidingFatcaInformationPostRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.InformationVoidedController.onPageLoad(originalMessageId).url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] =
            ArgumentCaptor.forClass(classOf[UserAnswers])

          verify(mockSession).set(userAnswersCaptor.capture())

          val savedVoidedReportData =
            userAnswersCaptor.getValue.get(VoidedReportDataPage).value

          savedVoidedReportData mustEqual VoidedReportData(
            messageRefIds = Seq(
              "GB2026GB-ABC1234567890-FATCA_003",
              "GB2026GB-ABC1234567890-FATCA_003_2"
            ),
            emails = emails
          )

          verify(mockVoidService).fatcaVoid(eqTo(originalMessageId), eqTo(report1.fiId))(any())
          verify(mockConfirmationEmailRecipientsService).getEmailRecipients(eqTo(report1.fiId), any())(any())
        }
      }

      "must redirect to Journey Recovery when email recipients cannot be retrieved" in {

        val mockSession = mock[SessionRepository]

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(submissions))

        when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any()))
          .thenReturn(Some(voidReportDetail))

        when(
          mockConfirmationEmailRecipientsService.getEmailRecipients(eqTo(report1.fiId), any())(any())
        ).thenReturn(Future.failed(new RuntimeException("email failure")))

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
            .overrides(
              bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
              bind[SessionRepository].toInstance(mockSession),
              bind[VoidService].toInstance(mockVoidService),
              bind[ConfirmationEmailRecipientsService].toInstance(mockConfirmationEmailRecipientsService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, voidingFatcaInformationPostRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

          verify(mockVoidService, never()).fatcaVoid(any(), any())(any())
          verify(mockSession, never()).set(any())
        }
      }

      "must redirect to Journey Recovery when the FATCA void call fails" in {

        val mockSession = mock[SessionRepository]
        val emails      = Seq("email1@test.com")

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(submissions))

        when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any()))
          .thenReturn(Some(voidReportDetail))

        when(
          mockConfirmationEmailRecipientsService.getEmailRecipients(eqTo(report1.fiId), any())(any())
        ).thenReturn(Future.successful(emails))

        when(mockVoidService.fatcaVoid(eqTo(originalMessageId), eqTo(report1.fiId))(any()))
          .thenReturn(Future.failed(new RuntimeException("void failure")))

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
            .overrides(
              bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
              bind[SessionRepository].toInstance(mockSession),
              bind[VoidService].toInstance(mockVoidService),
              bind[ConfirmationEmailRecipientsService].toInstance(mockConfirmationEmailRecipientsService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, voidingFatcaInformationPostRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

          verify(mockSession, never()).set(any())
        }
      }

      "must redirect to Journey Recovery when saving voided report data fails" in {

        val mockSession = mock[SessionRepository]
        val emails      = Seq("email1@test.com")

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(submissions))

        when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any()))
          .thenReturn(Some(voidReportDetail))

        when(
          mockConfirmationEmailRecipientsService.getEmailRecipients(eqTo(report1.fiId), any())(any())
        ).thenReturn(Future.successful(emails))

        when(mockVoidService.fatcaVoid(eqTo(originalMessageId), eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(()))

        when(mockSession.set(any()))
          .thenReturn(Future.failed(new RuntimeException("session failure")))

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
            .overrides(
              bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
              bind[SessionRepository].toInstance(mockSession),
              bind[VoidService].toInstance(mockVoidService),
              bind[ConfirmationEmailRecipientsService].toInstance(mockConfirmationEmailRecipientsService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, voidingFatcaInformationPostRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

          verify(mockVoidService).fatcaVoid(eqTo(originalMessageId), eqTo(report1.fiId))(any())
          verify(mockSession).set(any())
        }
      }

      "must redirect to back to manage-reports when user submits false" in {

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(submissions))

        when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any()))
          .thenReturn(Some(voidReportDetail))

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
            .overrides(
              bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
              bind[VoidService].toInstance(mockVoidService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, voidingFatcaInformationPostRoute)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.ViewSubmissionsController.onPageLoad(LocalDate.now().getYear - 1, report1.fiId).url

          verify(mockVoidService, never()).fatcaVoid(eqTo(originalMessageId), eqTo(report1.fiId))(any())
        }
      }

      "must return a Bad Request and error when invalid data is submitted" in {

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(submissions))

        when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any()))
          .thenReturn(Some(voidReportDetail))

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
            .overrides(
              bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
              bind[VoidService].toInstance(mockVoidService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, voidingFatcaInformationPostRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))
          val view      = application.injector.instanceOf[VoidingFatcaInformationView]
          val result    = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, report1.fiName, fatcaVoidCardModel, originalMessageId)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(maybeUserAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, voidingFatcaInformationPostRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if no matching void card is returned" in {

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.successful(submissions))

        when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any()))
          .thenReturn(None)

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
            .overrides(
              bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
              bind[VoidService].toInstance(mockVoidService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, voidingFatcaInformationPostRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery for a POST if submission history cannot be retrieved" in {

        when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any()))
          .thenReturn(Future.failed(new RuntimeException("submission history failure")))

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswersWithFiDetail))
            .overrides(
              bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService)
            )
            .build()

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
}
