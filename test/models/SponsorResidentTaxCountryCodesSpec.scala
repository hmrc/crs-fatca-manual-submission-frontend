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

class SponsorResidentTaxCountryCodesSpec extends SpecBase {

  "SponsorResidentTaxCountryCodes" - {

    "must return the correct country codes" in {
      val expectedCountryCodes = Seq(
        "GB",
        "GG",
        "JE",
        "IM"
      )

      val actualCountryCodes = SponsorResidentTaxCountryCodes(expectedCountryCodes).getCountryCode(Some(0))

      actualCountryCodes mustEqual expectedCountryCodes.head

      SponsorResidentTaxCountryCodes(expectedCountryCodes).getCountryCode(Some(10)) mustEqual ""
    }
  }
}
