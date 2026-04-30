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

import models.SubmissionsConstants.{CRS701, CRS702, CRS703, CRSAdditional701, FATCA1, FATCA2, FATCA3, FATCA4, SubmissionFileType, SubmissionType}
import play.twirl.api.Html
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class SubmissionCard(isVoided: Option[Boolean],
                          messageRefId: String,
                          originalMessageRefId: String,
                          timeSent: LocalDateTime,
                          fileType: SubmissionFileType,
                          submissionType: SubmissionType
) {
  val title: String = (if fileType.value.contains("CRS") then "CRS" else "FATCA") + " " + submissionType.value

  val summaryKey: String = fileType match {
    case FATCA1 | CRS701  => "New information"
    case FATCA2           => "Corrected information for an existing report"
    case CRS702           => "Corrected or deleted information for an existing report"
    case FATCA4           => "Amended information for an existing report"
    case CRS703           => "No information to report"
    case FATCA3           => "Date voided"
    case CRSAdditional701 => "Additional information for an existing report"
  }
  private val formattedDateTime: String = timeSent.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma"))
  val summaryValue: Html                = if fileType != FATCA3 then Html(s"MessageRefId: $messageRefId <br> $formattedDateTime") else Html(formattedDateTime)
}
