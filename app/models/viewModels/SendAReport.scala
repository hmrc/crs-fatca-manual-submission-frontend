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

package models.viewModels

import models.NormalMode

sealed trait TaskStatus

object TaskStatus {
  case object NotStarted extends TaskStatus
  case object Completed extends TaskStatus
  case object Incomplete extends TaskStatus
  case object NoStatus extends TaskStatus
}

final case class SendAReportSections(
  reportDetails: Option[TaskStatus],
  financialInstitutionDetails: Option[TaskStatus],
  sponsorDetails: Option[TaskStatus],
  filerCategory: Option[TaskStatus],
  accounts: Option[TaskStatus],
  accountHolders: Option[TaskStatus],
  controllingPersons: Option[TaskStatus],
  tbc1: Option[TaskStatus],
  tbc2: Option[TaskStatus]
) {

  private def task(titleKey: String, status: Option[TaskStatus], href: Option[String]): Seq[SendAReportTask] =
    status.map(SendAReportTask(titleKey, _, href)).toSeq

  val sections: Seq[SendAReportSection] =
    Seq(
      SendAReportSection(
        headingKey = "sendAReport.reportDetails.heading",
        idPrefix = "report-details",
        tasks = task("sendAReport.reportDetails.subHeading", reportDetails, href = Some(controllers.routes.UnderConstructionController.onPageLoad().url))
      ),
      SendAReportSection(
        headingKey = "sendAReport.financialInstitution.heading",
        idPrefix = "financial-institution",
        tasks = task("sendAReport.financialInstitution.subHeading",
                     financialInstitutionDetails,
                     href = Some(controllers.routes.UnderConstructionController.onPageLoad().url)
        )
      ),
      SendAReportSection(
        headingKey = "sendAReport.sponsor.heading",
        idPrefix = "sponsor",
        tasks = task("sendAReport.sponsor.subHeading",
                     sponsorDetails,
                     href = Some(controllers.manual.sponsor.routes.HaveSponsorController.onPageLoad(NormalMode).url)
        )
      ),
      SendAReportSection(
        headingKey = "sendAReport.filerCategory.heading",
        idPrefix = "filer-category",
        tasks = task(
          "sendAReport.filerCategory.subHeading",
          filerCategory,
          href = Some(controllers.manual.filercategory.routes.WhatTypeOfFilerIsSponsorController.onPageLoad(NormalMode).url)
        )
      ),
      SendAReportSection(
        headingKey = "sendAReport.accountsInformation.heading",
        idPrefix = "accounts-information",
        tasks = Seq(
          task("sendAReport.accountsInformation.accounts",
               accounts,
               href = Some(controllers.manual.account.routes.HaveNumberController.onPageLoad(NormalMode).url)
          ),
          task("sendAReport.accountsInformation.accountHolders", accountHolders, href = Some(controllers.routes.UnderConstructionController.onPageLoad().url)),
          task("sendAReport.accountsInformation.controllingPersons",
               controllingPersons,
               href = Some(controllers.routes.UnderConstructionController.onPageLoad().url)
          ),
          task("sendAReport.accountsInformation.tbc1", tbc1, href = Some(controllers.routes.UnderConstructionController.onPageLoad().url)),
          task("sendAReport.accountsInformation.tbc2", tbc2, href = Some(controllers.routes.UnderConstructionController.onPageLoad().url))
        ).flatten
      )
    ).filter(_.tasks.nonEmpty)
}

final case class SendAReportSection(
  headingKey: String,
  idPrefix: String,
  tasks: Seq[SendAReportTask]
)

final case class SendAReportTask(
  titleKey: String,
  status: TaskStatus,
  href: Option[String]
)
