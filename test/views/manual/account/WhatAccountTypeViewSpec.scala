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
import forms.manual.account.WhatAccountTypeFormProvider
import models.manual.account.WhatAccountType
import models.{NormalMode, NumberType}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import utils.ReportingConstants.REPORTING_THRESHOLD_YEAR
import views.html.manual.account.WhatAccountTypeView

class WhatAccountTypeViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: WhatAccountTypeView                                  = application.injector.instanceOf[WhatAccountTypeView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val form                                                               = new WhatAccountTypeFormProvider()()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))
  
  "WhatAccountTypeView" - {

    "should render page components" - {

      val items: Seq[RadioItem]               = WhatAccountType.options(NumberType.Other, 2025)
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, items)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("What type of account is this?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("What type of account is this?")
      }

      "must display all account type options" in {
        val elements = doc.select(".govuk-radios__label")
        elements.size() mustBe 5
        elements.get(0).text mustBe messages("whatAccountType.Depository")
        elements.get(1).text mustBe messages("whatAccountType.Custodial")
        elements.get(2).text mustBe messages("whatAccountType.InvestmentEntity")
        elements.get(3).text mustBe messages("whatAccountType.InsuranceOrAnnuityContract")
        elements.get(4).text mustBe messages("whatAccountType.NotReported")
      }
      

      "must display the submit button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }
    }

    "must render only the base options when the number type is not Other and the reporting period is at or after the threshold year" in {
      val items    = WhatAccountType.options(NumberType.Iban, REPORTING_THRESHOLD_YEAR)
      val html     = view(form, NormalMode, items)(request, messages)
      val doc      = Jsoup.parse(html.body)
      val elements = doc.select(".govuk-radios__label")

      elements.size() mustBe WhatAccountType.baseValues.size
      WhatAccountType.baseValues.zipWithIndex.foreach {
        case (value, i) => elements.get(i).text mustBe messages(s"whatAccountType.${value.toString}")
      }
    }

    "must add the InsuranceOrAnnuityContract option for a non-Other number type before the threshold year" in {
      val items    = WhatAccountType.options(NumberType.Iban, REPORTING_THRESHOLD_YEAR - 1)
      val html     = view(form, NormalMode, items)(request, messages)
      val doc      = Jsoup.parse(html.body)
      val elements = doc.select(".govuk-radios__label")

      elements.size() mustBe WhatAccountType.baseValues.size + 1
      elements.last.text mustBe messages("whatAccountType.NotReported")
    }
  }
}
