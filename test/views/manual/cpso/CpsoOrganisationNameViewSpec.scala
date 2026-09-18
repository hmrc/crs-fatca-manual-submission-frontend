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
import forms.manual.cpso.CpsoOrganisationNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.CpsoOrganisationNameView

class CpsoOrganisationNameViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: CpsoOrganisationNameView                             = application.injector.instanceOf[CpsoOrganisationNameView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new CpsoOrganisationNameFormProvider()
  implicit private val request: FakeRequest[AnyContent]                  = FakeRequest()
  implicit private val messages: Messages                                = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "CpsoOrganisationNameView" - {
    "should render page components" - {
      val form                                = formProvider()
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("What is the name of the organisation?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("What is the name of the organisation?")
      }

      "input field has organization auto-complete attribute" in {
        doc.select(".govuk-input").attr("autocomplete") must include("organization")
      }
    }
  }

}
