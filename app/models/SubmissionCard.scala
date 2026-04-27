package models

import models.SubmissionsConstants.SubmissionFileType

import java.time.LocalDate

case class SubmissionCard(isVoided: Option[Boolean], messageRefId: String, originalMessageRefId: String, timeSent: LocalDate, messageType:SubmissionFileType)
