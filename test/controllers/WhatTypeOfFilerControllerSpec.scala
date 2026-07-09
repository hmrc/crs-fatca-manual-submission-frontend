package controllers.manual

import base.SpecBase
import forms.WhatTypeOfFilerFormProvider
import models.SubmissionsConstants.CRS
import models.{NormalMode, ReportId, WhatTypeOfFiler}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhatTypeOfFilerView
import connectors.DatabaseConnector
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.{ReportIdPage, WhatTypeOfFilerPage}
import scala.concurrent.Future

class WhatTypeOfFilerControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val whatTypeOfFilerRoute = routes.WhatTypeOfFilerController.onPageLoad(NormalMode).url

  val formProvider = new WhatTypeOfFilerFormProvider()
  val form = formProvider()

  "WhatTypeOfFiler Controller" - {
    val ua = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS,2025,None,"TestfiID"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, whatTypeOfFilerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatTypeOfFilerView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      implicit val reportId = ReportId(CRS,2025,None,"TestfiID")
      val userAnswers = ua.set(WhatTypeOfFilerPage(), WhatTypeOfFiler.values.head).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatTypeOfFilerRoute)

        val view = application.injector.instanceOf[WhatTypeOfFilerView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(WhatTypeOfFiler.values.head), NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, whatTypeOfFilerRoute)
            .withFormUrlEncodedBody(("value", WhatTypeOfFiler.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatTypeOfFilerRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhatTypeOfFilerView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whatTypeOfFilerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, whatTypeOfFilerRoute)
            .withFormUrlEncodedBody(("value", WhatTypeOfFiler.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
