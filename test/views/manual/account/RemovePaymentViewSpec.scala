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
import forms.manual.account.{PaymentTypeFormProvider, RemovePaymentFormProvider}
import models.{Currency, NormalMode}
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.account.PaymentType.CRSInterest
import models.manual.account.{AccountPayment, AccountPaymentsAmount, PaymentType}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.account.{PaymentTypeView, RemovePaymentView}

class RemovePaymentViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: RemovePaymentView                                    = application.injector.instanceOf[RemovePaymentView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new RemovePaymentFormProvider()
  val form                                                               = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))
  val currency                                          = Currency(code = "VED", displayName = "Venezuelan Bolivar (VED)")
  val accountPayment                                    = AccountPayment(CRSInterest, Some(AccountPaymentsAmount(currency, "1000")))

  "RemovePaymentView" - {

    "should render page components" - {

      val regimeType                          = CRS
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, accountPayment)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display CRS title" in {
        doc.title() must include("Are you sure you want to remove this 1,000 VED interest payment for this account?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Are you sure you want to remove this 1,000 VED interest payment for this account?")
      }

      "must contain correct in submit button" in {
        doc.select("#submit").text() must include("Save and continue")
      }

      "must display correct radio buttons value" in {
        doc.select(".govuk-radios__label").text().trim().contains("Yes")
        doc.select(".govuk-radios__label").text().trim().contains("No")
        doc.select(".govuk-radios__input").text().trim().contains("true")
        doc.select(".govuk-radios__input").text().trim().contains("false")
      }

    }

  }

}
