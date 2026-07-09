package controllers

import base.SpecBase
import models.SubmissionsConstants.CRS
import play.api.test.FakeRequest
import models.ReportId
import play.api.test.Helpers._
import views.html.$className$View
import pages.ReportIdPage
import pages.manual.FINamePage
import repositories.SessionRepository
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind

import scala.concurrent.Future

class $className$ControllerSpec extends SpecBase with MockitoSugar {

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
        val request = FakeRequest(GET, routes.$className$Controller.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
