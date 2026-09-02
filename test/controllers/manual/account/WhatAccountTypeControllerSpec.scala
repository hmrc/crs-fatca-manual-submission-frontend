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

package controllers.manual.account

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.manual.account.WhatAccountTypeFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.account.WhatAccountType
import models.viewModels.AccountId
import models.{NormalMode, NumberType, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.account.{CurrentAccountIdPage, NumberTypePage, WhatAccountTypePage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.manual.account.WhatAccountTypeView

import scala.concurrent.Future

class WhatAccountTypeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private lazy val whatAccountTypeRoute = controllers.manual.account.routes.WhatAccountTypeController.onPageLoad(NormalMode).url
  private val accountId: AccountId      = AccountId(value = "SomeId")
  private val numType                   = NumberType.Other
  private val formProvider              = new WhatAccountTypeFormProvider()
  private val form                      = formProvider()

  private def items(app: Application): Seq[RadioItem] =
    WhatAccountType.options(2025, Some(numType))(messages(app))

  "WhatAccountType Controller" - {
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, ReportId(CRS, 2025, None, "TestfiID"))
      .withPage(CurrentAccountIdPage(), accountId)
      .withPage(NumberTypePage(accountId), numType)

    "must redirect to JourneyRecovery when regime is FATCA" in {
      val uaWithFatca = ua.withPage(ReportIdPage, ReportId(FATCA, 2025, None, "TestfiID"))
      val application =
        applicationBuilder(
          maybeUserAnswers = Some(uaWithFatca.withPage(NumberTypePage(accountId), NumberType.Other))
        )
          .build()

      running(application) {
        val request = FakeRequest(GET, whatAccountTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must save Depository and redirect to the next page on a GET when NumberType is Iban" in {
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua.withPage(NumberTypePage(accountId), NumberType.Iban)))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, whatAccountTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must save Depository and redirect to the next page on a GET when NumberType is Semp" in {
      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua.withPage(NumberTypePage(accountId), NumberType.Semp)))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, whatAccountTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, whatAccountTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatAccountTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, items(application))(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")
      val userAnswers       = ua.set(WhatAccountTypePage(accountId), WhatAccountType.baseValues.head).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatAccountTypeRoute)

        val view = application.injector.instanceOf[WhatAccountTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(WhatAccountType.baseValues.head), NormalMode, items(application))(request,
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
          FakeRequest(POST, whatAccountTypeRoute)
            .withFormUrlEncodedBody(("value", WhatAccountType.baseValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatAccountTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhatAccountTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, items(application))(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whatAccountTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, whatAccountTypeRoute)
            .withFormUrlEncodedBody(("value", WhatAccountType.baseValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
