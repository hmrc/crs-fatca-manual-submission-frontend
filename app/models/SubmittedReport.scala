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

import models.SubmissionsConstants.{RegimeType, SubmissionFileType, SubmissionStatus, SubmissionType}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class SubmittedReport(
  fiId: String,
  fiName: String,
  fileName: String,
  submissionStatus: SubmissionStatus,
  uploadDateTime: LocalDateTime,
  regime: RegimeType,
  reportingYear: String,
  submissionCaseId: String,
  submissionType: SubmissionType,
  submissionFileType: SubmissionFileType,
  messageRefId: String,
  submissionDeleteStatus: Option[Boolean] = None,
  originalMessageRefId: Option[String] = None
)

object SubmittedReport {
  implicit val format: OFormat[SubmittedReport] = Json.format[SubmittedReport]
}
