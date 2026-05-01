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
import models.{FatcaCardDetail, FatcaVoidCardModel, VoidReportDetails}
import org.mockito.ArgumentMatchersSugar.*
import org.mockito.Mockito.*
import pages.SubmissionsHistoryPage
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.VoidService
import views.html.InformationVoidedView

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, ZoneId, ZoneOffset}

class InformationVoidedControllerSpec extends SpecBase {

  private val zone = ZoneId.of("Europe/London")
  private val fixedDateTime = LocalDateTime.of(2026, 4, 28, 15, 36).atZone(zone)
  private val dateTime      = fixedDateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma"))
  private val fixedClock    = Clock.fixed(fixedDateTime.toInstant, zone)

  private val originalMessageId  = "Some-OMRId"
  private val fiName             = "someFiName"
  private val emailString        = "email1@test.com"
  private val cardDetail1        = FatcaCardDetail("GB2026GB-ABC1234567890-FATCA_003", "30 May 2027", "11:59", "FATCA")
  private val cardDetail2        = FatcaCardDetail("GB2026GB-ABC1234567890-FATCA_003_2", "28 May 2027", "09:25", "FATCA")
  private val fatcaVoidCardModel = FatcaVoidCardModel(Seq(cardDetail1, cardDetail2))
  private val messRefIds         = fatcaVoidCardModel.cardDetailList.map(_.messageRefId)

  private val report2 =
    submittedReport.copy(originalMessageRefId = Some(originalMessageId), messageRefId = "GB2026GB-ABC1234567890-FATCA_003_2")

  private val report1 =
    submittedReport.copy(messageRefId = "GB2026GB-ABC1234567890-FATCA_003")
  private val submissions = List(report1, report2)

  "InformationVoided Controller" - {
    "must return OK and the correct view for a GET" in {

      val userAnswersWithSubmissions = emptyUserAnswers.withPage(SubmissionsHistoryPage, submissions)

      val mockVoidService = mock[VoidService]
      when(mockVoidService.getVoidReportDetails(eqTo(originalMessageId), any())) thenReturn Some(VoidReportDetails(fatcaVoidCardModel, fiName, report1.fiId))

      val application = applicationBuilder(userAnswers = Some(userAnswersWithSubmissions))
        .overrides(
          inject.bind[VoidService].toInstance(mockVoidService),
          inject.bind[Clock].toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.InformationVoidedController.onPageLoad(originalMessageId).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[InformationVoidedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fiName, dateTime, messRefIds, emailString)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no matching submissions are found" in {

      val mockVoidService = mock[VoidService]
      when(mockVoidService.getVoidReportDetails(eqTo(originalMessageId), any())) thenReturn None

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(inject.bind[VoidService].toInstance(mockVoidService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.InformationVoidedController.onPageLoad(originalMessageId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "formatEmailList" - {
      val controller = new InformationVoidedController(null, null, null, null, null, null, null, null)
      "return a singular email" in {
        controller.formatEmailList(Seq("email1@test.com")) mustBe "email1@test.com"
      }

      "return two emails joined with 'and'" in {
        controller.formatEmailList(Seq("email1@test.com", "email2@test.com")) mustBe
          "email1@test.com and email2@test.com"
      }

      "return three emails with commas and 'and' before the last when given three emails" in {
        controller.formatEmailList(Seq("email1@test.com", "email2@test.com", "email3@test.com")) mustBe
          "email1@test.com, email2@test.com and email3@test.com"
      }

      "return four emails with commas and 'and' before the last when given four emails" in {
        controller.formatEmailList(Seq("email1@test.com", "email2@test.com", "email3@test.com", "email4@test.com")) mustBe
          "email1@test.com, email2@test.com, email3@test.com and email4@test.com"
      }
    }
  }
}
