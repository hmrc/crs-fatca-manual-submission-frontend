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

package viewmodels.checkAnswers.manual.account

import models.viewModels.AccountId
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryList, SummaryListRow, Value}

object AccountsSummary {

  def apply(
    accounts: Seq[(AccountId, String)],
    isComplete: AccountId => Boolean,
    changeAction: AccountId => Call,
    removeAction: AccountId => Call
  )(implicit messages: Messages): SummaryList =
    SummaryList(
      classes = "accounts-summary-list",
      rows = accounts.map {
        case (accountId, accountNumber) =>
          row(
            accountNumber,
            isComplete(accountId),
            changeAction(accountId),
            removeAction(accountId)
          )
      }
    )

  private def row(
    accountNumber: String,
    isComplete: Boolean,
    changeAction: Call,
    removeAction: Call
  )(implicit messages: Messages): SummaryListRow = {

    val incompleteTag =
      if (isComplete) ""
      else
        s"""<strong class="govuk-tag govuk-tag--blue">${escape(messages("accounts.incomplete"))}</strong>"""

    SummaryListRow(
      key = Key(
        content = HtmlContent(
          s"""<span class="govuk-!-margin-right-2">${escape(accountNumber)}</span> $incompleteTag"""
        ),
        classes = "govuk-!-font-weight-regular"
      ),
      value = Value(content = Text("")),
      actions = Some(
        Actions(
          items = Seq(
            action(
              changeAction,
              "site.change",
              "accounts.change.hidden",
              accountNumber
            ),
            action(
              removeAction,
              "site.remove",
              "accounts.remove.hidden",
              accountNumber
            )
          )
        )
      )
    )
  }

  private def action(
    destination: Call,
    visibleMessageKey: String,
    hiddenMessageKey: String,
    accountNumber: String
  )(implicit messages: Messages): ActionItem =
    ActionItem(
      href = destination.url,
      content = HtmlContent(
        s"""<span aria-hidden="true">${escape(messages(visibleMessageKey))}</span><span class="govuk-visually-hidden">${escape(
            messages(hiddenMessageKey, accountNumber)
          )}</span>"""
      ),
      visuallyHiddenText = None
    )

  private def escape(value: String): String =
    HtmlFormat.escape(value).body
}
