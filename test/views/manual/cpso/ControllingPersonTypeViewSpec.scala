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

package views.manual.cpso

import base.SpecBase
import forms.manual.cpso.ControllingPersonTypeFormProvider
import models.manual.cpso.{ControllingPersonType, ControllingPersonTypes}
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.ControllingPersonTypeView

class ControllingPersonTypeViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: ControllingPersonTypeView                            = application.injector.instanceOf[ControllingPersonTypeView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  private val formProvider                                               = new ControllingPersonTypeFormProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val name = "test name"

  private def render(reportingYear: Int, form: Form[ControllingPersonTypes] = formProvider()): Document =
    Jsoup.parse(view(form, NormalMode, reportingYear, name).body)

  "ControllingPersonTypeView" - {

    "must display the correct page title" in {
      val doc = render(2025)

      doc.title() must include(messages("cpso.controllingPersonType.title"))
    }

    "must display the correct heading with the controlling person's name" in {
      val doc = render(2025)

      doc.select("h1").text() mustBe messages("cpso.controllingPersonType.heading", name)
    }

    "must display the correct hint text" in {
      val doc = render(2025)

      doc.select(".govuk-hint").text() mustBe messages("cpso.controllingPersonType.hint")
    }

    "must display all checkbox options with the correct values and labels" in {
      val doc = render(2025)

      ControllingPersonType.values.zipWithIndex.foreach {
        case (personType, index) =>
          val checkbox = doc.select(s"""input[type=checkbox][value="${personType.toString}"]""")

          checkbox.size() mustBe 1
          checkbox.attr("name") mustBe s"types[$index]"

          val label = doc.select(s"""label[for="${checkbox.attr("id")}"]""")
          label.text() mustBe messages(s"cpso.controllingPersonType.${personType.toString}")
      }
    }

    "must display all 14 checkboxes for reporting years before 2025" in {
      val doc = render(2024)

      doc.select("input[type=checkbox]").size() mustBe 14
      doc.select("""input[type=checkbox][value="notReported"]""").size() mustBe 1
    }

    "must display all 14 checkboxes for reporting year 2025" in {
      val doc = render(2025)

      doc.select("input[type=checkbox]").size() mustBe 14
      doc.select("""input[type=checkbox][value="notReported"]""").size() mustBe 1
    }

    "must display only 13 checkboxes for reporting years after 2025" in {
      val doc = render(2026)

      doc.select("input[type=checkbox]").size() mustBe 13
      doc.select("""input[type=checkbox][value="notReported"]""").size() mustBe 0
    }

    "must preselect previously saved checkbox answers" in {
      val answer = ControllingPersonTypes(
        Seq(
          ControllingPersonType.ProtectorOfTrust,
          ControllingPersonType.OtherControllingPersonOfTrust
        )
      )

      val doc = render(2025, formProvider().fill(answer))

      doc.select("""input[value="protectorOfTrust"]""").hasAttr("checked") mustBe true
      doc.select("""input[value="otherControllingPersonOfTrust"]""").hasAttr("checked") mustBe true
      doc.select("""input[value="trusteeOfTrust"]""").hasAttr("checked") mustBe false
    }

    "must display an error summary when no checkbox is selected" in {
      val formWithErrors = formProvider().bind(Map.empty[String, String])
      val doc            = render(2025, formWithErrors)

      doc.select(".govuk-error-summary").text() must include(
        messages("cpso.controllingPersonType.error.required")
      )
    }

    "must display the checkbox error message when no option is selected" in {
      val formWithErrors = formProvider().bind(Map.empty[String, String])
      val doc            = render(2025, formWithErrors)

      doc.select(".govuk-error-message").text() must include(
        messages("cpso.controllingPersonType.error.required")
      )
    }

    "must display the save and continue button" in {
      val doc = render(2025)

      doc.select("#submit").text() mustBe "Save and continue"
    }
  }
}
