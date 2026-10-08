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
        doc.select("h1").text() must include("Where is Account Test Last for tax?")
      }

      "must display the guidance paragraph" in {
        doc.select("p.govuk-body").text() must include("If they are resident for tax in more than one country")
      }

      "must display the country field with the heading as its visually hidden label" in {
        doc.select("label[for=country]").text() mustBe "Where is Account Test Last for tax?"
        doc.select("label[for=country]").attr("class") must include("govuk-visually-hidden")
        doc.select("#country").size() mustBe 1
      }

      "must display the no-JavaScript country placeholder first" in {
        val firstOption = doc.select("#country option").first()

        firstOption.text() mustBe messages("addressNonUk.country.select")
        firstOption.attr("value") mustBe ""
        firstOption.hasAttr("selected") mustBe true
      }

      "must display the supplied countries with their code as the option value" in {
        doc.select("#country option").size() mustBe countries.size + 1

        val france = doc.select("#country option[value=FR]")
        france.size() mustBe 1
        france.text() mustBe "France"

        val unitedKingdom = doc.select("#country option[value=GB]")
        unitedKingdom.size() mustBe 1
        unitedKingdom.text() mustBe "United Kingdom"
      }

      "must display button" in {
        doc.select("button").text() mustBe "Save and continue"
      }

      "must not display an error summary" in {
        doc.select(".govuk-error-summary").size() mustBe 0
      }
    }

    "should preselect the answered country when the form is populated" - {

      val renderedHtml: HtmlFormat.Appendable = view(form.fill("GB"), NormalMode, accountHolder, countries)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must select the matching country and deselect the placeholder" in {
        doc.select("#country option[value=GB]").first().hasAttr("selected") mustBe true
        doc.select("#country option").first().hasAttr("selected") mustBe false
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
