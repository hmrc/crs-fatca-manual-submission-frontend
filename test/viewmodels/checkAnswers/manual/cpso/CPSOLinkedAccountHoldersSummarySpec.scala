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

package viewmodels.checkAnswers.manual.cpso

import base.SpecBase
import models.{CheckMode, ReportId}
import models.SubmissionsConstants.CRS
import models.viewModels.{AccountHolder, AccountHolderId, AccountHolders}
import models.viewModels.manual.cpso.CPSOId
import pages.manual.accountHolders.AccountHoldersPage
import pages.manual.cpso.{AccountHolderIdsPage, CurrentCPSOIdPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.all.*

class CPSOLinkedAccountHoldersSummarySpec extends SpecBase {

  private val cpsoId    = CPSOId("cpso-id")
  private val holderId1 = AccountHolderId("holder-1")
  private val holderId2 = AccountHolderId("holder-2")

  ".row" - {

    implicit val messages: Messages = stubMessages()
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "testFIID")

    "must show selected account holder names in deterministic order with a Change action" in {
      val regime = reportId.regime.toString.toLowerCase
      val ua = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(AccountHolderIdsPage(cpsoId, reportId), Set(holderId2, holderId1))
        .withPage(
          AccountHoldersPage()(reportId),
          AccountHolders(
            None,
            Map(
              holderId1.value -> AccountHolder(organisationName = Some("Zeta Ltd")),
              holderId2.value -> AccountHolder(organisationName = Some("Alpha & Sons"))
            )
          )
        )

      val row = CPSOLinkedAccountHoldersSummary.row(ua).value

      row mustBe SummaryListRowViewModel(
        key = s"cpso.accountHolders.checkYourAnswersLabel.$regime",
        value = ValueViewModel(HtmlContent("Alpha &amp; Sons<br>Zeta Ltd")),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.manual.cpso.routes.AccountHolderController.onPageLoad(CheckMode).url
          ).withVisuallyHiddenText(messages(s"cpso.accountHolders.change.hidden.$regime"))
        )
      )
    }

    "must not create a summary when there is no linked account holder data" in {
      val ua = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(AccountHolderIdsPage(cpsoId, reportId), Set(holderId1))

      CPSOLinkedAccountHoldersSummary.row(ua) mustBe empty
    }
  }
}
