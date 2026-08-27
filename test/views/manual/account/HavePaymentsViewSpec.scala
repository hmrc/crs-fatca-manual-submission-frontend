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

package views.manual.account

import base.SpecBase
import forms.manual.account.HavePaymentsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.account.HavePaymentsView
import models.SubmissionsConstants.{CRS, FATCA}

class HavePaymentsViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: HavePaymentsView                                     = application.injector.instanceOf[HavePaymentsView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))
  val reportingPeriod                                   = "2025"

  "HavePaymentsView" - {

    "should render page components for CRS" - {
      val regimeType                          = CRS
      val form                                = new HavePaymentsFormProvider()(regimeType, reportingPeriod)
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, regimeType, reportingPeriod)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display CRS title" in {
        doc.title() must include("Were any payments made to this account in 2025?")
      }

      "must display CRS heading" in {
        doc.select("h1").text() must include("Were any payments made to this account in 2025?")
      }

      "must display CRS radio labels" in {
        Seq("Yes", "No").foreach(
          lbl => doc.select(".govuk-radios__label").text() must include(lbl)
        )
      }

      "must display CRS button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

    "should render page components for FATCA" - {
      val regimeType                          = FATCA
      val form                                = new HavePaymentsFormProvider()(regimeType, reportingPeriod)
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, regimeType, reportingPeriod)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display FATCA title" in {
        doc.title() must include("Were any payments made to this account, a payee or an owner in 2025?")
      }

      "must display FATCA heading" in {
        doc.select("h1").text() must include("Were any payments made to this account, a payee or an owner in 2025?")
      }

      "must display FATCA radio labels" in {
        Seq("Yes", "No").foreach(
          lbl => doc.select(".govuk-radios__label").text() must include(lbl)
        )
      }

      "must display FATCA button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

  }
}
