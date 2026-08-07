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
import forms.manual.account.WhatWasTheAccountCurrencyFormProvider
import models.SubmissionsConstants.CRS
import models.viewModels.AccountId
import models.{Currency, NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.account.{CurrentAccountIdPage, WhatWasTheAccountCurrencyPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.manual.account.WhatWasTheAccountCurrencyView

import scala.concurrent.Future

class WhatWasTheAccountCurrencyControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val regime       = CRS
  private val formProvider = new WhatWasTheAccountCurrencyFormProvider()
  private val form         = formProvider(regime)
  private val answer       = Currency.GBP

  private lazy val whatWasTheAccountCurrencyRoute = controllers.manual.account.routes.WhatWasTheAccountCurrencyController.onPageLoad(NormalMode).url

  "WhatWasTheAccountCurrency Controller" - {

    val accountId: AccountId        = AccountId("TestAccountId")
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentAccountIdPage()(reportId), accountId)

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, whatWasTheAccountCurrencyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatWasTheAccountCurrencyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, regime)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = ua.set(WhatWasTheAccountCurrencyPage(accountId), answer).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatWasTheAccountCurrencyRoute)

        val view = application.injector.instanceOf[WhatWasTheAccountCurrencyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(answer), NormalMode, regime)(request, messages(application)).toString
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
          FakeRequest(POST, whatWasTheAccountCurrencyRoute)
            .withFormUrlEncodedBody(("currency", "GBP"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatWasTheAccountCurrencyRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WhatWasTheAccountCurrencyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, regime)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whatWasTheAccountCurrencyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, whatWasTheAccountCurrencyRoute)
            .withFormUrlEncodedBody(("value", "GBP"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
