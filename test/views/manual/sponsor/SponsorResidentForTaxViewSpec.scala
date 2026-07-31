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
import forms.SponsorResidentForTaxFormProvider
import models.{Countries, NormalMode}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.sponsor.SponsorResidentForTaxView

class SponsorResidentForTaxViewSpec extends SpecBase {
  private val application = applicationBuilder().build()

  private val view: SponsorResidentForTaxView                            = application.injector.instanceOf[SponsorResidentForTaxView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  val formProvider = new SponsorResidentForTaxFormProvider()
  val form         = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "SponsorResidentForTaxView" - {
    val sponsorName = "Sponsor Name"

    val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, sponsorName, Countries.all, None)
    lazy val doc                            = Jsoup.parse(renderedHtml.body)
    println(renderedHtml.body)

    "must display title" in {
      doc.title() must include(s"Where is the sponsor resident for tax")
    }

    "must display heading" in {
      doc.select("h1").text() must include(s"Where is Sponsor Name resident for tax?")
    }

    "must display paragraph" in {
      doc.select("p").text() must include("If they are resident for tax in more than one country, then you can add other countries on the next page.")
    }

    "must contain a country select" in {

      doc.select("select#country").hasAttr("name") mustBe true
      doc.select("select#country").attr("name") mustBe "country"
    }

    "must display button" in {
      doc.select("button.govuk-button").text() mustBe "Save and continue"
    }

  }
}
