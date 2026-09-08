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
import play.api.mvc.ControllerHelpers.request2flash
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.account.{AccountPaymentsView, PaymentTypeView, RemovePaymentView}

class AccountPaymentsViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: AccountPaymentsView                                  = application.injector.instanceOf[AccountPaymentsView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new RemovePaymentFormProvider()
  val form                                                               = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  val currency           = Currency(code = "VED", displayName = "Venezuelan Bolivar (VED)")
  val accountPaymentList = Seq(AccountPayment(CRSInterest, Some(AccountPaymentsAmount(currency, "1000"))))
  val emptyAccountList   = Seq.empty
  val crs                = "crs"
  val fatca              = "fatca"
  val reportingPeriod    = "2025"

  "AccountPaymentsView" - {

    "should render page components for crs" - {

      "when account payments are present" - {
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, accountPaymentList, reportingPeriod, crs)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)

        "must display CRS title" in {
          doc.title() must include("You have added 1 payment made to this account")
        }

        "must display heading" in {
          doc.select("h1").text() must include("You have added 1 payments made to this account")
        }

        "must contain the correct payment info in table" in {
          doc.select(".govuk-summary-list__row").text() must include("1,000 VED interest")
          doc.select(".govuk-summary-list__row").text() must include("Change")
          doc.select(".govuk-summary-list__row").text() must include("Change 1,000 VED interest")
          doc.select(".govuk-summary-list__row").text() must include("Remove")
          doc.select(".govuk-summary-list__row").text() must include("Remove 1,000 VED interest")
        }

        "must contain correct title in legend" in {
          doc.select("legend.govuk-fieldset__legend").text() must include("Do you need to add more payments for 2025?")
        }

        "must display correct radio buttons value" in {
          doc.select(".govuk-radios__label").text().trim().contains("Yes")
          doc.select(".govuk-radios__label").text().trim().contains("No")
          doc.select(".govuk-radios__input").text().trim().contains("true")
          doc.select(".govuk-radios__input").text().trim().contains("false")
        }
      }

      "when account payments are not resent" - {
        val regimeType                          = CRS
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, emptyAccountList, reportingPeriod, crs)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)

        "must display CRS title" in {
          doc.title() must include("You have not added any payments made to this account")
        }

        "must display heading" in {
          doc.select("h1").text() must include("You have not added any payments made to this account")
        }

        "must contain correct title in legend" in {
          doc.select("legend.govuk-fieldset__legend").text() must include("Do you need to add payments for 2025?")
        }
      }
    }

    "should render page components for fatca" - {
      "when account payments are present" - {
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, accountPaymentList, reportingPeriod, fatca)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)

        "must display fatca title" in {
          doc.title() must include("You have added 1 payment made to this account, a payee or an owner")
        }

        "must display heading" in {
          doc.select("h1").text() must include("You have added 1 payments made to this account")
        }

        "must contain correct title in legend" in {
          doc.select("legend.govuk-fieldset__legend").text() must include("Do you need to add more payments for 2025?")
        }
      }

      "when account payments are not resent" - {
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, emptyAccountList, reportingPeriod, fatca)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)

        "must display fatca title" in {
          doc.title() must include("You have not added any payments made to this account, a payee or an owner")
        }

        "must display heading" in {
          doc.select("h1").text() must include("You have not added any payments made to this account, a payee or an owner")
        }

        "must contain correct title in legend" in {
          doc.select("legend.govuk-fieldset__legend").text() must include("Do you need to add payments for 2025?")
        }
      }
    }

  }

}
