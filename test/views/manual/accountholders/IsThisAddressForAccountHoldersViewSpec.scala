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
import forms.manual.accountHolders.IsThisTheAddressForAccountHoldersFormProvider
import models.NormalMode
import models.response.{Address, Country}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.accountHolders.IsThisTheAddressForAccountHoldersView

class IsThisAddressForAccountHoldersViewSpec extends SpecBase {
  private val application = applicationBuilder().build()

  private val view: IsThisTheAddressForAccountHoldersView                = application.injector.instanceOf[IsThisTheAddressForAccountHoldersView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val form                                                               = new IsThisTheAddressForAccountHoldersFormProvider()()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val address: Address =
    Address(None, "1 Address line 1 Road", None, Some("Address line 2 Road"), None, "Town", Some("zz11zz"), Country.GB)

  "IsThisAddressForAccountHoldersView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, address, "TestAccountHolder")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Is this the account holder’s address?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Is this the address for TestAccountHolder?")
      }

      "must display address" in {
        doc.select("p").text() must include("1 Address line 1 Road Address line 2 Road Town zz11zz")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

  }
}
