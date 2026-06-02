/*
 * Copyright 2025 HM Revenue & Customs
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

package models.requests

import play.api.libs.json.{Json, OFormat}

case class CrsElectionsDetails(hasCARF: Option[Boolean], hasContracts: Option[Boolean], hasDormantAccounts: Option[Boolean], hasThresholds: Option[Boolean])

object CrsElectionsDetails:
  given OFormat[CrsElectionsDetails] = Json.format[CrsElectionsDetails]

case class FatcaElectionsDetails(hasThresholds: Option[Boolean], hasTreasuryRegulations: Option[Boolean])

object FatcaElectionsDetails:
  given OFormat[FatcaElectionsDetails] = Json.format[FatcaElectionsDetails]

case class ElectionsSubmissionDetails(
  fiId: String,
  reportingPeriod: String,
  crsDetails: Option[CrsElectionsDetails],
  fatcaDetails: Option[FatcaElectionsDetails]
)

object ElectionsSubmissionDetails:
  given OFormat[ElectionsSubmissionDetails] = Json.format[ElectionsSubmissionDetails]
