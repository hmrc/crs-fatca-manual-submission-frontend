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

import play.api.libs.json.*

object SubmissionsConstants:

  sealed trait RegimeType:
    def value: String
  case object CRS extends RegimeType:
    def value = "CRS"
  case object FATCA extends RegimeType:
    def value = "FATCA"
  case object CRFA extends RegimeType:
    def value = "CRFA"

  sealed trait SubmissionStatus:
    def value: String
  case object PENDING extends SubmissionStatus:
    def value = "PENDING"
  case object FAILED extends SubmissionStatus:
    def value = "FAILED"
  case object PASSED extends SubmissionStatus:
    def value = "PASSED"

  sealed trait SubmissionType:
    def value: String
  case object XML extends SubmissionType:
    def value = "XML"
  case object MANUAL extends SubmissionType:
    def value = "MANUAL"

  sealed trait SubmissionFileType:
    def value: String
  case object FATCA1 extends SubmissionFileType:
    def value = "FATCA1"
  case object FATCA2 extends SubmissionFileType:
    def value = "FATCA2"
  case object FATCA4 extends SubmissionFileType:
    def value = "FATCA4"
  case object CRS701 extends SubmissionFileType:
    def value = "CRS701"
  case object CRS702 extends SubmissionFileType:
    def value = "CRS702"
  case object CRS703 extends SubmissionFileType:
    def value = "CRS703"

  implicit val regimeTypeFormat: Format[RegimeType] = new Format[RegimeType]:
    def reads(json: JsValue): JsResult[RegimeType] = json match
      case JsString(s) => s.toUpperCase match
        case "CRS" => JsSuccess(CRS)
        case "FATCA" => JsSuccess(FATCA)
        case "CRFA" => JsSuccess(CRFA)
        case _ => JsError("Invalid regime type")
      case _ => JsError("Expected JsString")

    def writes(regime: RegimeType): JsValue = JsString(regime.value)

  implicit val submissionStatusFormat: Format[SubmissionStatus] = new Format[SubmissionStatus]:
    def reads(json: JsValue): JsResult[SubmissionStatus] = json match
      case JsString(s) => s.toUpperCase match
        case "PENDING" => JsSuccess(PENDING)
        case "FAILED" => JsSuccess(FAILED)
        case "PASSED" => JsSuccess(PASSED)
        case _ => JsError("Invalid submission status")
      case _ => JsError("Expected JsString")

    def writes(status: SubmissionStatus): JsValue = JsString(status.value)

  implicit val submissionTypeFormat: Format[SubmissionType] = new Format[SubmissionType]:
    def reads(json: JsValue): JsResult[SubmissionType] = json match
      case JsString(s) => s.toUpperCase match
        case "XML" => JsSuccess(XML)
        case "MANUAL" => JsSuccess(MANUAL)
        case _ => JsError("Invalid submission type")
      case _ => JsError("Expected JsString")

    def writes(subType: SubmissionType): JsValue = JsString(subType.value)

  implicit val submissionFileTypeFormat: Format[SubmissionFileType] = new Format[SubmissionFileType]:
    def reads(json: JsValue): JsResult[SubmissionFileType] = json match
      case JsString(s) => s.toUpperCase match
        case "FATCA1" => JsSuccess(FATCA1)
        case "FATCA2" => JsSuccess(FATCA2)
        case "FATCA4" => JsSuccess(FATCA4)
        case "CRS701" => JsSuccess(CRS701)
        case "CRS702" => JsSuccess(CRS702)
        case "CRS703" => JsSuccess(CRS703)
        case _ => JsError("Invalid submission file type")
      case _ => JsError("Expected JsString")

    def writes(fileType: SubmissionFileType): JsValue = JsString(fileType.value)

