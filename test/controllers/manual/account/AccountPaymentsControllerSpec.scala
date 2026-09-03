package controllers.manual.account

import base.SpecBase
import connectors.DatabaseConnector
import controllers.routes
import forms.manual.account.AccountPaymentsFormProvider
import models.SubmissionsConstants.CRS
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.account.DoYouNeedToAddPaymentsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.manual.account.AccountPaymentsView

import scala.concurrent.Future

class AccountPaymentsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AccountPaymentsFormProvider()
  val form = formProvider()

  lazy val accountPaymentsRoute = controllers.manual.account.routes.AccountPaymentsController.onPageLoad(NormalMode).url

  "AccountPayments Controller" - {

    val ua = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS,2025,None,"TestfiID"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, accountPaymentsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountPaymentsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      implicit val reportId = ReportId(CRS,2025,None,"TestfiID")

      val userAnswers = ua.set(DoYouNeedToAddPaymentsPage(), true).success.value

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, accountPaymentsRoute)

        val view = application.injector.instanceOf[AccountPaymentsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
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
          FakeRequest(POST, accountPaymentsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, accountPaymentsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AccountPaymentsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, accountPaymentsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(maybeUserAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, accountPaymentsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
