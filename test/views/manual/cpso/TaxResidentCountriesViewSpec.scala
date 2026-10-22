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
import forms.manual.cpso.TaxResidentCountriesFormProvider
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.ControllerHelpers.request2flash
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.TaxResidentCountriesView

import scala.jdk.CollectionConverters.*

class TaxResidentCountriesViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: TaxResidentCountriesView = application.injector.instanceOf[TaxResidentCountriesView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val formProvider        = new TaxResidentCountriesFormProvider()
  private val form: Form[Boolean] = formProvider()

  private val cpsoName = "Some Name"

  private val countries = Seq("United Kingdom", "France", "Germany")

  private def render(
    boundForm: Form[_] = form,
    mode: Mode = NormalMode,
    name: String = cpsoName,
    regimeName: String = "crs",
    residentCountries: Seq[String] = countries
  ): Document =
    Jsoup.parse(view(boundForm, mode, name, regimeName, residentCountries).body)

  private def changeUrl(index: Int): String =
    controllers.manual.cpso.routes.CurrentTaxResidentIdController.onChangeRedirect(index).url

  private def removeUrl(index: Int): String =
    controllers.manual.cpso.routes.CurrentTaxResidentIdController.onRemoveRedirect(index).url

  "TaxResidentCountriesView" - {

    "page title" - {

      "must use the controlling person wording for the crs regime" in {
        render().title() mustBe
          "You have added 3 countries where the controlling person is resident for tax" +
          " - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the substantial owner wording for the fatca regime" in {
        render(regimeName = "fatca").title() mustBe
          "You have added 3 countries where the substantial owner is resident for tax" +
          " - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the singular when only one country has been added" in {
        render(residentCountries = Seq("France")).title() mustBe
          "You have added 1 country where the controlling person is resident for tax" +
          " - Send a CRS or FATCA report - GOV.UK"
      }

      "must say no countries have been added when the list is empty" in {
        render(residentCountries = Nil).title() mustBe
          "You have not added any countries where the controlling person is resident for tax" +
          " - Send a CRS or FATCA report - GOV.UK"
      }
    }

    "page heading" - {

      "must render a single h1" in {
        render().select("h1").size() mustBe 1
      }

      "must use the supplied CP-SO name and the number of countries added" in {
        render().select("h1").text() mustBe
          "You have added 3 countries where Some Name is resident for tax"
      }

      "must say no countries have been added when the list is empty" in {
        render(residentCountries = Nil).select("h1").text() mustBe
          "You have not added any countries where Some Name is resident for tax"
      }
    }

    "countries added list" - {

      "must render one row per country in the order supplied" in {
        val rows = render().select(".govuk-summary-list__row")

        rows.size() mustBe countries.size
        rows.select("dt.govuk-summary-list__key").eachText().asScala.toList mustBe countries.toList
      }

      "must render a change and a remove link for every country" in {
        val rows = render().select(".govuk-summary-list__row")

        (0 until rows.size()).foreach {
          index =>
            val row     = rows.get(index)
            val country = countries(index)
            val links   = row.select("a.govuk-link")

            links.size() mustBe 2

            val changeLink = links.get(0)
            changeLink.attr("href") mustBe changeUrl(index)
            changeLink.select("span[aria-hidden]").text() mustBe "Change"
            changeLink.select("span[aria-hidden]").attr("id") mustBe s"Change-country-$country"
            changeLink.select("span.govuk-visually-hidden").text() mustBe s"Change $country to a different country"

            val removeLink = links.get(1)
            removeLink.attr("href") mustBe removeUrl(index)
            removeLink.select("span[aria-hidden]").text() mustBe "Remove"
            removeLink.select("span[aria-hidden]").attr("id") mustBe s"remove-country-$country"
            removeLink.select("span.govuk-visually-hidden").text() mustBe s"Remove $country from the list"
        }
      }

      "must not be rendered when no countries have been added" in {
        val doc = render(residentCountries = Nil)

        doc.select(".govuk-summary-list").size() mustBe 0
        doc.select(".govuk-summary-list__row").size() mustBe 0
      }
    }

    "add another country radios" - {

      "must render a yes and a no option bound to the value field" in {
        val radios = render().select("input[type=radio]")

        radios.size() mustBe 2
        radios.eachAttr("name").asScala.toSet mustBe Set("value")
        radios.eachAttr("value").asScala.toList mustBe List("true", "false")
      }

      "must label the options Yes and No" in {
        val doc = render()

        doc.select("label[for=value]").text() mustBe "Yes"
        doc.select("label[for=value-no]").text() mustBe "No"
      }

      "must use the sub heading as the legend" in {
        render().select(".govuk-fieldset__legend--m").text() mustBe
          "Do you need to add another country?"
      }
    }

    "continue button" - {

      "must be a submit button inside the form when countries have been added" in {
        val doc = render()

        doc.select("form button.govuk-button").size() mustBe 1
        doc.select("#submit").text() mustBe "Continue"
        doc.select("#submit").attr("type") mustBe "submit"
        doc.select("a.govuk-button").size() mustBe 0
      }
    }

    "no countries have been added" - {

      "must tell the user at least one country is required" in {
        render(residentCountries = Nil).select("p.govuk-body").eachText().asScala.toList must contain(
          "You must add at least one country."
        )
      }

      "must have link to the resident for tax page" in {
        val links = render(residentCountries = Nil).select("a.govuk-button")

        links.size() mustBe 1
        links.text() mustBe "Add a country"
        links.attr("href") must endWith("/manual/cp-so/resident-for-tax")
      }

      "must not render the continue submit button" in {
        render(residentCountries = Nil).select("#submit").size() mustBe 0
      }
    }

    "success notification" - {

      "must render a notification banner when a country has been removed" in {
        val requestWithFlash = FakeRequest().withFlash("country-removed" -> "France")

        val doc = Jsoup.parse(view(form, NormalMode, cpsoName, "crs", countries)(requestWithFlash, messages, requestWithFlash.flash).body)

        doc.select(".govuk-notification-banner--success").size() mustBe 1
        doc.select(".govuk-notification-banner__title").text() mustBe "Success"
        doc.select(".govuk-notification-banner__heading").text() mustBe "France removed."
      }

      "must not render a notification banner when nothing has been removed" in {
        render().select(".govuk-notification-banner").size() mustBe 0
      }
    }
  }
}
