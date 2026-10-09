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
import forms.manual.account.WhatIsAccountNumberFormProvider
import models.NormalMode
import models.NumberType.Isin
import models.SubmissionsConstants.CRS
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.account.WhatIsAccountNumberView

class WhatIsAccountNumberViewSpec extends SpecBase {

  private val application                                                = applicationBuilder().build()
  private val regime                                                     = CRS
  private val numType                                                    = Isin
  private val view: WhatIsAccountNumberView                              = application.injector.instanceOf[WhatIsAccountNumberView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val form                                                               = new WhatIsAccountNumberFormProvider()(numType, regime)

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "WhatIsTheAccountNumberView" - {

    "should render page components" - {

      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "IBAN")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("What is the account or identification number?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("What is the account or identification number?")
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

      "hint" - {
        "must exist for IBAN" in {
          val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "IBAN")
          lazy val doc                            = Jsoup.parse(renderedHtml.body)
          doc.select("#value-hint").text() must include("For example")
        }

        "must exist for ISIN" in {
          val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, "ISIN")
          lazy val doc                            = Jsoup.parse(renderedHtml.body)
          doc.select("#value-hint").text() must include("For example")
        }

        "must not exist for other types" - {
          List("OBAN", "OSIN", "SEMP", "OTHER") foreach {
            numberType =>
              s"must not exist for $numberType" in {
                val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, numberType)
                lazy val doc                            = Jsoup.parse(renderedHtml.body)
                doc.select("#value-hint") must be(empty)
              }
          }
        }
      }

    }

  }
}
