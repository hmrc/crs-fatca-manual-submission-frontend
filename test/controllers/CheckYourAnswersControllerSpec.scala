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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.CheckYourAnswersValidatorService
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    val year = 2026

    "must return OK and the correct view for a GET" in {

      val mockService = mock[CheckYourAnswersValidatorService]

      val application = applicationBuilder(userData = Some(emptyUserData))
        .overrides(bind[CheckYourAnswersValidatorService].toInstance(mockService))
        .build()

      running(application) {
        when(mockService.validate(any(), any())).thenReturn(Right(()))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(year).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must Redirect when validation fails" in {

      val mockService = mock[CheckYourAnswersValidatorService]

      val application = applicationBuilder(userData = Some(emptyUserData))
        .overrides(bind[CheckYourAnswersValidatorService].toInstance(mockService))
        .build()

      running(application) {
        when(mockService.validate(any(), any())).thenReturn(Left("/error"))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(year).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("error")
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userData = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(year).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
