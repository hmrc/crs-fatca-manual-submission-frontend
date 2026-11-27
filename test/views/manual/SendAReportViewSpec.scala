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

package views.manual

import base.SpecBase
import models.ReportId
import models.SubmissionsConstants.CRS
import models.viewModels.SendAReportSections
import models.viewModels.TaskStatus.*
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.SendAReportView

import scala.jdk.CollectionConverters.*

class SendAReportViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: SendAReportView =
    application.injector.instanceOf[SendAReportView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  implicit private val messages: Messages =
    messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val model = SendAReportSections(
    financialInstitutionDetails = Some(NotStarted),
    sponsorDetails = Some(NotStarted),
    filerCategory = Some(NotStarted),
    accounts = Some(Incomplete),
    accountHolders = Some(NoStatus),
    controllingPersons = Some(Completed),
    userAnswers = emptyUserAnswers.copy(
      lastUpdated = java.time.Instant.parse("2026-11-24T13:00:00Z")
    ),
    reportId = ReportId(CRS, 2025, None, "testFiid"),
    fiName = "Example Bank"
  )

  "SendAReportView" - {

    "should render page components" - {
      val renderedHtml: HtmlFormat.Appendable = view(model)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must startWith("Send a CRS report for the financial institution for 2025")
        doc.title() must not include "Example Bank"
      }

      "must display heading" in {
        doc.select("h1").text() mustBe "Send a CRS report for Example Bank for 2025"
      }

      "must display draft saved paragraph" in {
        doc.text() must include(
          "We’ll save this draft until Tuesday 22 December 2026 at 1:00pm."
        )

        doc.text() must include(
          "If you want to change the type of report, then you’ll need to delete this draft and create a new one."
        )
      }

      "must display section headings" in {
        val headings = doc.select("h2").eachText()

        headings must not contain "Set up the report"
        headings must contain("Check reporting financial institution details")
        headings must contain("Add accounts details")
      }

      "must display task list rows" in {
        val pageText = doc.text()

        pageText must not include "Report details"
        pageText must include("Reporting financial institution details")
        pageText must include("Accounts")
        pageText must include("Account holders")
        pageText must include("Controlling persons")
        pageText must not include "TBC"
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

        links must contain("Delete draft")
        links must not contain "Delete this report"
        links must contain("Back to manage reports for Example Bank")
      }
    }

    "must not display sections when no tasks are present" in {
      val model = SendAReportSections(
        financialInstitutionDetails = Some(NotStarted),
        sponsorDetails = None,
        filerCategory = None,
        accounts = None,
        accountHolders = None,
        controllingPersons = None,
        userAnswers = emptyUserAnswers.copy(
          lastUpdated = java.time.Instant.parse("2026-11-24T13:00:00Z")
        ),
        reportId = ReportId(CRS, 2025, None, "testFIID")
      )

      val renderedHtml = view(model)
      val doc          = Jsoup.parse(renderedHtml.body)

      doc.text() must not include "Set up the report"
      doc.text() must include("Check reporting financial institution details")
      doc.text() must not include "Sponsor details"
      doc.text() must not include "Add accounts details"
      doc.text() must not include "Accounts"
      doc.text() must not include "Account holders"
      doc.text() must not include "Controlling persons or substantial owners"
    }

    "must use the returned timestamp, configured retention and UK daylight-saving time" in {
      val summer = model.copy(
        userAnswers = emptyUserAnswers.copy(
          lastUpdated = java.time.Instant.parse("2026-03-15T12:00:00Z")
        )
      )

      Jsoup.parse(view(summer).body).text() must include("Sunday 12 April 2026 at 1:00pm")

      val refreshed = summer.copy(
        userAnswers = summer.userAnswers.copy(
          lastUpdated = java.time.Instant.parse("2026-03-16T12:00:00Z")
        )
      )

      Jsoup.parse(view(refreshed).body).text() must include("Monday 13 April 2026 at 1:00pm")
    }

    "must render Not required without a link" in {
      val page = Jsoup.parse(view(model.copy(controllingPersons = Some(NotRequired))).body)

      val row = page
        .select(".govuk-task-list__item")
        .asScala
        .find(_.text().contains("Controlling persons"))
        .value

      row.select(".govuk-task-list__status").text() mustBe "Not required"
      row.select("a").size() mustBe 0
    }

    "must render new-draft links in separate paragraphs with the required spacing" in {
      val page = Jsoup.parse(view(model).body)

      val discard = page
        .select("a")
        .asScala
        .find(_.text() == "Delete draft")
        .value

      val back = page
        .select("a")
        .asScala
        .find(_.text() == "Back to manage reports for Example Bank")
        .value

      discard.parent().tagName() mustBe "p"
      discard.parent().hasClass("govuk-!-margin-bottom-2") mustBe true
      back.parent().tagName() mustBe "p"
      page.select("a").eachText() must not contain "Delete this report"

      val expiry = page
        .select("p")
        .asScala
        .find(_.text().startsWith("We’ll save this draft until"))
        .value

      expiry.hasClass("govuk-!-padding-bottom-2") mustBe true
    }

    "must style Not required and Cannot start yet alike and suppress both links" in {
      val page = Jsoup.parse(
        view(
          model.copy(
            accountHolders = Some(CannotStartYet),
            controllingPersons = Some(NotRequired)
          )
        ).body
      )

      Seq("Account holders", "Controlling persons").foreach {
        name =>
          val row = page
            .select(".govuk-task-list__item")
            .asScala
            .find(_.text().contains(name))
            .value

          row.select("a").size() mustBe 0

          row
            .select(".govuk-task-list__status")
            .hasClass("govuk-task-list__status--cannot-start-yet") mustBe true
      }
    }

    "must use nil wording in the heading, page title and Send row" in {
      val nil = model.copy(
        financialInstitutionDetails = None,
        accounts = None,
        accountHolders = None,
        controllingPersons = None,
        isNilReport = true
      )

      val page = Jsoup.parse(view(nil).body)

      page.select("h1").text() mustBe "Send a CRS nil report for Example Bank for 2025"
      page.title() must startWith("Send a CRS nil report for the financial institution for 2025")
      page.select("main h2").text() mustBe "Send the nil report"
      page.select(".govuk-task-list__link").text() mustBe "Send the nil report for Example Bank"
    }
  }
}
