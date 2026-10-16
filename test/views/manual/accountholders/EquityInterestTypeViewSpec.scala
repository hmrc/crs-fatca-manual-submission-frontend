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
import forms.manual.accountHolders.EquityInterestTypeFormProvider
import models.manual.accountHolders.{EquityInterestType, EquityInterestTypes}
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.accountHolders.EquityInterestTypeView

class EquityInterestTypeViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: EquityInterestTypeView                               = application.injector.instanceOf[EquityInterestTypeView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  private val formProvider                                               = new EquityInterestTypeFormProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val name = "test name"

  private def render(form: Form[EquityInterestTypes] = formProvider()): Document =
    Jsoup.parse(view(form, NormalMode, name).body)

  "EquityInterestTypeView" - {

    "must display the correct page title" in {
      val doc = render()

      doc.title() must include(messages("accountHolders.equityInterestType.title", name))
    }

    "must display the correct heading with the account holder's name" in {
      val doc = render()

      doc.select("h1").text() mustBe messages("accountHolders.equityInterestType.heading", name)
    }

    "must display the correct hint text" in {
      val doc = render()

      doc.select(".govuk-hint").text() mustBe messages("accountHolders.equityInterestType.hint")
    }

    "must display all checkbox options with the correct values and labels" in {
      val doc = render()

      EquityInterestType.values.zipWithIndex.foreach {
        case (equityInterestType, index) =>
          val checkbox = doc.select(s"""input[type=checkbox][value="${equityInterestType.toString}"]""")

          checkbox.size() mustBe 1
          checkbox.attr("name") mustBe s"types[$index]"

          val label = doc.select(s"""label[for="${checkbox.attr("id")}"]""")
          label.text() mustBe messages(s"accountHolders.equityInterestType.${equityInterestType.toString}")
      }
    }

    "must display all 10 checkboxes" in {
      val doc = render()

      doc.select("input[type=checkbox]").size() mustBe 10
    }

    "must preselect previously saved checkbox answers" in {
      val answer = EquityInterestTypes(
        Seq(
          EquityInterestType.SettlorOfTrust,
          EquityInterestType.ProtectorOfTrust
        )
      )

      val doc = render(formProvider().fill(answer))

      doc.select("""input[value="settlorOfTrust"]""").hasAttr("checked") mustBe true
      doc.select("""input[value="protectorOfTrust"]""").hasAttr("checked") mustBe true
      doc.select("""input[value="trusteeOfTrust"]""").hasAttr("checked") mustBe false
    }

    "must display an error summary when no checkbox is selected" in {
      val formWithErrors = formProvider().bind(Map.empty[String, String])
      val doc            = render(formWithErrors)

      doc.select(".govuk-error-summary").text() must include(
        messages("accountHolders.equityInterestType.error.required")
      )
    }

    "must display the checkbox error message when no option is selected" in {
      val formWithErrors = formProvider().bind(Map.empty[String, String])
      val doc            = render(formWithErrors)

      doc.select(".govuk-error-message").text() must include(
        messages("accountHolders.equityInterestType.error.required")
      )
    }

    "must display the save and continue button" in {
      val doc = render()

      doc.select("button.govuk-button").text() mustBe messages("site.saveAndContinue")
    }
  }
}
