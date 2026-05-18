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
  val year = 2025
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
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserData("id"), None) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
