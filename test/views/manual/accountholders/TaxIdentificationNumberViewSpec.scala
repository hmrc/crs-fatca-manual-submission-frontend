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
import forms.manual.accountHolders.TaxIdentificationNumberFormProvider
import models.SubmissionsConstants.CRS
import models.manual.accountHolders.IndividualOrOrganisation.Individual
import models.{NormalMode, ReportId}
import models.response.Country
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.accountHolders.TaxIdentificationNumberView

class TaxIdentificationNumberViewSpec extends SpecBase {
  private val application = applicationBuilder().build()

  private val view: TaxIdentificationNumberView                          = application.injector.instanceOf[TaxIdentificationNumberView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  private val reportId: ReportId                                         = ReportId(CRS, 2025, None, "TestfiID")

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val accountHolder = "Test Last"
  private val fr            = Country("FR", "France")

  val formProvider = new TaxIdentificationNumberFormProvider()
  val form         = formProvider(fr, reportId, Individual)

  "TaxIdentificationNumberView" - {
    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, fr, accountHolder)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("What is the tax identification number for the account holder for the country?")
      }

      "must display heading with the account holder name" in {
        doc.select("h1").text() must include("What is the tax identification number for Test Last for France?")
      }

      "must display button" in {
        doc.select("button").text() mustBe "Save and continue"
      }
    }
  }

}
