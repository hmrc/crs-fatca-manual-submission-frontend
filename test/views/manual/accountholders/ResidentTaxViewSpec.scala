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
import forms.manual.accountHolders.ResidentTaxFormProvider
import models.NormalMode
import models.response.Country
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.accountHolders.ResidentTaxView

class ResidentTaxViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: ResidentTaxView                                      = application.injector.instanceOf[ResidentTaxView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new ResidentTaxFormProvider()
  val form                                                               = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val accountHolder = "Test Last"
  private val countries     = Seq(Country.GB, Country("FR", "France"))

  "ResidentTaxView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, accountHolder, countries)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Where is the account holder resident for tax?")
      }

      "must display heading with the account holder name" in {
        doc.select("h1").text() must include("Where is Test Last resident for tax?")
      }

      "must display the guidance paragraph" in {
        doc.select("p.govuk-body").text() must include(
          "If they are resident for tax in more than one country, then you can add other countries on the next page."
        )
      }

      "must display the country field with the heading as its visually hidden label" in {
        doc.select("label[for=country]").text() mustBe "Where is Test Last resident for tax?"
        doc.select("label[for=country]").attr("class") must include("govuk-visually-hidden")
        doc.select("#country").size() mustBe 1
      }

      "must display button" in {
        doc.select("button").text() mustBe "Save and continue"
      }

      "must not display an error summary" in {
        doc.select(".govuk-error-summary").size() mustBe 0
      }
    }

    "should render errors when the form has errors" - {

      val renderedHtml: HtmlFormat.Appendable = view(formProvider().bind(Map("country" -> "")), NormalMode, accountHolder, countries)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display the error summary" in {
        doc.select(".govuk-error-summary").size() mustBe 1
        doc.select(".govuk-error-summary__body").text() must include("Select where the account holder is resident for tax")
      }

      "must prefix the title with the error prefix" in {
        doc.title() must include("Error:")
        doc.title() must include("Where is the account holder resident for tax?")
      }

      "must display an inline error against the country field" in {
        doc.select("#country-error").text() must include("Select where the account holder is resident for tax")
      }
    }
  }
}
