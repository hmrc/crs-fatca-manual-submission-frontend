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
import forms.manual.cpso.{CPSOWhereAreTheyBasedFormProvider, RemoveCPSOFormProvider}
import models.NormalMode
import models.SubmissionsConstants.{CRS, FATCA}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.cpso.RemoveCPSOView

class RemoveCPSOViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: RemoveCPSOView                                       = application.injector.instanceOf[RemoveCPSOView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new RemoveCPSOFormProvider()
  val form                                                               = formProvider(CRS)

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "RemoveCPSOView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, true, "Test Name")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display CRS title & heading" in {
        doc.title() must include("Are you sure you want to remove the controlling person?")
        doc.select("h1").text() must include("Are you sure you want to remove the controlling person Test Name?")
        doc.select("p").text() must include("Before you can send your report, you must make sure that account holders link to a controlling person if needed.")
      }

      "must display FATCA title & heading" in {
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, false, "Test Name")
        lazy val doc                            = Jsoup.parse(renderedHtml.body)
        doc.title() must include("Are you sure you want to remove the substantial owner?")
        doc.select("h1").text() must include("Are you sure you want to remove the substantial owner Test Name?")
        doc.select("p").text() must include("Before you can send your report, you must make sure that account holders link to a substantial owner if needed.")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Continue"
      }

    }

  }
}
