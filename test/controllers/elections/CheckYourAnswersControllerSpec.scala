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
import models.{ElectionsId, FiIdentifiers, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{ElectionsIdPage, FiDetailsPage}
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{CheckYourAnswersValidatorService, ElectionsService}
import uk.gov.hmrc.http.InternalServerException
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    val year                              = 2026
    implicit val electionsId: ElectionsId = ElectionsId(year, "some-fiid")

    "must return OK and the correct view for a GET" in {

      val mockService = mock[CheckYourAnswersValidatorService]
      val testFIName  = "Test FI Name"
      val identifiers = FiIdentifiers("some-fiid", testFIName)
      val userAnswers = UserAnswers(userAnswersId)
        .set(FiDetailsPage, identifiers)
        .success
        .value
        .set(ElectionsIdPage, electionsId)
        .success
        .value
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(bind[CheckYourAnswersValidatorService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.elections.routes.CheckYourAnswersController.onPageLoad(year).url)
        when(mockService.validate(any(), any())(any[ElectionsId]())).thenReturn(Right(()))

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          list,
          2026,
          identifiers,
          "fatca"
        )(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery when FiDetails is missing for a GET" in {

      val mockService = mock[CheckYourAnswersValidatorService]
      val userAnswers = UserAnswers(userAnswersId)
        .set(ElectionsIdPage, electionsId)
        .success
        .value
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(bind[CheckYourAnswersValidatorService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.elections.routes.CheckYourAnswersController.onPageLoad(year).url)
        when(mockService.validate(any(), any())(any[ElectionsId]())).thenReturn(Right(()))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must Redirect when validation fails" in {

      val mockService = mock[CheckYourAnswersValidatorService]
      val testFIName  = "Test FI Name"
      val identifiers = FiIdentifiers("some-fiid", testFIName)
      val userAnswers = emptyUserAnswers
        .withPage(FiDetailsPage, identifiers)
        .withPage(ElectionsIdPage, electionsId)
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(bind[CheckYourAnswersValidatorService].toInstance(mockService))
        .build()

      running(application) {
        when(mockService.validate(any(), any())(any[ElectionsId]())).thenReturn(Left("/error"))
        val request = FakeRequest(GET, controllers.elections.routes.CheckYourAnswersController.onPageLoad(year).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must include("error")
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.elections.routes.CheckYourAnswersController.onPageLoad(year).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "onSubmit" - {
      "must redirect To Election Sent Page when service return success" in {

        val mockService = mock[ElectionsService]
        val testFIName  = "Test FI Name"
        val identifiers = FiIdentifiers("some-fiid", testFIName)
        val userAnswers = emptyUserAnswers
          .withPage(FiDetailsPage, identifiers)
          .withPage(ElectionsIdPage, electionsId)
        when(mockService.submitAndDeleteElectionData(any(), any())(any(), any[ElectionsId]())) thenReturn Future.successful(())

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswers))
            .overrides(
              inject.bind[ElectionsService].toInstance(mockService)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, controllers.elections.routes.CheckYourAnswersController.onSubmit(2026).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.elections.routes.ElectionsSentController.onPageLoad().url
        }
      }

      "must redirect To Journey Recovery when service return failure" in {

        val mockService = mock[ElectionsService]
        val testFIName  = "Test FI Name"
        val identifiers = FiIdentifiers("some-fiid", testFIName)
        val userAnswers = emptyUserAnswers
          .withPage(FiDetailsPage, identifiers)
          .withPage(ElectionsIdPage, electionsId)
        when(mockService.submitAndDeleteElectionData(any(), any())(any(), any[ElectionsId]())) thenReturn Future.failed(InternalServerException("Failed"))

        val application =
          applicationBuilder(maybeUserAnswers = Some(userAnswers))
            .overrides(
              inject.bind[ElectionsService].toInstance(mockService)
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, controllers.elections.routes.CheckYourAnswersController.onSubmit(2026).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

  }
}
