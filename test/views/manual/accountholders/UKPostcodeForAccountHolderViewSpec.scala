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
import controllers.routes
import forms.manual.accountHolders.UkPostCodeForAccountHolderFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.accountHolders.UkPostCodeForAccountHolderView

class UKPostcodeForAccountHolderViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: UkPostCodeForAccountHolderView                       = application.injector.instanceOf[UkPostCodeForAccountHolderView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new UkPostCodeForAccountHolderFormProvider()
  val form                                                               = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "UkPostCodeForAccountHolderView" - {

    "should render page components" - {

      val accountHolderName = "testName"

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, accountHolderName)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include(s"What is the postcode for the account holder?")
      }

      "must display heading" in {
        doc.select("h1").text() must include(s"What is the postcode for $accountHolderName?")
      }

      "must display paragraph" in {
        doc.select("p").text() must include("Enter the postcode to find the address automatically.")
      }

      "must display link" in {
        doc.select("a#enter-manually-id").text() must include("Or enter the address manually")
        doc.select("a#enter-manually-id").attr("href") mustBe routes.UnderConstructionController.onPageLoad().url
      }

      "must have autocomplete" in {
        doc.select("input").attr("autocomplete") must include("postal-code")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Find address"
      }

    }

  }
}
