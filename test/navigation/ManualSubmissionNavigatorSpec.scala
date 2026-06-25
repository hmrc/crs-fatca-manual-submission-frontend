package navigation

import base.SpecBase
import models.{NormalMode, UserAnswers}
import pages.TypeOfReportPage

class ManualSubmissionNavigatorSpec extends SpecBase {
  val navigator = new ManualSubmissionNavigator

  "ManualSubmissionNavigator in NormalMode" - {
    "TypeOfReportPage" - {
      "must go to ReportDetailsCheckAnswers" in {
        val ua = UserAnswers("id")
        navigator.nextPage(TypeOfReportPage, NormalMode, ua) mustBe
          controllers.routes.ReportDetailsCheckAnswersController.onPageLoad()
      }

    }
  }
}
