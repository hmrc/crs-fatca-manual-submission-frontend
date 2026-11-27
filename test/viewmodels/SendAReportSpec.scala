/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels

import base.SpecBase
import models.SubmissionsConstants.{CRS, FATCA}
import models.viewModels.*
import models.viewModels.TaskStatus.{CannotStartYet, Completed, Incomplete, NotStarted}
import models.{NormalMode, ReportId}
import pages.manual.accountHolders.AccountHoldersPage
import pages.manual.sponsor.HaveSponsorPage

class SendAReportSpec extends SpecBase {

  "SendAReportSections" - {

    "must return the first account-holder page when existing holders have no name" in {
      val reportId = ReportId(CRS, 2025, None, "TestFIID")

      val sendAReport = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = None,
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        userAnswers = emptyUserAnswers.withPage(
          AccountHoldersPage()(reportId),
          AccountHolders(
            currentAccountHolderId = None,
            accountHolders = Map(
              "1" -> AccountHolder(individualOrOrganisation = None)
            )
          )
        ),
        reportId = reportId
      )

      sendAReport.sections must contain(
        SendAReportSection(
          "sendAReport.accountsInformation.heading",
          "accounts-information",
          List(
            SendAReportTask(
              "sendAReport.accountsInformation.accountHolders",
              NotStarted,
              Some(controllers.manual.accountHolders.routes.AccountController.onPageLoad(NormalMode).url)
            )
          )
        )
      )
    }

    "must return the first account-holder page when no holders are present" in {
      val reportId = ReportId(CRS, 2025, None, "TestFIID")

      val sendAReport = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = None,
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        userAnswers = emptyUserAnswers,
        reportId = reportId
      )

      sendAReport.sections must contain(
        SendAReportSection(
          "sendAReport.accountsInformation.heading",
          "accounts-information",
          List(
            SendAReportTask(
              "sendAReport.accountsInformation.accountHolders",
              NotStarted,
              Some(controllers.manual.accountHolders.routes.AccountController.onPageLoad(NormalMode).url)
            )
          )
        )
      )
    }

    "must include the incomplete filer section for FATCA" in {
      val reportId = ReportId(FATCA, 2025, None, "TestFIID")

      val sendAReport = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = Some(Incomplete),
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        userAnswers = emptyUserAnswers.withPage(HaveSponsorPage()(reportId), true),
        reportId = reportId
      )

      sendAReport.sections must contain(
        SendAReportSection(
          "sendAReport.filerCategory.heading",
          "filer-category",
          List(
            SendAReportTask(
              "sendAReport.filerCategory.subHeading",
              Incomplete,
              Some(
                controllers.manual.filercategory.routes.WhatTypeOfFilerIsSponsorController
                  .onPageLoad(NormalMode)
                  .url
              )
            )
          )
        )
      )
    }

    "must link a completed FATCA filer section to check answers" in {
      val reportId = ReportId(FATCA, 2025, None, "TestFIID")

      val sendAReport = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = Some(Completed),
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        userAnswers = emptyUserAnswers.withPage(HaveSponsorPage()(reportId), true),
        reportId = reportId
      )

      sendAReport.sections must contain(
        SendAReportSection(
          "sendAReport.filerCategory.heading",
          "filer-category",
          List(
            SendAReportTask(
              "sendAReport.filerCategory.subHeading",
              Completed,
              Some(
                controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad().url
              )
            )
          )
        )
      )
    }

    "must show CannotStartYet without a link for the locked FATCA filer section" in {
      val reportId = ReportId(FATCA, 2025, None, "TestFIID")

      val sendAReport = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = Some(CannotStartYet),
        accounts = None,
        accountHolders = None,
        controllingPersons = None,
        userAnswers = emptyUserAnswers,
        reportId = reportId
      )

      sendAReport.sections must contain(
        SendAReportSection(
          "sendAReport.filerCategory.heading",
          "filer-category",
          List(
            SendAReportTask("sendAReport.filerCategory.subHeading", CannotStartYet, None)
          )
        )
      )
    }

    "must not include an empty filer section for CRS" in {
      val reportId = ReportId(CRS, 2025, None, "TestFIID")

      val sendAReport = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = None,
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        userAnswers = emptyUserAnswers,
        reportId = reportId
      )

      sendAReport.sections mustNot contain(
        SendAReportSection(
          "sendAReport.filerCategory.heading",
          "filer-category",
          List()
        )
      )
    }

    "must include the incomplete sponsor section for FATCA" in {
      val reportId = ReportId(FATCA, 2025, None, "TestFIID")

      val sendAReport = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = Some(Incomplete),
        filerCategory = None,
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        userAnswers = emptyUserAnswers,
        reportId = reportId
      )

      sendAReport.sections must contain(
        SendAReportSection(
          "sendAReport.sponsor.heading",
          "sponsor",
          List(
            SendAReportTask(
              "sendAReport.sponsor.subHeading",
              Incomplete,
              Some(controllers.manual.sponsor.routes.HaveSponsorController.onPageLoad(NormalMode).url)
            )
          )
        )
      )
    }

    "must link a completed FATCA sponsor section to check answers" in {
      val reportId = ReportId(FATCA, 2025, None, "TestFIID")

      val sendAReport = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = Some(Completed),
        filerCategory = None,
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        userAnswers = emptyUserAnswers,
        reportId = reportId
      )

      sendAReport.sections must contain(
        SendAReportSection(
          "sendAReport.sponsor.heading",
          "sponsor",
          List(
            SendAReportTask(
              "sendAReport.sponsor.subHeading",
              Completed,
              Some(controllers.manual.sponsor.routes.CheckAnswersController.onPageLoad().url)
            )
          )
        )
      )
    }

    "must not include an empty sponsor section for CRS" in {
      val reportId = ReportId(CRS, 2025, None, "TestFIID")

      val sendAReport = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = None,
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        userAnswers = emptyUserAnswers,
        reportId = reportId
      )

      sendAReport.sections mustNot contain(
        SendAReportSection(
          "sendAReport.sponsor.heading",
          "sponsor",
          List()
        )
      )
    }

    "must enable sending only when every displayed prerequisite is completed or not required" in {
      val ready = SendAReportSections(
        financialInstitutionDetails = Some(Completed),
        sponsorDetails = None,
        filerCategory = None,
        accounts = Some(Completed),
        accountHolders = Some(Completed),
        controllingPersons = Some(TaskStatus.NotRequired),
        userAnswers = emptyUserAnswers,
        reportId = ReportId(CRS, 2026, None, "fi")
      )

      ready.sections.last.tasks.head.status mustBe Incomplete
      ready.sections.last.tasks.head.href mustBe
        Some(controllers.routes.UnderConstructionController.onPageLoad().url)

      ready.sections
        .flatMap(_.tasks)
        .find(_.status == TaskStatus.NotRequired)
        .value
        .href mustBe None

      Seq(Incomplete, CannotStartYet, NotStarted).foreach {
        status =>
          val blocked = ready.copy(accountHolders = Some(status))

          blocked.sections.last.tasks.head.status mustBe CannotStartYet
          blocked.sections.last.tasks.head.href mustBe None
      }
    }

    "must not offer an account-holder link while it cannot start" in {
      val model = SendAReportSections(
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = None,
        accounts = Some(Incomplete),
        accountHolders = Some(CannotStartYet),
        controllingPersons = None,
        userAnswers = emptyUserAnswers,
        reportId = ReportId(CRS, 2026, None, "fi")
      )

      model.sections
        .flatMap(_.tasks)
        .find(_.titleKey == "sendAReport.accountsInformation.accountHolders")
        .value
        .href mustBe None
    }
  }
}
