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
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.ElectionsSentView

class ElectionsSentViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: ElectionsSentView                                    = application.injector.instanceOf[ElectionsSentView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "ElectionsSentView" - {

    "should render page components" - {

      val fiId                                = "12345"
      val reportingYear                       = "2027"
      val regime                              = "FATCA"
      val renderedHtml: HtmlFormat.Appendable = view(regime, "Test FI", reportingYear, "test@test.com", fiId)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("FATCA elections sent for the financial institution for 2027")
      }

      "must display heading" in {
        doc.select("h1").text() must include("FATCA elections sent for Test FI for 2027")
      }

      "must display paragraph" in {
        val pragraphText = doc.select("p").text()
        pragraphText must include(
          "We’ll contact you if we have any questions about your elections. If you need to change or remove these elections in the future, then you’ll need to email"
        )
        pragraphText must include("test@test.com")
        pragraphText must include("Back to manage elections for Test FI")
        pragraphText must include("Back to manage your CRS and FATCA reports")
      }

      "must display links" in {
        val pragraphText = doc.select("a")
        pragraphText
          .stream()
          .anyMatch(_.attr("href").equalsIgnoreCase("mailto:test@test.com")) mustBe true
        pragraphText
          .stream()
          .anyMatch(_.attr("href").contains("/manage-your-crs-and-fatca-financial-institutions")) mustBe true
        pragraphText
          .stream()
          .anyMatch(
            _.attr("href").contains("/crs-fatca-manual-submission-frontend/elections/manage-elections-for-2027?fiId=12345")
          ) mustBe true
      }

    }

  }
}
