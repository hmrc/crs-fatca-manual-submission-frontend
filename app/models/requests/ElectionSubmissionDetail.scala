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

case class CrsElectionsRequest(hasCARF: Option[Boolean], hasContracts: Boolean, hasDormantAccounts: Boolean, hasThresholds: Boolean)

object CrsElectionsRequest:
  given OFormat[CrsElectionsRequest] = Json.format[CrsElectionsRequest]

case class FatcaElectionsRequest(hasThresholds: Boolean, hasTreasuryRegulations: Boolean)

object FatcaElectionsRequest:
  given OFormat[FatcaElectionsRequest] = Json.format[FatcaElectionsRequest]

case class ElectionsSubmissionRequest(
  fiId: String,
  reportingPeriod: String,
  crsDetails: Option[CrsElectionsRequest],
  fatcaDetails: Option[FatcaElectionsRequest]
)

object ElectionsSubmissionRequest:
  given OFormat[ElectionsSubmissionRequest] = Json.format[ElectionsSubmissionRequest]
