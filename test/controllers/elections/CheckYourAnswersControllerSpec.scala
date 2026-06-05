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

package controllers.elections

import base.SpecBase
import controllers.routes
import models.{FiIdentifiers, UserAnswers}
import pages.FiDetailsPage
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import services.CheckYourAnswersValidatorService
import play.api.test.Helpers.*
import services.ElectionsService
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    val year = 2026

    "must return OK and the correct view for a GET" in {

      val mockService = mock[CheckYourAnswersValidatorService]
      val testFIName  = "Test FI Name"
      val userAnswers = UserAnswers(userAnswersId)
        .set(FiDetailsPage, FiIdentifiers("fiID", testFIName))
        .success
        .value
      val application = applicationBuilder(userData = Some(userAnswers))
        .overrides(bind[CheckYourAnswersValidatorService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.elections.routes.CheckYourAnswersController.onPageLoad(year).url)
        when(mockService.validate(any(), any())).thenReturn(Right(()))

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          list,
          2026,
          testFIName,
          "fatca"
        )(request, messages(application)).toString
      }
    }

    "must Redirect when validation fails" in {

      val mockService = mock[CheckYourAnswersValidatorService]

      val application = applicationBuilder(userData = Some(emptyUserAnswers))
        .overrides(bind[CheckYourAnswersValidatorService].toInstance(mockService))
        .build()

      running(application) {
        when(mockService.validate(any(), any())).thenReturn(Left("/error"))
        val request = FakeRequest(GET, controllers.elections.routes.CheckYourAnswersController.onPageLoad(year).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("error")
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userData = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.elections.routes.CheckYourAnswersController.onPageLoad(year).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "onSubmit" - {

      val onSubmitUrl = controllers.elections.routes.CheckYourAnswersController.onSubmit(2026).url
      "must redirect when service return success" in {

        val mockService = mock[ElectionsService]

        when(mockService.submitAndDeleteElectionData(any(), any())(any())) thenReturn Future.successful(())

        val application =
          applicationBuilder(userData = Some(emptyUserAnswers))
            .overrides(
              inject.bind[ElectionsService].toInstance(mockService)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, onSubmitUrl)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.elections.routes.ElectionsSentController.onPageLoad().url
        }
      }
    }

  }
}
