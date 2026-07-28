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
import forms.manual.sponsor.HaveSponsorFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.manual.sponsor.WhatIsAddressForSponsorView

class WhatIsAddressForSponsorViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: WhatIsAddressForSponsorView                          = application.injector.instanceOf[WhatIsAddressForSponsorView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val form                                                               = new HaveSponsorFormProvider()()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val addressRadios: Seq[RadioItem] = Seq(
    RadioItem(content = Text("1 Address line 1, Town, ZZ1 1ZZ"), value = Some("1 Address line 1, Town, ZZ1 1ZZ, United Kingdom")),
    RadioItem(content = Text("2 Address line 1, Town, ZZ1 1ZZ"), value = Some("2 Address line 1, Town, ZZ1 1ZZ, United Kingdom"))
  )
  "WhatIsAddressForSponsorView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "TestSponsorName", addressRadios)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("What is the sponsor’s address?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("What is the address for TestSponsorName?")
      }

      "must have radios" in {
        doc.select(".govuk-radios__item").size() mustBe 2
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

  }
}
