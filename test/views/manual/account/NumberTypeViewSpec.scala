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
import forms.manual.sponsor.HaveSponsorFormProvider
import models.NormalMode
import models.SubmissionsConstants.{CRS, FATCA}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.account.NumberTypeView

class NumberTypeViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: NumberTypeView                                       = application.injector.instanceOf[NumberTypeView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new HaveSponsorFormProvider()
  val form                                                               = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "NumberTypeView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, CRS)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("What type of account number or identification number is this?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("What type of account number or identification number is this?")
      }

      "must display options for CRS" in {
        val elements = doc.select(".govuk-radios__label")
        elements.size() mustBe 6
        elements.get(0).text mustBe "International Bank Account Number (IBAN)"
        elements.get(1).text mustBe "Other Bank Account Number (OBAN)"
        elements.get(2).text mustBe "International Securities Identification Number (ISIN)"
        elements.get(3).text mustBe "Other Securities Identification Number (OSIN)"
        elements.get(4).text mustBe "Specified Electronic Money Product (SEMP)"
        elements.get(5).text mustBe "Any other type of account number or identification number"
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

      "must display options for FATCA" in {
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, FATCA)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)
        val elements                            = doc.select(".govuk-radios__label")
        elements.size() mustBe 5
        elements.get(0).text mustBe "International Bank Account Number (IBAN)"
        elements.get(1).text mustBe "Other Bank Account Number (OBAN)"
        elements.get(2).text mustBe "International Securities Identification Number (ISIN)"
        elements.get(3).text mustBe "Other Securities Identification Number (OSIN)"
        elements.get(4).text mustBe "Any other type of account number or identification number"
      }

    }

  }
}
