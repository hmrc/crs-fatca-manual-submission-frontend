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
import forms.manual.cpso.RemoveTaxResidentCountryFormProvider
import models.SubmissionsConstants.CRS
import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.RemoveTaxResidentCountryView

import scala.jdk.CollectionConverters.*

class RemoveTaxResidentCountryViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: RemoveTaxResidentCountryView = application.injector.instanceOf[RemoveTaxResidentCountryView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val formProvider        = new RemoveTaxResidentCountryFormProvider()
  private val form: Form[Boolean] = formProvider(CRS)

  private val country  = "Germany"
  private val cpsoName = "Some Name"

  private def render(
    boundForm: Form[_] = form,
    mode: Mode = NormalMode,
    countryName: String = country,
    name: String = cpsoName,
    regimeName: String = "crs"
  ): Document =
    Jsoup.parse(view(boundForm, mode, countryName, name, regimeName).body)

  "RemoveTaxResidentCountryView" - {

    "page title" - {

      "must use the controlling person wording for the crs regime" in {
        render().title() mustBe
          "Are you sure you want to remove this tax-resident country for the controlling person?" +
          " - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the substantial owner wording for the fatca regime" in {
        render(regimeName = "fatca").title() mustBe
          "Are you sure you want to remove this tax-resident country for the substantial owner?" +
          " - Send a CRS or FATCA report - GOV.UK"
      }
    }

    "page heading" - {

      "must display header name" in {
        render(countryName = "France", name = "Someone Else").select("h1").text() mustBe
          "Are you sure you want to remove France as a tax-resident country for Someone Else?"
      }
    }

    "yes or no radios" - {

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
    }

    "submit button" - {

      "must render a save and continue button inside the form" in {
        val doc = render()

        doc.select("form button.govuk-button").size() mustBe 1
        doc.select("#submit").text() mustBe "Save and continue"
      }
    }
  }
}
