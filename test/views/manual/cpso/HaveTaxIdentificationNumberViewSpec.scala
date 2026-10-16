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
import forms.manual.cpso.HaveTaxIdentificationNumberFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import models.response.Country
import models.{Countries, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.HaveTaxIdentificationNumberView

import scala.jdk.CollectionConverters.*

class HaveTaxIdentificationNumberViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: HaveTaxIdentificationNumberView = application.injector.instanceOf[HaveTaxIdentificationNumberView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  private val formProvider = new HaveTaxIdentificationNumberFormProvider()
  private val form         = formProvider(CRS)

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val cpsoName = "Test Name"

  private val unitedKingdom = Country.GB
  private val otherCountry  = Countries.allCountries(FATCA).find(_.code == "XX").value

  private def render(
    boundForm: Form[_] = form,
    mode: Mode = NormalMode,
    country: Country = unitedKingdom,
    name: String = cpsoName,
    regiment: String = "crs"
  ): Document =
    Jsoup.parse(view(boundForm, mode, country, name, regiment).body)

  "HaveTaxIdentificationNumberViewx" - {

    "page title" - {

      "must use the controlling person title for CRS" in {
        render().title() mustBe
          "Do you have a tax identification number for the controlling person for the country? - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the substantial owner title for FATCA" in {
        render(regiment = "fatca").title() mustBe
          "Do you have a tax identification number for the substantial owner for the country? - Send a CRS or FATCA report - GOV.UK"
      }

    }

    "page heading" - {

      "must use the supplied CP-SO name" in {
        render(name = "Someone Else").select("h1").text() mustBe
          "Do you have a tax identification number for Someone Else for United Kingdom?"
      }

      "must quote the country when it is the 'Other country' entry" in {
        render(country = otherCountry).select("h1").text() mustBe
          "Do you have a tax identification number for Test Name for ‘Other country’?"
      }

    }

    "yes/no radios" - {

      "must render a yes and a no option bound to the value field" in {
        val radios = render().select("input[type=radio]")

        radios.size mustBe 2
        radios.eachAttr("name").asScala.toSet mustBe Set("value")
        radios.eachAttr("value").asScala.toList mustBe List("true", "false")
      }

      "must label the options Yes and No" in {
        val doc = render()

        doc.select("label[for=value]").text() mustBe "Yes"
        doc.select("label[for=value-no]").text() mustBe "No"
      }

    }

    "form" - {
      "must render the save and continue button" in {
        val buttons = render().select(".govuk-button")

        buttons.size mustBe 1
        buttons.text() mustBe "Save and continue"
      }
    }
  }
}
