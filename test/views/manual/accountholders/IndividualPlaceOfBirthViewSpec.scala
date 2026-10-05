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

package views.manual.accountholders

import base.SpecBase
import forms.manual.accountHolders.IndividualPlaceOfBirthFormProvider
import models.SubmissionsConstants.CRS
import models.{Countries, NormalMode}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.accountHolders.IndividualPlaceOfBirthView

class IndividualPlaceOfBirthViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: IndividualPlaceOfBirthView                           = application.injector.instanceOf[IndividualPlaceOfBirthView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new IndividualPlaceOfBirthFormProvider()
  implicit private val request: FakeRequest[AnyContent]                  = FakeRequest()
  implicit private val messages: Messages                                = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "IndividualPlaceOfBirthView" - {

    "display correct information" - {
      val form                                = formProvider()
      val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, Countries.allCountries(CRS), "test name")
      lazy val doc                            = Jsoup.parse(renderedHtml.body)

      "must display title" in {
        doc.title() must include("What is the account holder’s place of birth?")
      }

      "must display heading" in {
        doc.select("h1").text() must include("What is the place of birth for test name?")
      }

      "must display all address fields with correct autocomplete attributes" in {
        val expectedAutocompleteAttributes = Map(
          "city"   -> "address-level2",
          "region" -> "address-level1"
        )

        expectedAutocompleteAttributes.foreach {
          case (fieldId, expectedValue) =>
            val actualValue = doc.select(s"#$fieldId").attr("autocomplete")
            actualValue mustBe expectedValue
        }
      }

      "must display button" in {
        doc.select("#submit").text() mustBe "Save and continue"
      }

    }

  }

}
