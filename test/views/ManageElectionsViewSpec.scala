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
import config.FrontendAppConfig
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import services.ElectionsRows
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}
import views.html.ManageElectionsView

class ManageElectionsViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: ManageElectionsView                                  = application.injector.instanceOf[ManageElectionsView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val fiName        = "Test Financial Institution"
  private val fiId          = "FI123"
  private val selectedYear  = 2025
  private val electionYears = Seq(2024, 2025, 2026)

  private val emptyElectionsRows = ElectionsRows(SummaryList(Nil), SummaryList(Nil))

  val renderedHtml: HtmlFormat.Appendable = view(electionYears, emptyElectionsRows, selectedYear, fiName, fiId)
  lazy val doc                            = Jsoup.parse(renderedHtml.body)

  "ManageElectionsView" - {
    "must display the page title" in {
      doc.title() must include(messages("manageElections.title", selectedYear.toString))
    }

    "must display the heading with FI name and year" in {
      doc.select("h1").text() must include(fiName)
      doc.select("h1").text() must include(selectedYear.toString)
    }

    "must display the CRS subheading" in {
      doc.select("h2").text() must include(messages("manageElections.subheading.crs", selectedYear.toString))
    }

    "must display the FATCA subheading" in {
      doc.select("h2").text() must include(messages("manageElections.subheading.fatca", selectedYear.toString))
    }

    "must display the submitted reports link with FI name" in {
      doc.body().text() must include(messages("manageElections.back.reports", fiName))
    }

    "must display the manage reports link" in {
      doc.body().text() must include(messages("manageElections.back.manageReports"))
    }

    "must not render CRS summary list" in {
      doc.select(".govuk-summary-list").isEmpty mustBe true
    }

    "when CRS and FATCA rows are empty" - {

      "must display no elections paragraph for CRS" in {
        doc.body().text() must include(messages("manageElections.noelections"))
      }

      "must display add CRS election link" in {
        doc.body().text() must include(messages("manageElections.noelections.crs"))
      }

      "must display add FATCA election link" in {
        doc.body().text() must include(messages("manageElections.noelections.fatca"))
      }
    }

    "when CRS and FATCA rows are populated" - {

      val populatedElectionsRows = ElectionsRows(
        SummaryList(rows = Seq(SummaryListRow(key = Key(Text("Test CRS Key"))))),
        SummaryList(rows = Seq(SummaryListRow(key = Key(Text("Test FATCA Key")))))
      )

      val renderedHtml: HtmlFormat.Appendable = view(electionYears, populatedElectionsRows, selectedYear, fiName, fiId)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must render CRS summary list" in {
        doc.select(".govuk-summary-list").isEmpty mustBe false
      }

      "must not display the no elections paragraph" in {
        doc.body().text() must not include messages("manageElections.noelections")
      }
    }
  }
}
