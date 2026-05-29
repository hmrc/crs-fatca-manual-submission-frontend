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

class FatcaElectionsDetailsSpec extends SpecBase {

  private def details(
    hasThresholds: Option[YesNoNa] = None,
    hasTreasuryRegulations: Option[YesNoNa] = None
  ) = FatcaElectionsDetails(hasThresholds, hasTreasuryRegulations)

  "FatcaElectionsDetails.rows" - {

    "always returns 2 rows" in {
      FatcaElectionsDetails.rows(details()).length mustBe 2
    }

    "hasThresholds" - {

      "displays 'Not Provided' when None" in {
        val result = FatcaElectionsDetails.rows(details(hasThresholds = None))
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.fatca.hasThresholds"))
        row.value.value.content.asHtml.body mustBe "Not Provided"
      }

      "displays Yes when set to Yes" in {
        val result = FatcaElectionsDetails.rows(details(hasThresholds = Some(Yes)))
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.fatca.hasThresholds"))
        row.value.value.content.asHtml.body mustBe Yes.toString
      }

      "displays No when set to No" in {
        val result = FatcaElectionsDetails.rows(details(hasThresholds = Some(No)))
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.fatca.hasThresholds"))
        row.value.value.content.asHtml.body mustBe No.toString
      }

      "displays NA when set to NA" in {
        val result = FatcaElectionsDetails.rows(details(hasThresholds = Some(NA)))
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.fatca.hasThresholds"))
        row.value.value.content.asHtml.body mustBe NA.toString
      }
    }

    "hasTreasuryRegulations" - {

      "displays 'Not Provided' when None" in {
        val result = FatcaElectionsDetails.rows(details(hasTreasuryRegulations = None))
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.fatca.hasTreasuryRegulations"))
        row.value.value.content.asHtml.body mustBe "Not Provided"
      }

      "displays Yes when set to Yes" in {
        val result = FatcaElectionsDetails.rows(details(hasTreasuryRegulations = Some(Yes)))
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.fatca.hasTreasuryRegulations"))
        row.value.value.content.asHtml.body mustBe Yes.toString
      }

      "displays No when set to No" in {
        val result = FatcaElectionsDetails.rows(details(hasTreasuryRegulations = Some(No)))
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.fatca.hasTreasuryRegulations"))
        row.value.value.content.asHtml.body mustBe No.toString
      }

      "displays NA when set to NA" in {
        val result = FatcaElectionsDetails.rows(details(hasTreasuryRegulations = Some(NA)))
        val row    = result.find(_.key.content.asHtml.body == msgs("manageElections.fatca.hasTreasuryRegulations"))
        row.value.value.content.asHtml.body mustBe NA.toString
      }
    }
  }
}
