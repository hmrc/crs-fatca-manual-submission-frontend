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
import models.manual.cpso.{ControllingPersonType, ControllingPersonTypes}
import models.viewModels.manual.cpso.CPSOId
import pages.manual.cpso.{ControllingPersonTypePage, CurrentCPSOIdPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.all.*

class ControllingPersonTypeSummarySpec extends SpecBase {

  private val cpsoId = CPSOId("cpso-id")

  ".row" - {

    implicit val messages: Messages = stubMessages()
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "testFIID")

    "must show every selected controlling person type and a Change action" in {
      val selectedTypes = Seq(
        ControllingPersonType.OwnershipOfLegalPerson,
        ControllingPersonType.TrusteeOfTrust
      )
      val ua = emptyUserAnswers
        .withPage(CurrentCPSOIdPage()(reportId), cpsoId)
        .withPage(ControllingPersonTypePage(cpsoId, reportId), ControllingPersonTypes(selectedTypes))

      ControllingPersonTypeSummary.row(ua).value mustBe SummaryListRowViewModel(
        key = "cpso.controllingPersonType.checkYourAnswersLabel",
        value = ValueViewModel(
          HtmlContent(
            "cpso.controllingPersonType.ownershipOfLegalPerson<br>cpso.controllingPersonType.trusteeOfTrust"
          )
        ),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.manual.cpso.routes.ControllingPersonTypeController.onPageLoad(CheckMode).url
          ).withVisuallyHiddenText(messages("cpso.controllingPersonType.change.hidden"))
        )
      )
    }

    "must not create a summary when the answer is missing" in {
      val ua = emptyUserAnswers.withPage(CurrentCPSOIdPage()(reportId), cpsoId)

      ControllingPersonTypeSummary.row(ua) mustBe empty
    }
  }
}
