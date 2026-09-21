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
import models.SubmissionsConstants.{CRS, FATCA}
import models.response.Country
import org.scalatest.freespec.AnyFreeSpec

class CountriesSpec extends SpecBase {

  val otherCountry = Country(code = "XX", description = "Other country")

  "Countries" - {

    "must have unique country codes" in {
      val codes = Countries.nonUkTerritories(FATCA).map(_.code)
      codes.distinct mustEqual codes
    }

    "nonUkTerritories must" - {

      "default to CRS country list" in {
        Countries.nonUkTerritories() mustBe Countries.nonUkTerritories(CRS)
      }

      "not include 'Other country' for CRS" in {
        Countries.nonUkTerritories(CRS) must not contain otherCountry
      }

      "add 'Other country' to country list for FATCA" in {
        Countries.nonUkTerritories(FATCA) mustBe Countries.nonUkTerritories :+ otherCountry
      }

    }

  }
}
