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
import config.FrontendAppConfig
import models.SubmissionsConstants.{CRS, FATCA}
import models.response.{AddressDetails, TINDetails, TINType}
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class CheckDetailsForFinancialInstitutionUtilSpec extends SpecBase {

  private val frontendUrl   = "fi-management-url"
  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.fIManagementFrontendUrl).thenReturn(frontendUrl)
  private val util = new CheckDetailsForFinancialInstitutionUtil(mockAppConfig)

  implicit private val messages: Messages = stubMessages()

  "CheckDetailsForFinancialInstitutionUtil" - {

    "changeFiUrl" - {

      "must return the registered business change URL for an FI user" in {
        util.changeFiUrl("fi-123", isFIUser = true) mustBe s"$frontendUrl/registered-business/change-answers/fi-123"
      }

      "must return the standard change URL for a non-user FI" in {
        util.changeFiUrl("fi-123", isFIUser = false) mustBe s"$frontendUrl/change-answers/fi-123"
      }
    }

    "getRows" - {

      "must include FI name, address and TIN rows but omit GIIN for CRS" in {
        val detail = fiDetail.copy(
          FIName = "Example FI",
          AddressDetails = AddressDetails(
            AddressLine1 = "Line 1",
            AddressLine2 = Some("Line 2"),
            AddressLine3 = None,
            AddressLine4 = Some("Line 4"),
            CountryCode = Some("GB"),
            PostalCode = Some("AB1 2CD")
          ),
          GIIN = Some("GIIN-123"),
          IsFIUser = false,
          TINDetails = Some(
            Seq(
              TINDetails(TINType.UTR, "1234567890", "GB"),
              TINDetails(TINType.CRN, "CRN123", "GB")
            )
          )
        )

        val result = util.getRows(detail, CRS)

        result.rows.map(
          row => row.key.content -> row.value.content
        ) mustBe Seq(
          HtmlContent(messages("CheckDetailsForFinancialInstitution.key.fiName")) -> HtmlContent("Example FI"),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.value.address")) -> HtmlContent(
            "Line 1<br>Line 2<br>Line 4<br>GB<br>AB1 2CD"
          ),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.tinType.utr")) -> HtmlContent("1234567890"),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.tinType.crn")) -> HtmlContent("CRN123")
        )
      }

      "must include the CRN row when a UTR is absent" in {
        val detail = fiDetail.copy(
          IsFIUser = false,
          TINDetails = Some(Seq(TINDetails(TINType.CRN, "CRN123", "GB")))
        )

        val tinRows = util.getRows(detail, CRS).rows.drop(2)

        tinRows.map(
          row => row.key.content -> row.value.content
        ) mustBe Seq(
          HtmlContent(messages("CheckDetailsForFinancialInstitution.tinType.crn")) -> HtmlContent("CRN123")
        )
      }

      "must include only the TURN row when it is present without UTR or CRN" in {
        val detail = fiDetail.copy(
          IsFIUser = false,
          TINDetails = Some(Seq(TINDetails(TINType.TURN, "TURN123", "GB")))
        )

        val tinRows = util.getRows(detail, CRS).rows.drop(2)

        tinRows.map(
          row => row.key.content -> row.value.content
        ) mustBe Seq(
          HtmlContent(messages("CheckDetailsForFinancialInstitution.tinType.turn")) -> HtmlContent("TURN123")
        )
      }

      "must show the no-TIN fallback row for a non-user FI without TIN details" in {
        val result = util.getRows(fiDetail.copy(IsFIUser = false, GIIN = None, TINDetails = None), CRS)

        result.rows.map(
          row => row.key.content -> row.value.content
        ) mustBe Seq(
          HtmlContent(messages("CheckDetailsForFinancialInstitution.key.fiName")) -> HtmlContent(fiDetail.FIName),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.value.address")) -> HtmlContent(
            fiDetail.AddressDetails.formatAddressDetails
          ),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.key.noTin")) -> HtmlContent(
            messages("CheckDetailsForFinancialInstitution.value.noTin")
          )
        )
      }

      "must omit TIN rows for an FI user" in {
        val detail = fiDetail.copy(
          IsFIUser = true,
          TINDetails = Some(
            Seq(
              TINDetails(TINType.UTR, "1234567890", "GB"),
              TINDetails(TINType.CRN, "CRN123", "GB"),
              TINDetails(TINType.TURN, "TURN123", "GB")
            )
          )
        )

        val result = util.getRows(detail, CRS)

        result.rows.map(_.key.content) mustBe Seq(
          HtmlContent(messages("CheckDetailsForFinancialInstitution.key.fiName")),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.value.address"))
        )
      }

      "must include the GIIN row and omit TIN rows for FATCA" in {
        val detail = fiDetail.copy(
          GIIN = Some("GIIN-123"),
          TINDetails = Some(Seq(TINDetails(TINType.UTR, "1234567890", "GB")))
        )

        val result = util.getRows(detail, FATCA)

        result.rows.map(
          row => row.key.content -> row.value.content
        ) mustBe Seq(
          HtmlContent(messages("CheckDetailsForFinancialInstitution.key.fiName")) -> HtmlContent(detail.FIName),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.value.address")) -> HtmlContent(
            detail.AddressDetails.formatAddressDetails
          ),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.key.giin")) -> HtmlContent("GIIN-123")
        )
      }

      "must show the no-GIIN fallback row for FATCA when GIIN is absent" in {
        val result = util.getRows(fiDetail.copy(GIIN = None), FATCA)

        result.rows.map(
          row => row.key.content -> row.value.content
        ) mustBe Seq(
          HtmlContent(messages("CheckDetailsForFinancialInstitution.key.fiName")) -> HtmlContent(fiDetail.FIName),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.value.address")) -> HtmlContent(
            fiDetail.AddressDetails.formatAddressDetails
          ),
          HtmlContent(messages("CheckDetailsForFinancialInstitution.key.noGiin")) -> HtmlContent(
            messages("CheckDetailsForFinancialInstitution.value.noGiin")
          )
        )
      }
    }
  }
}
