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
import models.viewModels.SendAReportSections
import models.viewModels.TaskStatus._
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.SendAReportView

class SendAReportViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: SendAReportView                                      = application.injector.instanceOf[SendAReportView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val model = SendAReportSections(
    reportDetails = Some(NotStarted),
    financialInstitutionDetails = Some(NotStarted),
    sponsorDetails = Some(NotStarted),
    filerCategory = Some(NotStarted),
    accounts = Some(NotStarted),
    accountHolders = Some(NoStatus),
    controllingPersons = Some(Completed),
    tbc1 = Some(Incomplete),
    tbc2 = Some(Incomplete)
  )

  "SendAReportView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(model)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Manual journey index page - design TBC")
      }

      "must display heading" in {
        doc.select("h1").text() mustBe "Manual journey index page - design TBC"
      }

      "must display draft saved paragraph" in {
        doc.text() must include(
          "This draft will be saved from 28 days of the date you last saved any manual report information for any financial institution"
        )
      }

      "must display section headings" in {
        val headings = doc.select("h2").eachText()

        headings must contain("Set up the report")
        headings must contain("Check the reporting financial institution details")
        headings must contain("Add the sponsor (optional)")
        headings must contain("Provide the filer category")
        headings must contain("Add accounts information")
      }

      "must display task list rows" in {
        val pageText = doc.text()

        pageText must include("Report details")
        pageText must include("Reporting financial institution details")
        pageText must include("Sponsor details")
        pageText must include("Filer-category")
        pageText must include("Accounts")
        pageText must include("Account holders")
        pageText must include("Controlling persons or substantial owners")
        pageText must include("TBC")
      }

      "must display statuses" in {
        val pageText = doc.text()

        pageText must include("Not started")
        pageText must include("Completed")
        pageText must include("Incomplete")
      }

      "must display incomplete statuses as blue tags" in {
        doc.select(".govuk-tag.govuk-tag--blue").eachText() must contain("Incomplete")
      }

      "must display action links" in {
        val links = doc.select("a").eachText()

        links must contain("Discard this draft")
        links must contain("Delete this report")
        links must contain("Back to manage reports for FI name")
      }

    }

    "must not display sections when no tasks are present" in {
      val model = SendAReportSections(
        reportDetails = Some(NotStarted),
        financialInstitutionDetails = Some(NotStarted),
        sponsorDetails = None,
        filerCategory = Some(NotStarted),
        accounts = None,
        accountHolders = None,
        controllingPersons = None,
        tbc1 = None,
        tbc2 = None
      )

      val renderedHtml = view(model)
      val doc          = Jsoup.parse(renderedHtml.body)

      doc.text() must include("Set up the report")
      doc.text() must include("Check the reporting financial institution details")
      doc.text() must include("Provide the filer category")

      doc.text() must not include "Add the sponsor (optional)"
      doc.text() must not include "Sponsor details"
      doc.text() must not include "Add accounts information"
      doc.text() must not include "Accounts"
      doc.text() must not include "Account holders"
      doc.text() must not include "Controlling persons or substantial owners"
    }
  }
}
