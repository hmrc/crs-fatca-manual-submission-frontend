package controllers

import base.SpecBase
import forms.$className$FormProvider
import models.$className$
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import connectors.DatabaseConnector
import views.html.$className$View
import models.SubmissionsConstants.CRS
import models.{NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.{ReportIdPage, $className$Page}
import pages.manual.FINamePage
import repositories.SessionRepository

import scala.concurrent.Future

class $className$ControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new $className$FormProvider()
  val form = formProvider()

  lazy val $className;format="decap"$Route = routes.$className$Controller.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      $className$Page.toString -> Json.obj(
        "$field1Name$" -> "value 1",
        "$field2Name$" -> "value 2"
      )
    )
  )

  "$className$ Controller" - {
    val testFiName        = "TestFI"
    implicit val reportId = ReportId(CRS, 2025, None, "TestfiID")
    val frontEndUA        = emptyUserAnswers.withPage(ReportIdPage, reportId)
    val backEndUA         = emptyUserAnswers.withPage(FINamePage(), testFiName)
    val mockRepository    = mock[SessionRepository]

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(backEndUA))
        .overrides(
          bind[SessionRepository].toInstance(mockRepository)
        )
        .build()

      running(application) {
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(frontEndUA)))
        val request = FakeRequest(GET, $className;format="decap"$Route)

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val validAnswer = $className$("value 1", "value 2")
      val userAnswers = backEndUA.set($className$Page(), validAnswer).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockRepository)
        )
        .build()

      running(application) {
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(frontEndUA)))
        val request = FakeRequest(GET, $className;format="decap"$Route)

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill($className$("value 1", "value 2")), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[DatabaseConnector]

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(backEndUA))
          .overrides(
            bind[SessionRepository].toInstance(mockRepository),
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(frontEndUA)))
        val request =
          FakeRequest(POST, $className;format="decap"$Route)
            .withFormUrlEncodedBody(("$field1Name$", "value 1"), ("$field2Name$", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(backEndUA))
        .overrides(
          bind[SessionRepository].toInstance(mockRepository)
        )
        .build()

      running(application) {
        when(mockRepository.get(any())).thenReturn(Future.successful(Some(frontEndUA)))
        val request =
          FakeRequest(POST, $className;format="decap"$Route)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, $className;format="decap"$Route)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, $className;format="decap"$Route)
            .withFormUrlEncodedBody(("$field1Name$", "value 1"), ("$field2Name$", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
