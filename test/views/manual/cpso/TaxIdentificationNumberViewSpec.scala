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
import forms.manual.cpso.TaxIdentificationNumberFormProvider
import models.SubmissionsConstants.CRS
import models.manual.cpso.IndividualOrOrganisation.Individual
import models.response.Country
import models.{Mode, NormalMode, ReportId, TaxIdentificationNumber}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.TaxIdentificationNumberView

class TaxIdentificationNumberViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: TaxIdentificationNumberView = application.injector.instanceOf[TaxIdentificationNumberView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  private val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private val form: Form[String] =
    new TaxIdentificationNumberFormProvider()(Country.GB, reportId, Some(Individual))

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val cpsoName = "Test Name"

  private val pageHeading = s"What is the tax identification number for $cpsoName for United Kingdom?"

  private def render(
    boundForm: Form[_] = form,
    mode: Mode = NormalMode,
    name: String = cpsoName,
    country: Country = Country.GB,
    regimeName: String = "crs"
  ): Document =
    Jsoup.parse(view(boundForm, mode, name, country, regimeName).body)

  "TaxIdentificationNumberView" - {

    "page title" - {

      "must use the controlling person wording for the crs regime" in {
        render().title() mustBe
          "What is the tax identification number for the controlling person for the country?" +
          " - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the substantial owner wording for the fatca regime" in {
        render(regimeName = "fatca").title() mustBe
          "What is the tax identification number for the substantial owner for the country?" +
          " - Send a CRS or FATCA report - GOV.UK"
      }
    }

    "page heading" - {

      "must render a single h1" in {
        render().select("h1").size() mustBe 1
      }

      "must ask the question for the supplied CP-SO name and country" in {
        render().select("h1").text() mustBe pageHeading
      }

      "must use the country's in-sentence display name when one is supplied" in {
        val unitedStates = Country("US", "United States", None, Some("the United States"))

        render(country = unitedStates).select("h1").text() mustBe
          s"What is the tax identification number for $cpsoName for the United States?"
      }

      "must display other country in curly quotes" in {
        val other = Country("XX", "Other", None, None)

        render(country = other).select("h1").text() mustBe
          s"What is the tax identification number for $cpsoName for ‘Other’?"
      }
    }

    "submit button" - {

      "must render a single save and continue button inside the form" in {
        val buttons = render().select("form button.govuk-button")

        buttons.size() mustBe 1
        buttons.text() mustBe "Save and continue"
      }
    }
  }
}
