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

/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms

import forms.behaviours.StringFieldBehaviours
import forms.manual.accountHolders.TaxIdentificationNumberFormProvider
import models.ReportId
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.response.Country
import org.scalatest.matchers.should.Matchers.shouldBe

class TaxIdentificationNumberFormProviderSpec extends StringFieldBehaviours {

  private val validData = Map(
    "tin" -> "333388888"
  )

  private val organizationUSA2023Blacklist = Set("000000000", "222222222", "000222111", "333333333", "888888888")
  private val individualUSA2023Blacklist   = Set("000000000", "666666666", "888888888")

  val requiredKey = "taxIdentificationNumber.error.tin.required"
  val lengthKey   = "taxIdentificationNumber.error.tin.length"

  private val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
  private val fr                 = Country("FR", "France")
  private val us                 = Country("US", "U")

  val formProvider = new TaxIdentificationNumberFormProvider()
  val form         = formProvider(fr, reportId, Individual)

  ".tin" - {

    "bind valid data for non US and fatca" in {
      val result = form.bind(validData)
      result.errors shouldBe empty
    }

    "bind valid data for US and fatca" in {
      val reportIdFatca: ReportId = ReportId(FATCA, 2025, None, "TestfiID")
      val formUS                  = formProvider(us, reportIdFatca, Individual)

      val result = formUS.bind(validData.updated("tin", "98096B.00000.LE.555"))
      result.errors shouldBe empty

      // 000-00-0000 format
      val result2 = formUS.bind(validData.updated("tin", "020-10-0330"))
      result2.errors shouldBe empty

      // 00-0000000 format
      val result3 = formUS.bind(validData.updated("tin", "99-0770000"))
      result3.errors shouldBe empty

      // 000000000 format
      val result4 = formUS.bind(validData.updated("tin", "770002244"))
      result4.errors shouldBe empty
    }

    "cannot be empty" in {
      val result = form.bind(validData.updated("tin", ""))
      result.errors("tin").map(_.message) shouldBe Seq("accountHolders.taxIdentificationNumber.error.tin.required")
    }

    "must fail when longer than 200 characters and country not usa" in {
      val tin    = "A" * 201
      val result = form.bind(validData.updated("tin", tin))
      result.errors("tin").map(_.message) shouldBe Seq("accountHolders.taxIdentificationNumber.error.tin.length")
    }

    "must fail when invalid characters are provided and country not usa" in {
      val doubleDash           = "4444--444"
      val ampersandHash        = "4444&#444"
      val forwardSlashAsterisk = "4444/*444"

      val result = form.bind(validData.updated("tin", doubleDash))
      result.errors("tin").map(_.message) shouldBe Seq("accountHolders.taxIdentificationNumber.error.tin.invalidChars")

      val result_ampersandHash = form.bind(validData.updated("tin", ampersandHash))
      result_ampersandHash.errors("tin").map(_.message) shouldBe Seq("accountHolders.taxIdentificationNumber.error.tin.invalidChars")

      val result_forwardSlashAsterisk = form.bind(validData.updated("tin", forwardSlashAsterisk))
      result_forwardSlashAsterisk.errors("tin").map(_.message) shouldBe Seq("accountHolders.taxIdentificationNumber.error.tin.invalidChars")
    }

    "must fail when invalid Giin or SSN is entered for fatca regime and country USA" in {
      val reportIdFatca: ReportId = ReportId(FATCA, 2025, None, "TestfiID")
      val formUS                  = formProvider(us, reportIdFatca, Individual)
      val invalidGiin             = "98O96B.00000.LE.350"

      val result = formUS.bind(validData.updated("tin", invalidGiin))
      result.errors("tin").map(_.message) shouldBe Seq("accountHolders.taxIdentificationNumber.error.tin.giinOrSsn.invalid")

      // 98096B.00000.LE.555
      val wrongFormat        = "000001.LE.555.98096B"
      val result_wrongFormat = formUS.bind(validData.updated("tin", wrongFormat))
      result_wrongFormat.errors("tin").map(_.message) shouldBe Seq("accountHolders.taxIdentificationNumber.error.tin.giinOrSsn.invalid")

      // 000-00-0000
      formUS.bind(validData.updated("tin", "1010-00-0000")).errors("tin").map(_.message) shouldBe Seq(
        "accountHolders.taxIdentificationNumber.error.tin.giinOrSsn.invalid"
      )

      // 00-0000000
      formUS.bind(validData.updated("tin", "080-0001000")).errors("tin").map(_.message) shouldBe Seq(
        "accountHolders.taxIdentificationNumber.error.tin.giinOrSsn.invalid"
      )

      // 000000000
      formUS.bind(validData.updated("tin", "20000000003")).errors("tin").map(_.message) shouldBe Seq(
        "accountHolders.taxIdentificationNumber.error.tin.giinOrSsn.invalid"
      )

    }

    "must fail when value entered for fatca regime and country USA for 2023 and onwards and it is an organization is blacklisted" in {
      val reportIdFatca: ReportId = ReportId(FATCA, 2023, None, "TestfiID")
      val formUS                  = formProvider(us, reportIdFatca, Organisation)

      organizationUSA2023Blacklist.foreach {
        v =>
          val result = formUS.bind(validData.updated("tin", v))
          result.errors("tin").map(_.message) shouldBe Seq("accountHolders.taxIdentificationNumber.error.tin.organization.is.blacklisted")
      }
    }

    "must fail when value entered for fatca regime and country USA for 2023 and onwards and it is an Individual is blacklisted" in {
      val reportIdFatca: ReportId = ReportId(FATCA, 2023, None, "TestfiID")
      val formUS                  = formProvider(us, reportIdFatca, Individual)

      individualUSA2023Blacklist.foreach {
        v =>
          val result = formUS.bind(validData.updated("tin", v))
          result.errors("tin").map(_.message) shouldBe Seq("accountHolders.taxIdentificationNumber.error.tin.individual.is.blacklisted")
      }
    }
  }
}
