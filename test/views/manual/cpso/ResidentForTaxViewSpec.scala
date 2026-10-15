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
import forms.manual.cpso.ResidentForTaxFormProvider
import models.SubmissionsConstants.CRS
import models.response.Country
import models.{Countries, Mode, NormalMode}
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.ResidentForTaxView

class ResidentForTaxViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: ResidentForTaxView = application.injector.instanceOf[ResidentForTaxView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  private val formProvider = new ResidentForTaxFormProvider()
  private val form         = formProvider(CRS)

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val cpsoName  = "Test Name"
  private val countries = Countries.allCountries(CRS)

  private val unitedKingdom = countries.find(_.code == "GB").value

  private def render(
    boundForm: Form[_] = form,
    mode: Mode = NormalMode,
    name: String = cpsoName,
    selectedCountries: Seq[Country] = countries,
    regiment: String = "crs"
  ): Document =
    Jsoup.parse(view(boundForm, mode, name, selectedCountries, regiment).body)

  "ResidentForTaxView" - {

    "page title" - {

      "must include the controlling person title for CRS" in {
        render().title() mustBe "Where is the controlling person resident for tax? - Send a CRS or FATCA report - GOV.UK"
      }

      "must include the substantial owner title for FATCA" in {
        render(regiment = "fatca").title() mustBe
          "Where is the substantial owner resident for tax? - Send a CRS or FATCA report - GOV.UK"
      }

    }

    "page content" - {

      "must render the heading, naming the controlling person" in {
        render().select("h1").text() mustBe "Where is Test Name resident for tax?"
      }

      "must render the heading with the supplied CP-SO name" in {
        render(name = "Someone Else").select("h1").text() mustBe "Where is Someone Else resident for tax?"
      }

      "must render the explanatory paragraph" in {
        render().select("p.govuk-body").text() must include(
          "If they are resident for tax in more than one country, then you can add other countries on the next page."
        )
      }

      "must render button" in {
        render().select("#submit").text() mustBe "Save and continue"
      }
    }

  }
}
