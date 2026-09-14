package controllers

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import connectors.DatabaseConnector
import forms.manual.cpso.CpsoOrganisationNameFormProvider
import views.html.CpsoOrganisationNameView
import models.SubmissionsConstants.CRS
import models.manual.cpso.CpsoOrganisationName
import models.{NormalMode, ReportId, UserAnswers}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import pages.{ReportIdPage, CpsoOrganisationNamePage}

import scala.concurrent.Future

class CpsoOrganisationNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new CpsoOrganisationNameFormProvider()
  val form = formProvider()

  lazy val cpsoOrganisationNameRoute = routes.CpsoOrganisationNameController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      CpsoOrganisationNamePage.toString -> Json.obj(
        "organizationName" -> "value 1",
        "some-name" -> "value 2"
      )
    )
  )

  "CpsoOrganisationName Controller" - {
    val ua = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS,2025,None,"TestfiID"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, cpsoOrganisationNameRoute)

        val view = application.injector.instanceOf[CpsoOrganisationNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val validAnswer = CpsoOrganisationName("value 1", "value 2")
      implicit val reportId = ReportId(CRS,2025,None,"TestfiID")
      val userAnswers = ua.set(CpsoOrganisationNamePage(), validAnswer).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, cpsoOrganisationNameRoute)

        val view = application.injector.instanceOf[CpsoOrganisationNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(CpsoOrganisationName("value 1", "value 2")), NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, cpsoOrganisationNameRoute)
            .withFormUrlEncodedBody(("organizationName", "value 1"), ("some-name", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, cpsoOrganisationNameRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CpsoOrganisationNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, cpsoOrganisationNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, cpsoOrganisationNameRoute)
            .withFormUrlEncodedBody(("organizationName", "value 1"), ("some-name", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
