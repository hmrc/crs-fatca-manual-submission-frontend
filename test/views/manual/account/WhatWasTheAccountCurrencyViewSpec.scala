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

package views.manual.account

import base.SpecBase
import forms.manual.account.WhatWasTheAccountCurrencyFormProvider
import models.NormalMode
import models.SubmissionsConstants.CRS
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.account.WhatWasTheAccountCurrencyView

class WhatWasTheAccountCurrencyViewSpec extends SpecBase {

  private val application                                                = applicationBuilder().build()
  private val regime                                                     = CRS
  private val view: WhatWasTheAccountCurrencyView                        = application.injector.instanceOf[WhatWasTheAccountCurrencyView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val form                                                               = new WhatWasTheAccountCurrencyFormProvider()(regime)

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "WhatWasTheAccountCurrencyView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, regime)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("What was the account currency?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("What was the account currency?")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

      "must display currency field" in {
        val elements = doc.select("#currency")
        elements.size() mustBe 1
      }

    }

  }
}
