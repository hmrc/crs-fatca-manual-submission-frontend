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

package controllers.manual.filercatagory

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.manual.filercatagory.WhatTypeOfFilerIsSponsorFormProvider
import models.SubmissionsConstants.CRS
import models.{NormalMode, ReportId}
import models.manual.filercatagory.WhatTypeOfFilerIsSponsor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.manual.filercatagory.WhatTypeOfFilerIsSponsorView
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.ReportIdPage
import pages.manual.filercatagory.WhatTypeOfFilerIsSponsorPage
import scala.concurrent.Future

class WhatTypeOfFilerIsSponsorControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val whatTypeOfFilerIsSponsorRoute = controllers.manual.filercatagory.routes.WhatTypeOfFilerIsSponsorController.onPageLoad(NormalMode).url

  val formProvider = new WhatTypeOfFilerIsSponsorFormProvider()
  val form = formProvider()

  "WhatTypeOfFilerIsSponsor Controller" - {
    val ua = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS,2025,None,"TestfiID"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, whatTypeOfFilerIsSponsorRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatTypeOfFilerIsSponsorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      implicit val reportId = ReportId(CRS,2025,None,"TestfiID")
      val userAnswers = ua.set(WhatTypeOfFilerIsSponsorPage(), WhatTypeOfFilerIsSponsor.values.head).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatTypeOfFilerIsSponsorRoute)

        val view = application.injector.instanceOf[WhatTypeOfFilerIsSponsorView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(WhatTypeOfFilerIsSponsor.values.head), NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, whatTypeOfFilerIsSponsorRoute)
            .withFormUrlEncodedBody(("value", WhatTypeOfFilerIsSponsor.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatTypeOfFilerIsSponsorRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhatTypeOfFilerIsSponsorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whatTypeOfFilerIsSponsorRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, whatTypeOfFilerIsSponsorRoute)
            .withFormUrlEncodedBody(("value", WhatTypeOfFilerIsSponsor.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
