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

package viewmodels.checkAnswers

import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.cpso.CpsoOrganisationNamePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CpsoOrganisationNameSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    answers.get(CpsoOrganisationNamePage()).map {
      answer =>

      val value = HtmlFormat.escape(answer.organizationName).toString + "<br/>"

        SummaryListRowViewModel(
          key     = "cpsoOrganisationName.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.manual.cpso.routes.CpsoOrganisationNameController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("cpsoOrganisationName.change.hidden"))
          )
        )
    }
}
