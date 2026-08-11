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

package views.manual.sponsor

import base.SpecBase
import forms.manual.sponsor.TaxResidentCountriesFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.sponsor.TaxResidentCountriesView
import play.api.mvc.ControllerHelpers.request2flash

class TaxResidentCountriesViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: TaxResidentCountriesView                             = application.injector.instanceOf[TaxResidentCountriesView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new TaxResidentCountriesFormProvider()
  val form                                                               = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "TaxResidentCountriesView" - {

    "should render page components when No Countries are added" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "Test Sponsor", Seq.empty)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("You have not added any countries where the sponsor is resident for tax")
      }

      "must display heading" in {
        doc.select("h1").text() must include("You have not added any countries where Test Sponsor is resident for tax")
      }

      "must display paragraph" in {
        doc.select("p").text() must include("You must add at least one country.")
      }

      "must display button" in {
        doc.select("a").text() must include("Add a country")
      }

    }

    "should render page components when 1 Country is added" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "Test Sponsor", Seq("GB"))
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("You have added 1 country where the sponsor is resident for tax")
      }

      "must display heading" in {
        doc.select("h1").text() must include("You have added 1 country where Test Sponsor is resident for tax")
      }

      "must display sub heading" in {
        doc.select(".govuk-fieldset__legend--m").text() must include("Do you need to add another country?")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Continue"
      }

    }

    "should render page components when more than 1 Country are added" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "Test Sponsor", Seq("GB", "FR"))
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("You have added 2 countries where the sponsor is resident for tax")
      }

      "must display heading" in {
        doc.select("h1").text() must include("You have added 2 countries where Test Sponsor is resident for tax")
      }

      "must display sub heading" in {
        doc.select(".govuk-fieldset__legend--m").text() must include("Do you need to add another country?")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Continue"
      }

    }

  }
}
