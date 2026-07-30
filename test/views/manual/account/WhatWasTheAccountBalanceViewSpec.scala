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
import forms.manual.account.WhatWasTheAccountBalanceFormProvider
import forms.manual.sponsor.HaveSponsorFormProvider
import models.NormalMode
import models.SubmissionsConstants.{CRS, FATCA}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.account.WhatWasTheAccountBalanceView

class WhatWasTheAccountBalanceViewSpec extends SpecBase {

  private val application                                                = applicationBuilder().build()
  private val regime                                                     = CRS
  private val view: WhatWasTheAccountBalanceView                         = application.injector.instanceOf[WhatWasTheAccountBalanceView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val form                                                               = new WhatWasTheAccountBalanceFormProvider()(regime)

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "WhatWasTheAccountBalanceView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, regime)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("What was the account balance?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("What was the account balance?")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

      "must display currency field" in {
        val elements = doc.select("#currency")
        elements.size() mustBe 1
      }

      "must display amount field" in {
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, regime)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)
        val elements                            = doc.select("#amount")
        elements.size() mustBe 1
      }

    }

  }
}
