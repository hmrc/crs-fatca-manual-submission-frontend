package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, ReportId, UserAnswers}
import pages.WhatIsAccountNumberPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WhatIsAccountNumberSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages, reportId: ReportId): Option[SummaryListRow] =
    answers.get(WhatIsAccountNumberPage()).map {
      answer =>

        SummaryListRowViewModel(
          key     = "whatIsAccountNumber.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.WhatIsAccountNumberController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("whatIsAccountNumber.change.hidden"))
          )
        )
    }
}
