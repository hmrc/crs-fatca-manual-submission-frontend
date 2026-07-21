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
import forms.manual.sponsor.AddressNonUkFormProvider
import models.{AddressNonUk, Countries, NormalMode}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.sponsor.AddressNonUkView

class AddressNonUkViewSpec extends SpecBase {

  private val application =
    applicationBuilder().build()

  private val view: AddressNonUkView =
    application.injector.instanceOf[AddressNonUkView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  private val formProvider =
    new AddressNonUkFormProvider()

  private val form =
    formProvider()

  implicit private val request: FakeRequest[AnyContent] =
    FakeRequest()

  implicit private val messages: Messages =
    messagesControllerComponents.messagesApi.preferred(
      Seq(Lang("en"))
    )

  private val sponsorName = "Test Sponsor"

  "AddressNonUkView" - {

    "should render the page components" - {

      val renderedHtml: HtmlFormat.Appendable =
        view(
          form,
          NormalMode,
          sponsorName
        )

      lazy val doc =
        Jsoup.parse(renderedHtml.body)

      "must display the page title" in {
        doc.title() must include(
          "What is the sponsor's address?"
        )
      }

      "must display the heading containing the sponsor name" in {
        doc.select("h1").text() mustBe
          s"What is the address for $sponsorName?"
      }

      "must display the address line 1 input" in {
        doc.select("label[for=addressLine1]").text() mustBe
          "Address line 1"

        doc.select("#addressLine1").size() mustBe 1
      }

      "must display the optional address line 2 input" in {
        doc.select("label[for=addressLine2]").text() mustBe
          "Address line 2 (optional)"

        doc.select("#addressLine2").size() mustBe 1
      }

      "must display the city input" in {
        doc.select("label[for=addressLine3]").text() mustBe
          "City"

        doc.select("#addressLine3").size() mustBe 1
      }

      "must display the optional region input" in {
        doc.select("label[for=addressLine4]").text() mustBe
          "Region (optional)"

        doc.select("#addressLine4").size() mustBe 1
      }

      "must display the optional postcode input" in {
        doc.select("label[for=postcode]").text() mustBe
          "Postcode (optional)"

        doc.select("#postcode").size() mustBe 1
      }

      "must add autocomplete attributes to the address inputs" in {
        doc.select("#addressLine1").attr("autocomplete") mustBe
          "address-line1"

        doc.select("#addressLine2").attr("autocomplete") mustBe
          "address-line2"

        doc.select("#addressLine3").attr("autocomplete") mustBe
          "address-line3"

        doc.select("#addressLine4").attr("autocomplete") mustBe
          "address-line4"

        doc.select("#postcode").attr("autocomplete") mustBe
          "address-postcode"
      }

      "must display the country field" in {
        doc.select("label[for=country]").text() mustBe
          "Country"

        doc.select("#country").size() mustBe 1
      }

      "must display a blank option first" in {
        val firstOption =
          doc.select("#country option").first()

        firstOption.text() mustBe ""
        firstOption.attr("value") mustBe ""
        firstOption.hasAttr("selected") mustBe true
      }

      "must display all countries and the blank option" in {
        doc.select("#country option").size() mustBe
          Countries.all.size + 1
      }

      "must use the country code as the option value" in {
        val france =
          doc.select("#country option[value=FR]")

        france.size() mustBe 1
        france.text() mustBe "France"
      }

      "must configure the country field as an accessible autocomplete" in {
        val countrySelect =
          doc.select("#country")

        countrySelect.attr("data-module") mustBe
          "hmrc-accessible-autocomplete"

        countrySelect.attr("data-show-all-values") mustBe
          "false"

        countrySelect.attr("data-auto-select") mustBe
          "false"

        countrySelect.attr("data-default-value") mustBe ""
      }

      "must submit to the AddressNonUk controller" in {
        val formElement =
          doc.select("form").first()

        formElement.attr("method").toLowerCase mustBe "post"

        formElement.attr("action") mustBe
          controllers.manual.sponsor.routes.AddressNonUkController
            .onSubmit(NormalMode)
            .url
      }

      "must display the save and continue button" in {
        doc.select("button.govuk-button").text() mustBe
          messages("site.saveContinue")
      }
    }

    "when the form contains an existing address" - {

      val address = AddressNonUk(
        addressLine1 = "1 Test Street",
        addressLine2 = Some("Test Building"),
        addressLine3 = "Paris",
        addressLine4 = Some("Ile de France"),
        postcode = Some("75001"),
        country = "FR"
      )

      val renderedHtml =
        view(
          form.fill(address),
          NormalMode,
          sponsorName
        )

      lazy val doc =
        Jsoup.parse(renderedHtml.body)

      "must populate the address fields" in {
        doc.select("#addressLine1").attr("value") mustBe
          "1 Test Street"

        doc.select("#addressLine2").attr("value") mustBe
          "Test Building"

        doc.select("#addressLine3").attr("value") mustBe
          "Paris"

        doc.select("#addressLine4").attr("value") mustBe
          "Ile de France"

        doc.select("#postcode").attr("value") mustBe
          "75001"
      }

      "must select the previously answered country" in {
        val selectedCountry =
          doc.select("#country option[selected]")

        selectedCountry.size() mustBe 1
        selectedCountry.attr("value") mustBe "FR"
        selectedCountry.text() mustBe "France"
      }
    }

    "when the submitted form contains errors" - {

      val invalidForm =
        form.bind(
          Map(
            "addressLine1" -> "",
            "addressLine2" -> "",
            "addressLine3" -> "",
            "addressLine4" -> "",
            "postcode"     -> "",
            "country"      -> ""
          )
        )

      val renderedHtml =
        view(
          invalidForm,
          NormalMode,
          sponsorName
        )

      lazy val doc =
        Jsoup.parse(renderedHtml.body)

      "must display the error summary" in {
        doc.select(".govuk-error-summary").size() mustBe 1
      }

      "must display the address line 1 error" in {
        doc.select("#addressLine1-error").text() must include(
          "Enter Address line 1"
        )
      }

      "must display the city error" in {
        doc.select("#addressLine3-error").text() must include(
          "Enter city"
        )
      }

      "must display the country error" in {
        doc.select("#country-error").text() must include(
          "Select country"
        )
      }
    }
  }
}
