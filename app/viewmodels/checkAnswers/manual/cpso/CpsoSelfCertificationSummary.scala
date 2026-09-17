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

/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.checkAnswers.manual.cpso

import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.cpso.{CpsoSelfCertificationPage, CurrentCPSOIdPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CpsoSelfCertificationSummary {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    for {
      currentId <- answers.get(CurrentCPSOIdPage()(reportId))
      answer    <- answers.get(CpsoSelfCertificationPage(currentId, reportId))
    } yield {

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"cpso.selfCertification.$answer"))
        )
      )

      SummaryListRowViewModel(
        key = "cpso.selfCertification.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", controllers.manual.cpso.routes.CpsoSelfCertificationController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("cpsoSelfCertification.change.hidden"))
        )
      )
    }
}
