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

package views.manual.accountholders

import base.SpecBase
import forms.manual.accountHolders.AccountFormProvider
import models.NormalMode
import models.viewModels.{Account, AccountId, Accounts}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.CheckboxItemViewModel
import views.html.manual.accountHolders.AccountView

class AccountViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: AccountView                                          = application.injector.instanceOf[AccountView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new AccountFormProvider()
  val form                                                               = formProvider(Seq.empty)

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "AccountViewView" - {

    "should render page components - When No Accounts Available - CRS" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "FI name", Seq.empty, true)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Accounts held by this account holder")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Accounts held by this account holder")
      }

      "must display para" in {
        doc.select("p").text() must include("You must add an account first for it to appear as an option.")
      }

      "must display link" in {
        doc.select("p").text() must include("Back to send a CRS report for FI name")
      }

    }

    "should render page components - When No Accounts Available - FATCA" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "FI name", Seq.empty, false)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Accounts held by this account holder")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Accounts held by this account holder")
      }

      "must display para" in {
        doc.select("p").text() must include("You must add an account first for it to appear as an option.")
      }

      "must display link" in {
        doc.select("p").text() must include("Back to send a FATCA report for FI name")
      }

    }

    "should render page components - Accounts Available" - {

      val accId = AccountId("1")

      val accounts = Accounts(currentAccountId = None,
                              accounts = Map(
                                "1" -> Account(
                                  haveNumber = Some(false),
                                  identifier = Some("testId"),
                                  numberType = None,
                                  accountId = accId
                                )
                              )
      )

      val existingIdAndNumbers: Seq[(AccountId, String)] =
        accounts.accounts.values.flatMap {
          acc => acc.accountNumber.map(acc.accountId -> _)
        }.toSeq

      val options: Seq[CheckboxItem] =
        existingIdAndNumbers.zipWithIndex.map {
          case ((accountId, accountNumber), index) =>
            CheckboxItemViewModel(
              content = Text(accountNumber),
              fieldId = "value",
              index = index,
              value = accountId.value
            )
              .copy(name = Some(s"value[$index]"))
        }

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "FI name", options, true)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Accounts held by this account holder")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Accounts held by this account holder")
      }

      "must display para" in {
        doc.select("p").text() must include("You must add an account first for it to appear as an option.")
      }

      "must not display link" in {
        doc.select("p").text() must not include "Back to send a CRS report for FI name"
      }

      "must display fieldset heading" in {
        doc.select(".govuk-fieldset").text() must include("Which accounts does this account holder hold?")
      }

      "must display hint text" in {
        doc.select(".govuk-hint").text() must include("Select at least one account number or identifier.")
      }

      "must display checkbox values" in {
        doc.select(".govuk-checkboxes__input").attr("value") must include("1")
        doc.select(".govuk-checkboxes__label").text() must include("testId")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

  }
}
