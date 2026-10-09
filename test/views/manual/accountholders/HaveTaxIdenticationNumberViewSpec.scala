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
import forms.manual.accountHolders.HaveTaxIdenticationNumberFormProvider
import models.NormalMode
import models.response.Country
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.accountHolders.HaveTaxIdenticationNumberView

class HaveTaxIdenticationNumberViewSpec extends SpecBase {

  private val application =
    applicationBuilder().build()

  private val view: HaveTaxIdenticationNumberView                        = application.injector.instanceOf[HaveTaxIdenticationNumberView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  private val formProvider                                               = new HaveTaxIdenticationNumberFormProvider()
  private val form: Form[Boolean]                                        = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val accountHolder = "Some Name"
  private val country       = Country.GB

  private val expectedTitle   = "Do you have a tax identification number for the account holder for the country?"
  private val expectedHeading = s"Do you have a tax identification number for $accountHolder for ${country.description}?"

  "HaveTaxIdenticationNumberView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, accountHolder, country)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include(expectedTitle)
      }

      "must display the heading with the account holder name and the country" in {
        doc.select("h1").text() mustBe expectedHeading
      }

      "must display the heading as the page heading legend of the radios" in {
        doc.select("legend h1").text() mustBe expectedHeading
        doc.select("legend").attr("class") must include("govuk-fieldset__legend--l")
      }

      "must display the yes and no radios" in {
        doc.select(".govuk-radios__input").size() mustBe 2

        doc.select("#value").attr("type") mustBe "radio"
        doc.select("#value").attr("value") mustBe "true"
        doc.select("#value-no").attr("value") mustBe "false"

        doc.select("label[for=value]").text() mustBe messages("site.yes")
        doc.select("label[for=value-no]").text() mustBe messages("site.no")
      }

      "must not select a radio when the question has not been answered" in {
        doc.select(".govuk-radios__input[checked]").size() mustBe 0
      }

      "must display button" in {
        doc.select("#submit").text() mustBe messages("site.saveContinue")
      }
    }

    "should render errors when the form has errors" - {

      val renderedHtml: HtmlFormat.Appendable = view(formProvider().bind(Map("value" -> "")), NormalMode, accountHolder, country)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display the error summary" in {
        doc.select(".govuk-error-summary").size() mustBe 1
        doc.select(".govuk-error-summary__title").text() mustBe messages("error.summary.title")
        doc.select(".govuk-error-summary__body").text() must include("Select yes if you have a tax identification number")
      }

      "must link the error summary to the radios" in {
        doc.select(".govuk-error-summary a").attr("href") mustBe "#value"
      }
    }
  }
}
