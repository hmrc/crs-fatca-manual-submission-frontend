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
import controllers.manual.reportdetails.routes.{ReportDetailsCheckAnswersController, ReportingYearController, TypeOfReportController}
import models.*
import models.SubmissionsConstants.FATCA
import pages.*
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import pages.manual.sponser.{HaveSponserPage, SponserNamePage}

class ManualSubmissionNavigatorSpec extends SpecBase {

  val navigator = new ManualSubmissionNavigator

  "ManualSubmissionNavigator in NormalMode" - {
    "nextPageWithoutReportId" - {
      "CrsOrFatcaPage" - {
        "must go to Reporting Year Page when Normal Mode" in {
          val userData = UserAnswers("id")
          navigator.nextPageWithoutReportId(CrsOrFatcaPage, NormalMode, userData) mustBe
            ReportingYearController.onPageLoad(NormalMode)
        }

      }
      "ReportingYearPage" - {
        "must go to TypeOfReport Page when Normal Mode" in {
          val userData = UserAnswers("id")
          navigator.nextPageWithoutReportId(ReportingYearPage, NormalMode, userData) mustBe
            TypeOfReportController.onPageLoad(NormalMode)
        }

      }
      "TypeOfReportPage" - {
        "must go to ReportDetailsCheckAnswers" in {
          val ua = UserAnswers("id")
          navigator.nextPageWithoutReportId(TypeOfReportPage, NormalMode, ua) mustBe
            ReportDetailsCheckAnswersController.onPageLoad()
        }

      }
    }
    "nextPage" - {
      implicit val reportId = ReportId(FATCA, 2024, None, "TestFIID")

      "HaveSponserPage" - {
        "must go to SponserName Page when Normal Mode" in {
          val userData = UserAnswers("id").withPage(HaveSponserPage(), true)
          navigator.nextPage(HaveSponserPage(), NormalMode, userData) mustBe
            controllers.manual.sponser.routes.SponserNameController.onPageLoad(NormalMode)
        }

        "must go to UnderConstruction Page when Normal Mode" in {
          val userData = UserAnswers("id").withPage(HaveSponserPage(), false)
          navigator.nextPage(HaveSponserPage(), NormalMode, userData) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }

        "must go to JourneyRecovery Page when Normal Mode" in {
          val userData = UserAnswers("id")
          navigator.nextPage(HaveSponserPage(), NormalMode, userData) mustBe
            controllers.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "SponserNamePage" - {
        "must go to UnderConstruction Page when Normal Mode" in {
          val userData = UserAnswers("id")
          navigator.nextPage(SponserNamePage(), NormalMode, userData) mustBe
            controllers.routes.UnderConstructionController.onPageLoad()
        }
      }
    }
  }
}
