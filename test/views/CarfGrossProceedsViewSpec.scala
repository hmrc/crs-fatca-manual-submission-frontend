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

package views

import base.SpecBase
import forms.CarfGrossProceedsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.CarfGrossProceedsView

class CarfGrossProceedsViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: CarfGrossProceedsView                                = application.injector.instanceOf[CarfGrossProceedsView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new CarfGrossProceedsFormProvider()
  val form                                                               = formProvider("2027")

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "CarfGrossProceedsView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "TestFIName", "2027")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Is the financial institution reporting gross proceeds under the Cryptoasset Reporting Framework (CARF) for 2027?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Is TestFIName reporting gross proceeds under the Cryptoasset Reporting Framework (CARF) for 2027?")
        doc.select("h1").text() must include("CRS elections for 2027")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Continue"
      }

    }

  }
}
