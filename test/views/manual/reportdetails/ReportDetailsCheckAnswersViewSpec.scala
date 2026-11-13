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

package views.manual.reportdetails

import base.SpecBase
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.ReportDetailsCheckAnswersView

class ReportDetailsCheckAnswersViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: ReportDetailsCheckAnswersView                        = application.injector.instanceOf[ReportDetailsCheckAnswersView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "ReportDetailsCheckAnswersView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(SummaryList(), "TestFI Name")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Check your report details for the financial institution")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Check your report details for TestFI Name")
      }

      "must display warning" in {
        doc.select(".govuk-warning-text").text() must
          include(
            "! Warning You will not be able to change these details after creating the draft. To use different details later, you must create a new draft."
          )
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Create draft"
      }

    }

  }
}
