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
import forms.manual.account.WasAccountOpenFormProvider
import models.NormalMode
import models.manual.account.WasAccountOpen
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.account.WasAccountOpenView

class WasAccountOpenViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: WasAccountOpenView                                   = application.injector.instanceOf[WasAccountOpenView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new WasAccountOpenFormProvider()
  val form: Form[WasAccountOpen]                                         = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "HaveNumberView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, 2020)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Was the account opened in 2020?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Was the account opened in 2020?")
      }

      "must display not applicable as accounting year was before 2025" in {
        doc.body().text() must include("Not reported")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

  }
}
