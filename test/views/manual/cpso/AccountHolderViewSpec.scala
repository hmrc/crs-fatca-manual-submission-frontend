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
import forms.AccountHolderFormProvider
import models.viewModels.AccountHolderId
import models.{AccountHolder, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.CheckboxItemViewModel
import views.html.manual.cpso.AccountHolderView

import scala.jdk.CollectionConverters.*

class AccountHolderViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: AccountHolderView = application.injector.instanceOf[AccountHolderView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val formProvider                     = new AccountHolderFormProvider()
  private val form: Form[Set[AccountHolderId]] = formProvider(Seq.empty)

  private val fiName = "Some Fi Name"

  private val options: Seq[CheckboxItem] = Seq(
    CheckboxItemViewModel(content = Text("Account holder one"), fieldId = "value", index = 0, value = "id1"),
    CheckboxItemViewModel(content = Text("Account holder two"), fieldId = "value", index = 1, value = "id2")
  )

  private def render(
    boundForm: Form[_] = form,
    mode: Mode = NormalMode,
    name: String = fiName,
    checkboxOptions: Seq[CheckboxItem] = options,
    regimeName: String = "crs"
  ): Document =
    Jsoup.parse(view(boundForm, mode, name, checkboxOptions, regimeName).body)

  private def addAccountPath(mode: Mode): String =
    controllers.manual.accountHolders.routes.AccountController.onPageLoad(mode).url

  private def sendAReportPath: String =
    controllers.manual.routes.SendAReportController.onPageLoad().url

  private def linkWithText(doc: Document, text: String) =
    doc.select("a").asScala.find(_.text() == text)

  "AccountHolderView" - {

    "page title" - {

      "must use the controlling person wording for the crs regime" in {
        render().title() mustBe
          "Account holders linked to this controlling person - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the substantial owner wording for the fatca regime" in {
        render(regimeName = "fatca").title() mustBe
          "Account holders linked to this substantial owner - Send a CRS or FATCA report - GOV.UK"
      }
    }

    "page heading" - {

      "must render a single h1" in {
        render().select("h1").size() mustBe 1
      }

      "must use the controlling person wording for the crs regime" in {
        render().select("h1").text() mustBe "Account holders linked to this controlling person"
      }

      "must use the substantial owner wording for the fatca regime, ignoring case" in {
        render(regimeName = "FATCA").select("h1").text() mustBe
          "Account holders linked to this substantial owner"
      }
    }

    "introductory paragraph" - {

      "must tell the user an account holder has to be added first" in {
        render().select("p.govuk-body").eachText().asScala.toList must contain(
          "You must add an account holder first for it to appear as an option."
        )
      }

      "must link to the add account holder page" in {
        linkWithText(render(), "add an account holder").map(_.attr("href")) mustBe
          Some(addAccountPath(NormalMode))
      }

      "must be rendered whether or not any account holders exist" in {
        render(checkboxOptions = Seq.empty).select("p.govuk-body").eachText().asScala.toList must contain(
          "You must add an account holder first for it to appear as an option."
        )
      }
    }

    "when no account holders exist" - {

      "must not render any checkboxes" in {
        render(checkboxOptions = Seq.empty).select("input[type=checkbox]").size() mustBe 0
      }

      "must not render the continue button" in {
        render(checkboxOptions = Seq.empty).select(".govuk-button").size() mustBe 0
      }

      "must link back to the send a report page, naming the regime and the financial institution" in {
        val doc = render(checkboxOptions = Seq.empty)

        linkWithText(doc, s"Back to send a CRS report for $fiName").map(_.attr("href")) mustBe
          Some(sendAReportPath)
      }

      "must use the supplied regime in the send a report link" in {
        val doc = render(checkboxOptions = Seq.empty, regimeName = "fatca")

        linkWithText(doc, s"Back to send a FATCA report for $fiName").map(_.attr("href")) mustBe
          Some(sendAReportPath)
      }
    }

    "when account holders exist" - {

      "must label each checkbox with the supplied content" in {
        val doc = render()

        doc.select("label[for=value_0]").text() mustBe "Account holder one"
        doc.select("label[for=value_1]").text() mustBe "Account holder two"
      }

      "must not render the send a report link" in {
        linkWithText(render(), s"Back to send a CRS report for $fiName") mustBe None
      }

      "must render the continue button inside the form" in {
        val doc = render()

        doc.select("form button.govuk-button").size() mustBe 1
        doc.select(".govuk-button").text() mustBe "Continue"
        doc.select(".govuk-button").attr("type") mustBe "submit"
      }
    }

    "checkbox legend and hint" - {

      "must use the controlling person wording for the crs regime" in {
        val legend = render().select(".govuk-fieldset__legend--m")

        legend.text() mustBe "Which account holders are linked to this controlling person?"
        legend.hasClass("wrappable") mustBe true
      }

      "must use the substantial owner wording for the fatca regime" in {
        render(regimeName = "fatca").select(".govuk-fieldset__legend--m").text() mustBe
          "Which account holders are linked to this substantial owner?"
      }

      "must render the hint" in {
        render().select("#value-hint").text() mustBe "Select at least one account holder."
      }
    }

    "supporting text" - {

      "must render the crs paragraph for a crs report without the fatca bullets" in {
        val doc = render()

        doc.select("p.govuk-body").eachText().asScala.toList must contain(
          "You must add at least one controlling person for the account holder if their organisation is a passive non-financial entity."
        )
        doc.select("ul.govuk-list--bullet").size() mustBe 0
      }

      "must render the fatca paragraph and bullets for a fatca report" in {
        val doc = render(regimeName = "fatca")

        doc.select("p.govuk-body").eachText().asScala.toList must contain(
          "You can add one or more substantial owners for the account holder if their organisation is either:"
        )
        doc.select("ul.govuk-list--bullet li").eachText().asScala.toList mustBe List(
          "an owner-documented foreign financial institution with specified US owners",
          "a passive non-financial foreign entity with substantial US owners, or controlling persons"
        )
      }

      "must not render the crs paragraph for a fatca report" in {
        render(regimeName = "fatca").select("p.govuk-body").eachText().asScala.toList must not contain
          "You must add at least one controlling person for the account holder if their organisation is a passive non-financial entity."
      }
    }
  }
}
