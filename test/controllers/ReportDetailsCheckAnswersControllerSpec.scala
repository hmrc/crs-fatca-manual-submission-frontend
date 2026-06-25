package controllers

import base.SpecBase
import models.ReportId
import models.SubmissionsConstants.CRS
import pages.ReportIdPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ReportDetailsCheckAnswersView

class ReportDetailsCheckAnswersControllerSpec extends SpecBase {

  "ReportDetailsCheckAnswers Controller" - {

    val ua   = emptyUserAnswers.withPage(ReportIdPage, ReportId(CRS, 2025, None, "testFiID"))
    val list = SummaryListViewModel(Seq.empty)
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReportDetailsCheckAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReportDetailsCheckAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }
  }
}
