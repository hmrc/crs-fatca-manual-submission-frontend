package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, ReportId, UserAnswers}
import pages.manual.WhatTypeOfFilerPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WhatTypeOfFilerSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    answers.get(WhatTypeOfFilerPage()).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"whatTypeOfFiler.$answer"))
          )
        )

        SummaryListRowViewModel(
          key     = "whatTypeOfFiler.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", routes.WhatTypeOfFilerController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("whatTypeOfFiler.change.hidden"))
          )
        )
    }
}
