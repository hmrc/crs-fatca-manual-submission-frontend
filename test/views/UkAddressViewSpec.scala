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

package views

import base.SpecBase
import forms.UkAddressFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.UkAddressView

class UkAddressViewSpec extends SpecBase {
  private val application = applicationBuilder().build()

  private val view: UkAddressView = application.injector.instanceOf[UkAddressView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider = new UkAddressFormProvider()


  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "UkAddressView" - {
    "should render page components" - {
        val renderedHtml = view(formProvider(), NormalMode, "Test Sponsor Name")
        lazy val doc = Jsoup.parse(renderedHtml.body)
        println(renderedHtml.body)
        val expectedTitleLabels = Seq(
            "Address line 1 ",
            "Address line 2 (Optional)",
            "City",
            "County (Optional)",
            "Postcode",
            "Country"
        )

        "must display title" in {
            doc.title() must include("What is the sponsor’s registered address?")
        }

        "must display heading" in {
            doc.select("h1").text() must include("What is the registered address for Test Sponsor Name?")
        }

        "must display all address fields" in {
            val labels = doc.select(".govuk-label").eachText()
            expectedTitleLabels.foreach { label =>
                labels must contain(label.trim())
            }
        }

        "must display all address fields with correct autocomplete attributes" in {
            val expectedAutocompleteAttributes = Map(
                "addressLine1" -> "address-line1",
                "addressLine2" -> "address-line2",
                "city" -> "address-level2",
                "county" -> "address-level1",
                "postCode" -> "postal-code"
            )

            expectedAutocompleteAttributes.foreach { case (fieldId, expectedValue) =>
                val actualValue = doc.select(s"#$fieldId").attr("autocomplete")
                actualValue mustBe expectedValue
            }
        }

        "must contain a country field with the correct label " in {
            val countryLabel = doc.select("label[for=country]").text()
            countryLabel mustBe "Country"
            val nameAttribute = doc.select("#country").attr("name")
            nameAttribute mustBe "country"

            val gbOption = doc.select("#country option[value=GB]")
            gbOption.isEmpty mustBe false
            gbOption.text() mustBe "United Kingdom"

            val ggOption = doc.select("#country option[value=GG]")
            ggOption.isEmpty mustBe false
            ggOption.text() mustBe "Guernsey"

            val imOption = doc.select("#country option[value=IM]")
            imOption.isEmpty mustBe false
            imOption.text() mustBe "Isle of Man"
        }
      
        "must display button" in {
            doc.select(".govuk-button").text() mustBe "Save and continue"
        }
    }
  }
}
