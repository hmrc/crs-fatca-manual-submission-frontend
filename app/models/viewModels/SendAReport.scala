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

import models.SubmissionsConstants.{CRS, FATCA}
import models.{NormalMode, ReportId, UserAnswers}
import pages.manual.account.AccountsPage
import pages.manual.accountHolders.AccountHoldersPage
import pages.manual.sponsor.HaveSponsorPage

sealed trait TaskStatus

object TaskStatus {
  case object NotStarted extends TaskStatus
  case object Completed extends TaskStatus
  case object Incomplete extends TaskStatus
  case object NoStatus extends TaskStatus
  case object NotRequired extends TaskStatus
  case object CannotStartYet extends TaskStatus
}

def handleAccountHolderNavigation(userAnswers: UserAnswers, reportId: ReportId) =
  userAnswers.get(AccountHoldersPage()(reportId)) match {
    case Some(accountHolderList) =>
      if (accountHolderList.accountHolders.valuesIterator.exists(_.getName().nonEmpty))
        Some(controllers.manual.accountHolders.routes.AccountHolderListController.onPageLoad(NormalMode).url)
      else
        Some(controllers.manual.accountHolders.routes.AccountController.onPageLoad(NormalMode).url)

    case None =>
      Some(controllers.manual.accountHolders.routes.AccountController.onPageLoad(NormalMode).url)
  }

def handleAccountNavigation(
  userAnswers: UserAnswers,
  reportId: ReportId
): Option[String] = {
  val hasAccounts = userAnswers
    .get(AccountsPage(reportId))
    .exists(_.accounts.nonEmpty)

  val destination =
    if (hasAccounts) {
      controllers.manual.account.routes.AccountsController.onPageLoad()
    } else {
      controllers.manual.account.routes.HaveNumberController.onPageLoad(NormalMode)
    }

  Some(destination.url)
}

final case class SendAReportSections(
  financialInstitutionDetails: Option[TaskStatus],
  sponsorDetails: Option[TaskStatus],
  filerCategory: Option[TaskStatus],
  accounts: Option[TaskStatus],
  accountHolders: Option[TaskStatus],
  controllingPersons: Option[TaskStatus],
  userAnswers: UserAnswers,
  reportId: ReportId,
  isNilReport: Boolean = false,
  fiName: String = ""
) {

  private def task(
    titleKey: String,
    status: Option[TaskStatus],
    href: Option[String]
  ): Seq[SendAReportTask] =
    status.map {
      value =>
        SendAReportTask(
          titleKey,
          value,
          if (value == TaskStatus.CannotStartYet || value == TaskStatus.NotRequired) None
          else href
        )
    }.toSeq

  private val fillerSection = {
    val redirectionLink = filerCategory match {
      case Some(TaskStatus.Completed) =>
        Some(controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad().url)

      case _ =>
        userAnswers
          .get(HaveSponsorPage()(reportId))
          .fold(None)(
            _ =>
              Some(
                controllers.manual.filercategory.routes.WhatTypeOfFilerIsSponsorController.onPageLoad(NormalMode).url
              )
          )
    }

    SendAReportSection(
      headingKey = "sendAReport.filerCategory.heading",
      idPrefix = "filer-category",
      tasks = task(
        "sendAReport.filerCategory.subHeading",
        filerCategory,
        href = redirectionLink
      )
    )
  }

  private val sponsorSection = {
    val redirectionLink = sponsorDetails match {
      case Some(TaskStatus.Completed) =>
        Some(controllers.manual.sponsor.routes.CheckAnswersController.onPageLoad().url)

      case _ =>
        Some(controllers.manual.sponsor.routes.HaveSponsorController.onPageLoad(NormalMode).url)
    }

    SendAReportSection(
      headingKey = "sendAReport.sponsor.heading",
      idPrefix = "sponsor",
      tasks = task(
        "sendAReport.sponsor.subHeading",
        sponsorDetails,
        href = redirectionLink
      )
    )
  }

  private val detailSections: Seq[SendAReportSection] =
    Seq(
      SendAReportSection(
        headingKey = "sendAReport.financialInstitution.heading",
        idPrefix = "financial-institution",
        tasks = task(
          "sendAReport.financialInstitution.subHeading",
          financialInstitutionDetails,
          href = Some(
            controllers.manual.reportingFI.routes.CheckDetailsForFinancialInstitutionController.onPageLoad().url
          )
        )
      )
    ) ++
      (if (reportId.regime == FATCA) Seq(sponsorSection, fillerSection) else Seq.empty) ++
      Seq(
        SendAReportSection(
          headingKey = "sendAReport.accountsInformation.heading",
          idPrefix = "accounts-information",
          tasks = Seq(
            task(
              "sendAReport.accountsInformation.accounts",
              accounts,
              href = handleAccountNavigation(userAnswers, reportId)
            ),
            task(
              "sendAReport.accountsInformation.accountHolders",
              accountHolders,
              href = handleAccountHolderNavigation(userAnswers, reportId)
            ),
            task(
              controllingPersonsOrSubstantialOwnerMessageKey,
              controllingPersons,
              href = Some(
                controllers.manual.cpso.routes.CheckCPSOAddedOrAllowedController.onChangeRedirect().url
              )
            )
          ).flatten
        )
      )

  val sections: Seq[SendAReportSection] =
    detailSections.filter(_.tasks.nonEmpty) :+ SendAReportSection(
      if (isNilReport) "sendAReport.sendNil.heading" else "sendAReport.send.heading",
      "send-report",
      task(
        if (isNilReport) "sendAReport.sendNil.task" else "sendAReport.send.task",
        Some(
          if (
            detailSections.flatMap(_.tasks).forall {
              task =>
                task.status == TaskStatus.Completed || task.status == TaskStatus.NotRequired
            }
          ) TaskStatus.Incomplete
          else TaskStatus.CannotStartYet
        ),
        Some(controllers.routes.UnderConstructionController.onPageLoad().url)
      )
    )

  private def controllingPersonsOrSubstantialOwnerMessageKey =
    if reportId.regime == CRS then "sendAReport.accountsInformation.controllingPersons"
    else "sendAReport.accountsInformation.substantialOwner"
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
