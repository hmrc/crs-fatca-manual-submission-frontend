package controllers.manual.sponsor

import base.SpecBase
import controllers.routes
import models.ReportId
import models.SubmissionsConstants.CRS
import pages.ReportIdPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.CheckAnswersView

class CheckAnswersControllerSpec extends SpecBase {

  "CheckAnswers Controller" - {

    val ua = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS, 2025, None, "testFiID"))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
