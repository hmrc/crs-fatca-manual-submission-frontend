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
import models.SubmissionsConstants.{FATCA1, FATCA4}
import models.viewModels.InformationVoidedViewModel
import models.{FatcaVoidCardDetail, FatcaVoidCardModel, FiIdentifiers, ReadSubmissionResponseDetails, UserAnswers, VoidReportDetails}
import org.mockito.ArgumentMatchersSugar.*
import org.mockito.Mockito.*
import pages.FiDetailsPage
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{SubmissionHistoryService, VoidService}
import utils.DateTimeFormats.formatTimeVoidSubmitted
import views.html.InformationVoidedView

import java.time.{Clock, LocalDateTime, ZoneId}
import scala.concurrent.Future

class InformationVoidedControllerSpec extends SpecBase {

  private val zone          = ZoneId.of("Europe/London")
  private val fixedDateTime = LocalDateTime.of(2026, 4, 28, 15, 36)
  private val fixedClock    = Clock.fixed(fixedDateTime.atZone(zone).toInstant, zone)
  private val dateTime      = fixedDateTime.formatTimeVoidSubmitted

  private val year               = "2027"
  private val originalMessageId  = "Some-OMRId"
  private val fiName             = "someFiName"
  private val fiId               = "some-fiId"
  private val emailString        = "email1@test.com"
  private val cardDetail1        = FatcaVoidCardDetail("GB2026GB-ABC1234567890-FATCA_003", "Sent 30 May 2027 at 11:59", FATCA1)
  private val cardDetail2        = FatcaVoidCardDetail("GB2026GB-ABC1234567890-FATCA_003_2", "Sent 28 May 2027 at 09:25", FATCA4)
  private val fatcaVoidCardModel = FatcaVoidCardModel(Seq(cardDetail1, cardDetail2))
  private val messRefIds         = fatcaVoidCardModel.cardDetailList.map(_.messageRefId).reverse

  private val infoVoidedViewModel = InformationVoidedViewModel(
    fiName = fiName,
    dateTime = dateTime,
    messageRefIds = messRefIds,
    emailString = emailString,
    fiId = fiId
  )
  private val mockVoidService              = mock[VoidService]
  private val mockSubmissionHistoryService = mock[SubmissionHistoryService]

  private val report2 =
    submittedReport.copy(originalMessageRefId = Some(originalMessageId), messageRefId = "GB2026GB-ABC1234567890-FATCA_003_2")

  private val report1 =
    submittedReport.copy(messageRefId = "GB2026GB-ABC1234567890-FATCA_003", fiId = fiId)
  val submissions                          = ReadSubmissionResponseDetails(List(report1, report2))
  val userAnswersWithFiDetail: UserAnswers = emptyUserAnswers.withPage(FiDetailsPage, FiIdentifiers(report1.fiId, fiName))

  override def beforeEach(): Unit = {
    reset(mockSubmissionHistoryService)
    reset(mockVoidService)
  }
  "InformationVoided Controller" - {
    "must return OK and the correct view for a GET" in {

      when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any())).thenReturn(Future.successful(submissions))
      when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any())) thenReturn Some(
        VoidReportDetails(fatcaVoidCardModel, fiName, report1.fiId, year)
      )

      val application = applicationBuilder(userData = Some(userAnswersWithFiDetail))
        .overrides(
          inject.bind[VoidService].toInstance(mockVoidService),
          inject.bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService),
          inject.bind[Clock].toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.InformationVoidedController.onPageLoad(originalMessageId).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[InformationVoidedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(infoVoidedViewModel)(request, messages(application)).toString
      }
    }
    "must redirect to Journey Recovery for a GET if no FI detail is found" in {
      val application = applicationBuilder(userData = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.InformationVoidedController.onPageLoad(originalMessageId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if call to fetch submissions fails" in {

      when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any())).thenReturn(Future.failed(Exception("bad")))

      val application = applicationBuilder(userData = Some(userAnswersWithFiDetail))
        .overrides(inject.bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.InformationVoidedController.onPageLoad(originalMessageId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no matching submissions is found for the messageRefId" in {

      when(mockSubmissionHistoryService.getSubmissionHistory(eqTo(report1.fiId))(any())).thenReturn(Future.successful(submissions))
      when(mockVoidService.getVoidFatcaReportDetails(eqTo(originalMessageId), any())) thenReturn None

      val application = applicationBuilder(userData = Some(userAnswersWithFiDetail))
        .overrides(inject.bind[VoidService].toInstance(mockVoidService), inject.bind[SubmissionHistoryService].toInstance(mockSubmissionHistoryService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.InformationVoidedController.onPageLoad(originalMessageId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
