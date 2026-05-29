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

package models.elections

import base.SpecBase
import models.elections.YesNoNa.*
import models.elections.{CrsElectionsDetails, YesNoNa}

class CrsElectionsDetailsSpec extends SpecBase {

  private val year2025 = 2025
  private val year2026 = 2026

  private def details(
    hasCARF: Option[YesNoNa] = None,
    hasContracts: Option[YesNoNa] = None,
    hasDormantAccounts: Option[YesNoNa] = None,
    hasThresholds: Option[YesNoNa] = None
  ) = CrsElectionsDetails(hasCARF, hasContracts, hasDormantAccounts, hasThresholds)

  "CrsElectionsDetails.rows" - {

    "always includes the 3 base rows" - {

      "for hasContracts, hasDormantAccounts and hasThresholds" in {
        val result = CrsElectionsDetails.rows(details(), year2025)
        result.map(_.key.content.asHtml.body) must contain allOf (
          msgs("manageElections.crs.hasContracts"),
          msgs("manageElections.crs.hasDormantAccounts"),
          msgs("manageElections.crs.hasThresholds")
        )
      }

      "showing 'Not Provided' when values are None" in {
        val result = CrsElectionsDetails.rows(details(), year2025)
        result.count(_.value.content.asHtml.body == "Not Provided") mustBe 3
      }

      "showing the value when set" in {
        val result = CrsElectionsDetails.rows(
          details(hasContracts = Some(Yes), hasDormantAccounts = Some(No), hasThresholds = Some(NA)),
          year2025
        )
        val values = result.map(_.value.content.asHtml.body)
        values must contain(Yes.toString)
        values must contain(No.toString)
        values must contain(NA.toString)
      }
    }

    "maybeCarfRow" - {

      "is included when selectedYear is greater than 2025" in {
        val result = CrsElectionsDetails.rows(details(hasCARF = Some(Yes)), year2026)
        result.map(_.key.content.asHtml.body) must contain(
          msgs("manageElections.crs.hasCARF", year2026.toString)
        )
      }

      "is not included when selectedYear is 2025" in {
        val result = CrsElectionsDetails.rows(details(hasCARF = Some(Yes)), year2025)
        result.map(_.key.content.asHtml.body) must not contain
          msgs("manageElections.crs.hasCARF", year2025.toString)
      }

      "is not included when selectedYear is less than 2025" in {
        val result = CrsElectionsDetails.rows(details(hasCARF = Some(Yes)), 2024)
        result.map(_.key.content.asHtml.body) must not contain
          msgs("manageElections.crs.hasCARF", "2024")
      }

      "displays 'Not Provided' when hasCARF is None" in {
        val result  = CrsElectionsDetails.rows(details(hasCARF = None), year2026)
        val carfRow = result.find(_.key.content.asHtml.body == msgs("manageElections.crs.hasCARF", year2026.toString))
        carfRow.value.value.content.asHtml.body mustBe "Not Provided"
      }
    }

    "maybeGrossProceedsRow" - {

      "is included when hasCARF is Yes" in {
        val result = CrsElectionsDetails.rows(details(hasCARF = Some(Yes)), year2025)
        result.map(_.key.content.asHtml.body) must contain(
          msgs("manageElections.crs.grossProceeds", year2025.toString)
        )
      }

      "is included when hasCARF is No" in {
        val result = CrsElectionsDetails.rows(details(hasCARF = Some(No)), year2025)
        result.map(_.key.content.asHtml.body) must contain(
          msgs("manageElections.crs.grossProceeds", year2025.toString)
        )
      }

      "is not included when hasCARF is NA" in {
        val result = CrsElectionsDetails.rows(details(hasCARF = Some(NA)), year2025)
        result.map(_.key.content.asHtml.body) must not contain
          msgs("manageElections.crs.grossProceeds", year2025.toString)
      }

      "is not included when hasCARF is None" in {
        val result = CrsElectionsDetails.rows(details(hasCARF = None), year2025)
        result.map(_.key.content.asHtml.body) must not contain
          msgs("manageElections.crs.grossProceeds", year2025.toString)
      }

      "displays Yes when hasCARF is Yes" in {
        val result = CrsElectionsDetails.rows(details(hasCARF = Some(Yes)), year2025)
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.crs.grossProceeds", year2025.toString))
        row.value.value.content.asHtml.body mustBe Yes.toString
      }

      "displays No when hasCARF is No" in {
        val result = CrsElectionsDetails.rows(details(hasCARF = Some(No)), year2025)
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.crs.grossProceeds", year2025.toString))
        row.value.value.content.asHtml.body mustBe No.toString
      }
    }

    "total row count" - {

      "is 3 when year <= 2025 and hasCARF is None" in {
        CrsElectionsDetails.rows(details(), year2025).length mustBe 3
      }

      "is 4 when year <= 2025 and hasCARF is Yes or No" in {
        CrsElectionsDetails.rows(details(hasCARF = Some(Yes)), year2025).length mustBe 4
      }

      "is 4 when year > 2025 and hasCARF is None" in {
        CrsElectionsDetails.rows(details(), year2026).length mustBe 4
      }

      "is 5 when year > 2025 and hasCARF is Yes or No" in {
        CrsElectionsDetails.rows(details(hasCARF = Some(Yes)), year2026).length mustBe 5
      }
    }
  }
}
