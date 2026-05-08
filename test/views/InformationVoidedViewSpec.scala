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
import models.viewModels.InformationVoidedViewModel
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.InformationVoidedView

class InformationVoidedViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: InformationVoidedView                                = application.injector.instanceOf[InformationVoidedView]
  private val frontendAppConfig: FrontendAppConfig                       = application.injector.instanceOf[FrontendAppConfig]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val fiName      = "Test Financial Institution"
  private val fiId        = "FI123"
  private val dateTime    = "12 May 2026 at 10:30am"
  private val emailString = "email@test.com"

  private def buildModel(messageRefIds: Seq[String]): InformationVoidedViewModel =
    InformationVoidedViewModel(
      fiName = fiName,
      dateTime = dateTime,
      messageRefIds = messageRefIds,
      emailString = emailString,
      fiId = fiId
    )

  "InformationVoidedView" - {

    "when there is a single report voided" - {

      val renderedHtml: HtmlFormat.Appendable = view(buildModel(Seq("testMesRefId001")))
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display the panel heading" in {
        doc.select(".govuk-panel__title").text() mustEqual messages("informationVoided.heading")
      }

      "must display the dateTime in the panel body" in {
        doc.body().text() must include(dateTime)
      }

      "must display the email addresses para" in {
        doc.body().text() must include(emailString)
      }

      "must display subheading" in {
        doc.select("h2").text() must include(messages("informationVoided.subheading"))
      }

      "must display paragraph with the FI name" in {
        doc.body().text() must include(messages("informationVoided.para.2", fiName))
      }

      "must display the messageRefId" in {
        doc.body().text() must include("testMesRefId001")
      }

      "must not render a bullet list" in {
        doc.select("ol.govuk-list--bullet").isEmpty mustBe true
      }

      "must display the submitted reports link" in {
        val link = doc.select("#submitted-reports-link")
        link.text() mustEqual messages("informationVoided.link.1", fiName)
        link.attr("href") mustEqual controllers.routes.ReadSubmissionDataController.onPageLoad(fiId, fiName).url
      }

      "must display the manage reports link" in {
        val link = doc.select("#manage-reports-link")
        link.text() mustEqual "Back to manage your CRS and FATCA reports"
        link.attr("href") mustEqual frontendAppConfig.manageReportsLink
      }
    }

    "when there are multiple reports voided" - {

      val messageRefIds                       = Seq("testMesRefId001", "testMesRefId002", "testMesRefId003")
      val renderedHtml: HtmlFormat.Appendable = view(buildModel(messageRefIds))
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display the multiple reports paragraph with the FI name" in {
        doc.body().text() must include(messages("informationVoided.para.3", fiName))
      }

      "must contain every messageRefId as a list item" in {
        val listItems = doc.select("ol.govuk-list.govuk-list--bullet li").eachText()
        messageRefIds.foreach {
          id =>
            listItems must contain(id)
        }
      }
    }
  }
}
