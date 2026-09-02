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
import forms.manual.account.PaymentTypeFormProvider
import models.NormalMode
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.account.PaymentType
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.account.PaymentTypeView

class PaymentTypeViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: PaymentTypeView                                      = application.injector.instanceOf[PaymentTypeView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new PaymentTypeFormProvider()
  val form                                                               = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "PaymentTypeView" - {

    "should render page components for CRS" - {

      val regimeType                          = CRS
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, regimeType, showDividendsAndInterestRadioFields = true)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display CRS title" in {
        doc.title() must include("What type of payments were these for this account?")
      }

      "must display CRS heading" in {
        doc.select("h1").text() must include("What type of payments were these for this account?")
      }

      "must display CRS payment type options when showDividendsAndInterestRadioFields is true" in {
        val elements = doc.select(".govuk-radios__label")
        elements.size() mustBe PaymentType.crsValues.size
        PaymentType.crsValues.zipWithIndex.foreach {
          case (value, index) =>
            elements.get(index).text mustBe messages(s"account.paymentType.${value.toString}")
        }
      }

      "must display CRS payment type options when showDividendsAndInterestRadioFields is false" in {
        val renderedHtml2: HtmlFormat.Appendable = view(form, NormalMode, regimeType, showDividendsAndInterestRadioFields = false)
        lazy val doc2                            = Jsoup.parse(renderedHtml2.body)

        val elements = doc2.select(".govuk-radios__label")
        elements.size() mustBe PaymentType.crsNonCashValueInsuranceValues.size
        PaymentType.crsNonCashValueInsuranceValues.zipWithIndex.foreach {
          case (value, index) =>
            elements.get(index).text mustBe messages(s"account.paymentType.${value.toString}")
        }
      }

      "must display the submit button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }
    }

    "should render page components for FATCA" - {

      val regimeType                          = FATCA
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, regimeType, showDividendsAndInterestRadioFields = true)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display FATCA title" in {
        doc.title() must include("What type of payments were these for this account?")
      }

      "must display FATCA heading" in {
        doc.select("h1").text() must include("What type of payments were these for this account?")
      }

      "must display FATCA payment type options" in {
        val elements = doc.select(".govuk-radios__label")
        elements.size() mustBe PaymentType.fataValues.size
        PaymentType.fataValues.zipWithIndex.foreach {
          case (value, index) =>
            elements.get(index).text mustBe messages(s"account.paymentType.${value.toString}")
        }
      }

      "must display the submit button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }
    }

  }

}
