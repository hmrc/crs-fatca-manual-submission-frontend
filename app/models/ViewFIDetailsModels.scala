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

import play.api.libs.json.{Json, OFormat}

final case class ViewFIDetailsResponse(ViewFIDetails: ViewFIDetails)

object ViewFIDetailsResponse:
  given format: OFormat[ViewFIDetailsResponse] = Json.format[ViewFIDetailsResponse]

final case class ViewFIDetails(ResponseCommon: ResponseCommon, ResponseDetails: ResponseDetails)

object ViewFIDetails:
  given format: OFormat[ViewFIDetails] = Json.format[ViewFIDetails]

final case class ResponseCommon(
  OriginatingSystem: String,
  TransmittingSystem: String,
  RequestType: String, // should be 'VIEW' - not bothering with enums
  Regime: String,
  ResponseParameters: Option[List[ResponseParameter]]
)

object ResponseCommon:
  given format: OFormat[ResponseCommon] = Json.format[ResponseCommon]

final case class ResponseParameter(ParamName: String, ParamValue: String)

object ResponseParameter:
  given format: OFormat[ResponseParameter] = Json.format[ResponseParameter]

final case class ResponseDetails(FIDetails: Seq[FIDetail])

object ResponseDetails:
  given format: OFormat[ResponseDetails] = Json.format[ResponseDetails]

final case class FIDetail(
  FIID: String,
  FIName: String,
  SubscriptionID: String,
  TINDetails: Option[Seq[TINDetails]],
  GIIN: Option[String],
  IsFIUser: Boolean,
  AddressDetails: AddressDetails,
  PrimaryContactDetails: Option[ContactDetails],
  SecondaryContactDetails: Option[ContactDetails]
)

object FIDetail:
  given format: OFormat[FIDetail] = Json.format[FIDetail]

final case class ContactDetails(ContactName: String, EmailAddress: String, PhoneNumber: Option[String])

object ContactDetails:
  given format: OFormat[ContactDetails] = Json.format[ContactDetails]

final case class TINDetails(TINType: String, TIN: String, IssuedBy: String) // not bothering with TINType enums

object TINDetails:
  given format: OFormat[TINDetails] = Json.format[TINDetails]

final case class AddressDetails(
  AddressLine1: String,
  AddressLine2: Option[String],
  AddressLine3: Option[String],
  AddressLine4: Option[String],
  CountryCode: Option[String],
  PostalCode: Option[String]
)

object AddressDetails:
  given format: OFormat[AddressDetails] = Json.format[AddressDetails]
