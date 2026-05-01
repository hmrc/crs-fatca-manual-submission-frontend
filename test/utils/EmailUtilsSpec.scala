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

package utils

import base.SpecBase

class EmailUtilsSpec extends SpecBase {

  "formatEmailList" - {
    "return a singular email" in {
      formatEmailList(Seq("email1@test.com")) mustBe "email1@test.com"
    }

    "return two emails joined with 'and'" in {
      formatEmailList(Seq("email1@test.com", "email2@test.com")) mustBe
        "email1@test.com and email2@test.com"
    }

    "return three emails with commas and 'and' before the last when given three emails" in {
      formatEmailList(Seq("email1@test.com", "email2@test.com", "email3@test.com")) mustBe
        "email1@test.com, email2@test.com and email3@test.com"
    }

    "return four emails with commas and 'and' before the last when given four emails" in {
      formatEmailList(Seq("email1@test.com", "email2@test.com", "email3@test.com", "email4@test.com")) mustBe
        "email1@test.com, email2@test.com, email3@test.com and email4@test.com"
    }
  }
}
