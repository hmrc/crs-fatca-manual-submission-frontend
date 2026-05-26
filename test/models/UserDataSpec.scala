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

package models

import base.SpecBase
import pages.{CarfGrossProceedsPage, CrsGrossProceedsPage}

class UserDataSpec extends SpecBase {

  "CarfGrossProceedsPage" - {
    "must remove CrsGrossProceedsPage when CarfGrossProceedsPage is set to false" in {
      val userData = emptyUserAnswers
        .withPage(CrsGrossProceedsPage, true)
        .withPage(CarfGrossProceedsPage, false)

      userData.get(CrsGrossProceedsPage) mustBe None
    }

    "must not remove CrsGrossProceedsPage when CarfGrossProceedsPage is set to true" in {
      val userData = emptyUserAnswers
        .withPage(CrsGrossProceedsPage, true)
        .withPage(CarfGrossProceedsPage, true)

      userData.get(CrsGrossProceedsPage) mustBe Some(true)
    }
  }
}
