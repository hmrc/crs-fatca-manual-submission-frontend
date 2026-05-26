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
import pages.elections.{CRSContractsPage, CRSDormantAccountsPage, CRSThresholdsPage}

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator
  val year      = 2025
  "Navigator" - {

    "in Normal mode" - {
      "from IsUsTreasuryRegulatedPage" - {

        "must go to IsApplyingThresholds with year when year is provided" in {
          val userData = UserAnswers("id")
          navigator.nextPage(IsUsTreasuryRegulatedPage, NormalMode, userData, Some(year)) mustBe
            controllers.elections.routes.IsApplyingThresholdsController.onPageLoad(NormalMode, year)
        }

        "must go to JourneyRecovery when year is None" in {
          val userData = UserAnswers("id")
          navigator.nextPage(IsUsTreasuryRegulatedPage, NormalMode, userData, None) mustBe
            routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "from IsApplyingThresholdsPage" - {

        "must go to JourneyRecovery regardless of year" in {
          val userData = UserAnswers("id")
          navigator.nextPage(IsApplyingThresholdsPage, NormalMode, userData, Some(year)) mustBe
            routes.JourneyRecoveryController.onPageLoad()
        }

        "must go to JourneyRecovery when year is None" in {
          val userData = UserAnswers("id")
          navigator.nextPage(IsApplyingThresholdsPage, NormalMode, userData, None) mustBe
            routes.JourneyRecoveryController.onPageLoad()
        }
      }
      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers, None) mustBe routes.IndexController.onPageLoad()
      }

      "must go from elections/crs/thresholds page" - {
        "to /elections/crs/gross-proceeds when the reporting period is 2026 or later" in {
          val reportingYear = 2026
          navigator.nextPage(CRSThresholdsPage,
                             NormalMode,
                             emptyUserAnswers,
                             Some(reportingYear)
          ) mustBe controllers.elections.routes.CarfGrossProceedsController
            .onPageLoad(NormalMode, reportingYear)

        }

        "to /check-your-answers when the reporting period is 2025 or earlier" in {
          val reportingYear = 2025
          navigator.nextPage(CRSThresholdsPage, NormalMode, emptyUserAnswers, Some(reportingYear)) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "must go from /elections/crs/carf-gross-proceeds page" - {
        val reportingPeriod = 2026
        "to /elections/crs/gross-proceeds when the answer is Yes" in {
          val userAnswers = emptyUserAnswers.withPage(CarfGrossProceedsPage, true)
          navigator.nextPage(CarfGrossProceedsPage,
                             NormalMode,
                             userAnswers,
                             Some(reportingPeriod)
          ) mustBe controllers.elections.routes.CrsGrossProceedsController
            .onPageLoad(NormalMode, reportingPeriod)
        }

        "to journey recovery if year is not present" in {
          val userAnswers = emptyUserAnswers.withPage(CarfGrossProceedsPage, true)
          navigator.nextPage(CarfGrossProceedsPage, NormalMode, userAnswers, None) mustBe routes.JourneyRecoveryController.onPageLoad()
        }

        "to /check-your-answers when the answer is No" in {
          val userAnswers = emptyUserAnswers.withPage(CarfGrossProceedsPage, false)
          navigator.nextPage(CarfGrossProceedsPage, NormalMode, userAnswers, Some(reportingPeriod)) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "must go from /elections/crs/crs-gross-proceeds" - {
        "to /check-your-answers" in {
          val userAnswers = emptyUserAnswers.withPage(CrsGrossProceedsPage, false)
          navigator.nextPage(CrsGrossProceedsPage, NormalMode, userAnswers, Some(2026)) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "to journey recovery if year is not present" in {
          val userAnswers = emptyUserAnswers.withPage(CrsGrossProceedsPage, true)
          navigator.nextPage(CrsGrossProceedsPage, NormalMode, userAnswers, None) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }
    }

    "from CRSContractsPage" - {

      "must go to Dormant Accounts Page with year when year is provided" in {
        val userData = UserAnswers("id")
        navigator.nextPage(CRSContractsPage, NormalMode, userData, Some(year)) mustBe
          controllers.elections.routes.CRSDormantAccountsController.onPageLoad(NormalMode, year)
      }

      "must go to JourneyRecovery when year is None" in {
        val userData = UserAnswers("id")
        navigator.nextPage(CRSContractsPage, NormalMode, userData, None) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "from CRSDormantAccountPage" - {

      "must go to Threshold Page with year when year is provided" in {
        val userData = UserAnswers("id")
        navigator.nextPage(CRSDormantAccountsPage, NormalMode, userData, Some(year)) mustBe
          controllers.elections.routes.CRSThresholdsController.onPageLoad(NormalMode, year)
      }

      "must go to JourneyRecovery when year is None" in {
        val userData = UserAnswers("id")
        navigator.nextPage(CRSDormantAccountsPage, NormalMode, userData, None) mustBe
          routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id"), None) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from /elections/crs/carf-gross-proceeds page" - {
        val reportingPeriod = 2026
        "to /elections/crs/gross-proceeds when the answer is Yes" in {
          val userAnswers = emptyUserAnswers.withPage(CarfGrossProceedsPage, true)
          navigator.nextPage(CarfGrossProceedsPage,
                             CheckMode,
                             userAnswers,
                             Some(reportingPeriod)
          ) mustBe controllers.elections.routes.CrsGrossProceedsController
            .onPageLoad(CheckMode, reportingPeriod)
        }

        "to /check-your-answers when the answer is No" in {
          val userAnswers = emptyUserAnswers.withPage(CarfGrossProceedsPage, false)
          navigator.nextPage(CarfGrossProceedsPage, CheckMode, userAnswers, Some(reportingPeriod)) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }
  }
}
