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

package navigation

import base.SpecBase
import models.*
import pages.*

class ManualSubmissionNavigatorSpec extends SpecBase {

  val navigator = new ManualSubmissionNavigator

  "ManualSubmissionNavigator in NormalMode" - {
    "CrsOrFatcaPage" - {
      "must go to Reporting Year Page when Normal Mode" in {
        val userData = UserAnswers("id")
        navigator.nextPage(CrsOrFatcaPage, NormalMode, userData) mustBe
          controllers.routes.ReportingYearController.onPageLoad(NormalMode)
      }

    }

    "ReportingYearPage" - {
      "must go to TypeOfReport Page when Normal Mode" in {
        val userData = UserAnswers("id")
        navigator.nextPage(ReportingYearPage, NormalMode, userData) mustBe
          controllers.routes.TypeOfReportController.onPageLoad(NormalMode)
      }

    }

    "TypeOfReportPage" - {
      "must go to ReportDetailsCheckAnswers" in {
        val ua = UserAnswers("id")
        navigator.nextPage(TypeOfReportPage, NormalMode, ua) mustBe
          controllers.routes.ReportDetailsCheckAnswersController.onPageLoad()
      }

    }
  }
}
