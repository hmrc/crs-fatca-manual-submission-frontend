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

package views.manual.sponsor

import base.SpecBase
import models.ReportId
import models.SubmissionsConstants.CRS
import models.response.Country
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import pages.ReportIdPage
import pages.manual.FINamePage
import pages.manual.sponsor.{HaveSponsorPage, SponsorNamePage, TaxResidentCountriesListPage, WhatIsGIINForSponsorPage}
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import viewmodels.checkAnswers.manual.sponsor.CheckAnswersSummary
import views.html.manual.sponsor.CheckAnswersView

class CheckAnswersViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: CheckAnswersView                                     = application.injector.instanceOf[CheckAnswersView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))
  implicit val reportId: ReportId                       = ReportId(CRS, 2025, None, "testFiID")
  val fiName                                            = "testFiName"

  "CheckAnswersView" - {

    "should render page components with basic" - {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(HaveSponsorPage(), false)
        .withPage(FINamePage(), fiName)
      val checkAnswerSummary = CheckAnswersSummary(ua)

      val renderedHtml: HtmlFormat.Appendable = view(checkAnswerSummary, "TestFIName")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Check your answers for the sponsor for the financial institution")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Check your answers for the sponsor for TestFIName")
      }

      "must not display summary card" in {
        doc.getElementsByClass("govuk-summary-card") mustBe Elements()
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Continue"
      }

    }
    "should render page components with summary card" - {
      val ua = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(HaveSponsorPage(), true)
        .withPage(SponsorNamePage(), "Test Sponsor")
        .withPage(WhatIsGIINForSponsorPage()(), "Test GIIN")
        .withPage(TaxResidentCountriesListPage(), Seq(Country("XX", "Test Country")))
        .withPage(FINamePage(), fiName)
      val checkAnswerSummary = CheckAnswersSummary(ua)

      val renderedHtml: HtmlFormat.Appendable = view(checkAnswerSummary, "TestFIName")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must not display summary card" in {
        doc.getElementsByClass("govuk-summary-card").text() must include("Test Country")
      }

    }

  }
}
