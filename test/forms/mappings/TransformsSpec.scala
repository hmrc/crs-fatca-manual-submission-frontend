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

package forms.mappings

import base.SpecBase

class TransformsSpec extends SpecBase with Transforms {

  "formatAmount must" - {

    "strip whitespace" in {
      formatAmount(" 2  3 4") mustBe "234"
      formatAmount("2  3 4. 40") mustBe "234.40"
    }
    "transform decimals to have two decimal places (e.g 5. = 5.00)" in {
      formatAmount("5.2") mustBe "5.20"
      formatAmount("5.1") mustBe "5.10"
    }
    "transform leading decimal to 0. (e.g .5 = 0.5)" in {
      formatAmount(".5") mustBe "0.50"
    }
    "transform leading negative decimal to -0. (e.g -.5 = -0.50)" in {
      formatAmount("-.5") mustBe "-0.50"
    }
    "remove leading zeroes (e.g 005 = 5)" in {
      formatAmount("005") mustBe "5"
      formatAmount("-005") mustBe "-5"
    }
    "transform whole number to integer (e.g 5.00 = 5 " in {
      formatAmount("05.00") mustBe "5"
      formatAmount("5.00") mustBe "5"
      formatAmount("-5.00") mustBe "-5"
    }

    "remove commas (e.g 20,00 = 2000)" in {
      formatAmount("10,00") mustBe "1000"
      formatAmount("-100,0.0,0") mustBe "-1000"
      formatAmount("-12,34567") mustBe "-1234567"
    }

  }
  "formatLargeNumber must" - {
    "format numbers of value 1000+ with commas (e.g 2000 = 2,000)" in {
      formatLargeNumber("1000") mustBe "1,000"
      formatLargeNumber("-1000") mustBe "-1,000"
      formatLargeNumber("-1234567") mustBe "-1,234,567"
      formatLargeNumber("-1234567.89") mustBe "-1,234,567.89"
    }
  }
}
