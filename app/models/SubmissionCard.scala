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

import models.SubmissionsConstants.{FATCA3, SubmissionFileType, SubmissionType}
import play.twirl.api.Html
import utils.DateTimeFormats.formatTimeSent

import java.time.LocalDateTime

case class SubmissionCard(isVoided: Option[Boolean],
                          messageRefId: String,
                          originalMessageRefId: String,
                          timeSent: LocalDateTime,
                          fileType: SubmissionFileType,
                          submissionType: SubmissionType
) {
  val title: String = (if fileType.value.contains("CRS") then "CRS" else "FATCA") + " " + submissionType.value

  val summaryKey: String                = fileType.cardSummaryKey
  private val formattedDateTime: String = timeSent.formatTimeSent
  val summaryValue: Html                = if fileType != FATCA3 then Html(s"MessageRefId: $messageRefId <br> $formattedDateTime") else Html(formattedDateTime)
}
