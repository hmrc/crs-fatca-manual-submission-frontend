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
import models.{NormalMode, ReportId}
import models.SubmissionsConstants.CRS
import models.viewModels.TaskStatus.NotStarted
import models.viewModels.*
import pages.manual.accountHolders.AccountHoldersPage

class SendAReportSpec extends SpecBase {

  "SendAReportSections" - {

    "must return UnderConstruction Page Url for AccountHolderNavigation When AccountHolder already present" in {

      val reportId = ReportId(
        CRS,
        2025,
        None,
        "TestFIID"
      )
      val sendAReport = SendAReportSections(
        reportDetails = None,
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = None,
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        tbc1 = None,
        tbc2 = None,
        userAnswers = emptyUserAnswers
          .withPage(
            AccountHoldersPage()(reportId),
            AccountHolders(
              currentAccountHolderId = None,
              accountHolders = Map.apply(
                "1" ->
                  AccountHolder(
                    individualOrOrganisation = None
                  )
              )
            )
          ),
        reportId = reportId
      )

      sendAReport.sections mustBe Seq(
        SendAReportSection(
          "sendAReport.accountsInformation.heading",
          "accounts-information",
          List(
            SendAReportTask("sendAReport.accountsInformation.accountHolders", NotStarted, Some(controllers.routes.UnderConstructionController.onPageLoad().url))
          )
        )
      )

    }

    "must return Accounts Page Url for AccountHolderNavigation When AccountHolder already present" in {

      val reportId = ReportId(
        CRS,
        2025,
        None,
        "TestFIID"
      )
      val sendAReport = SendAReportSections(
        reportDetails = None,
        financialInstitutionDetails = None,
        sponsorDetails = None,
        filerCategory = None,
        accounts = None,
        accountHolders = Some(NotStarted),
        controllingPersons = None,
        tbc1 = None,
        tbc2 = None,
        userAnswers = emptyUserAnswers,
        reportId = reportId
      )

      sendAReport.sections mustBe Seq(
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

  }
}
