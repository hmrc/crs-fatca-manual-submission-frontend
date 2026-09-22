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

package views.manual.cpso

import base.SpecBase
import forms.IndividualNameFormProvider
import forms.manual.cpso.CpsoSelfCertificationFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.cpso.{CpsoSelfCertificationView, IndividualNameView}

class CpsoSelfCertificationViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: CpsoSelfCertificationView                            = application.injector.instanceOf[CpsoSelfCertificationView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new CpsoSelfCertificationFormProvider()
  implicit private val request: FakeRequest[AnyContent]                  = FakeRequest()
  implicit private val messages: Messages                                = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "CpsoSelfCertificationView" - {
    "should render page components" - {
      val reportingPeriod   = 2025
      val controllingPerson = "Some name"

      val form                                = formProvider()
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, reportingPeriod, controllingPerson)
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("Has the controlling person provided a valid self-certification?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("Has Some name provided a valid self-certification?")
      }

      "radio should display correct label and value" in {
        doc.select(".govuk-radios__input").iterator().forEachRemaining {
          elem =>
            Seq("CRS1000", "CRS1001", "CRS1002").contains(elem.getElementsByAttribute("value").attr("value")) mustEqual true
        }

        doc.select(".govuk-radios__label").iterator().forEachRemaining {
          elem =>
            Seq("Yes", "No", "Not reported").contains(elem.text().trim) mustEqual true
        }
      }

      "submit button contains correct text" in {
        doc.select("button").text() mustEqual "Save and continue"
      }

      "two radios should be displayed for reporting periods from 2026" in {
        val renderedHtml2: HtmlFormat.Appendable = view(form, NormalMode, 2026, controllingPerson)
        lazy val doc2                            = Jsoup.parse(renderedHtml2.body)
        doc2.select(".govuk-radios__input").size() mustEqual 2
        doc2.select(".govuk-radios__input").iterator().forEachRemaining {
          elem =>
            Seq("CRS1001", "CRS1002").contains(elem.getElementsByAttribute("value").attr("value")) mustEqual true
            elem.getElementsByAttribute("value").attr("value") mustNot contain("CRS1000")
        }
      }
    }
  }
}
