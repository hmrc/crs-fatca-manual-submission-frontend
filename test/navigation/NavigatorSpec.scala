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
import controllers.routes
import models.*
import pages.*

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator
  val year      = 2025
  "Navigator" - {

    "in Normal mode" - {
      "from IsUsTreasuryRegulatedPage" - {

        "must go to IsApplyingThresholds with year when year is provided" in {
          val userData = UserData("id")
          navigator.nextPage(IsUsTreasuryRegulatedPage, NormalMode, userData, Some(year)) mustBe
            controllers.elections.routes.IsApplyingThresholdsController.onPageLoad(NormalMode, year)
        }

        "must go to JourneyRecovery when year is None" in {
          val userData = UserData("id")
          navigator.nextPage(IsUsTreasuryRegulatedPage, NormalMode, userData, None) mustBe
            routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "from IsApplyingThresholdsPage" - {

        "must go to JourneyRecovery regardless of year" in {
          val userData = UserData("id")
          navigator.nextPage(IsApplyingThresholdsPage, NormalMode, userData, Some(year)) mustBe
            routes.JourneyRecoveryController.onPageLoad()
        }

        "must go to JourneyRecovery when year is None" in {
          val userData = UserData("id")
          navigator.nextPage(IsApplyingThresholdsPage, NormalMode, userData, None) mustBe
            routes.JourneyRecoveryController.onPageLoad()
        }
      }
      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserData("id"), None) mustBe routes.IndexController.onPageLoad()
      }

      "must go from elections/crs/thresholds page" - {
        "to /elections/crs/gross-proceeds when the reporting period is 2026 or later" in {
          val userAnswers: UserData = emptyUserAnswers.withPage(CRSReportingPeriodPage, 2026)
          navigator.nextPage(CRSThresholdsPage, NormalMode, userAnswers) mustBe controllers.elections.routes.CarfGrossProceedsController.onPageLoad(NormalMode)

        }

        "to /check-your-answers when the reporting period is 2025 or earlier" in {
          val userAnswers: UserData = emptyUserAnswers.withPage(CRSReportingPeriodPage, 2025)
          navigator.nextPage(CRSThresholdsPage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "to /journey-recovery when the reporting period is not found" in {
          val userAnswers: UserData = emptyUserAnswers
          navigator.nextPage(CRSThresholdsPage, NormalMode, userAnswers) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from /elections/crs/carf-gross-proceeds page" - {
        "to /elections/crs/gross-proceeds when the answer is Yes" in {
          val userAnswers = emptyUserAnswers.withPage(CarfGrossProceedsPage, true)
          navigator.nextPage(CarfGrossProceedsPage, NormalMode, userAnswers) mustBe controllers.elections.routes.CrsGrossProceedsController
            .onPageLoad(NormalMode)
        }

        "to /check-your-answers when the answer is No" in {
          val userAnswers = emptyUserAnswers.withPage(CarfGrossProceedsPage, false)
          navigator.nextPage(CarfGrossProceedsPage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "to /journey-recovery when the answer is not found" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(CarfGrossProceedsPage, NormalMode, userAnswers) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "must go from /elections/crs/crs-gross-proceeds" - {
        "to /check-your-answers when the answers" in {
          val userAnswers = emptyUserAnswers.withPage(CrsGrossProceedsPage, false)
          navigator.nextPage(CrsGrossProceedsPage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserData("id"), None) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from /elections/crs/carf-gross-proceeds page" - {
        "to /elections/crs/gross-proceeds when the answer is Yes" in {
          val userAnswers = emptyUserAnswers.withPage(CarfGrossProceedsPage, true)
          navigator.nextPage(CarfGrossProceedsPage, CheckMode, userAnswers) mustBe controllers.elections.routes.CrsGrossProceedsController
            .onPageLoad(NormalMode)
        }

        "to /check-your-answers when the answer is No" in {
          val userAnswers = emptyUserAnswers.withPage(CarfGrossProceedsPage, false)
          navigator.nextPage(CarfGrossProceedsPage, CheckMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "to /journey-recovery when the answer is not found" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(CarfGrossProceedsPage, CheckMode, userAnswers) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }
    }
  }
}
