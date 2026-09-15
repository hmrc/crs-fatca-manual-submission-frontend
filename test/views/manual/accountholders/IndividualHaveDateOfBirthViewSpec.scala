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
import forms.manual.accountHolders.IndividualHaveDateOfBirthFormProvider
import models.NormalMode
import models.SubmissionsConstants.CRS
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.accountHolders.IndividualHaveDateOfBirthView

class IndividualHaveDateOfBirthViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: IndividualHaveDateOfBirthView                        = application.injector.instanceOf[IndividualHaveDateOfBirthView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new IndividualHaveDateOfBirthFormProvider()
  val form                                                               = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "IndividualHaveDateOfBirthView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "FI name", "account holder name", CRS)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Date of birth for the account holder")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Date of birth for the account holder")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

  }
}
