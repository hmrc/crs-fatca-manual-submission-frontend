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
import forms.manual.cpso.IndividualHaveDateOfBirthFormProvider
import models.NormalMode
import models.SubmissionsConstants.{CRS, FATCA}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.cpso.IndividualHaveDateOfBirthView

class IndividualHaveDateOfBirthViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: IndividualHaveDateOfBirthView                        = application.injector.instanceOf[IndividualHaveDateOfBirthView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new IndividualHaveDateOfBirthFormProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "IndividualHaveDateOfBirthView" - {

    "should render page components - CRS" - {

      val form                                = formProvider(CRS)
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, true, "FI name", "cpso Name")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Date of birth for the controlling person")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Date of birth for the controlling person")
      }

      "must display paragraph" in {
        doc.select("p").text() must
          include("You must provide the date of birth for cpso Name for new accounts. For pre-existing accounts, you must provide the date of birth if it’s:")
      }

      "must display list items" in {
        val liItems = doc.select("li").text()
        liItems must include("in records maintained by FI name")
        liItems must include("required for FI name to collect it")
        liItems must include("required for FI name to update account information under anti-money laundering and Know Your Customer procedures")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

    "should render page components - FATCA" - {

      val form                                = formProvider(FATCA)
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, false, "FI name", "cpso Name")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Date of birth for the substantial owner")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Date of birth for the substantial owner")
      }

      "must display paragraph" in {
        doc.select("p").text() must
          include("You must provide the date of birth for cpso Name for pre-existing accounts if it’s:")
      }

      "must display list items" in {
        val liItems = doc.select("li").text()
        liItems must include("in records maintained by FI name")
        liItems must include("required for FI name to collect it")
        liItems must include("required for FI name to update account information under anti-money laundering and Know Your Customer procedures")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

  }
}
