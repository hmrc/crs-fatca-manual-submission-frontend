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
import models.NormalMode
import models.SubmissionsConstants.{CRS, FATCA}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.cpso.IndividualNameView

class IndividualNameViewSpec extends SpecBase {
  private val application = applicationBuilder().build()

  private val view: IndividualNameView                                   = application.injector.instanceOf[IndividualNameView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new IndividualNameFormProvider()
  implicit private val request: FakeRequest[AnyContent]                  = FakeRequest()
  implicit private val messages: Messages                                = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  val expectedTitleLabels = Seq(
    "First name",
    "Last name"
  )

  "IndividualNameView" - {
    "should render page components" - {
      "for crs regime" - {
        val regime                              = "crs"
        val form                                = formProvider(CRS)
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, regime)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)

        "must display title" in {
          doc.title() must include("What is the name of the controlling person?")
        }

        "must display heading" in {
          doc.select("h1").text() must include("What is the name of the controlling person?")
        }

        "must display all address fields" in {
          val labels = doc.select(".govuk-label").eachText()
          expectedTitleLabels.foreach {
            label =>
              labels must contain(label.trim())
          }
        }

        "must display all address fields with correct autocomplete attributes" in {
          val expectedAutocompleteAttributes = Map(
            "firstName" -> "given-name",
            "lastName"  -> "family-name"
          )

          expectedAutocompleteAttributes.foreach {
            case (fieldId, expectedValue) =>
              val actualValue = doc.select(s"#$fieldId").attr("autocomplete")
              actualValue mustBe expectedValue
          }
        }
      }

      "for fatca regime" - {
        val regime                              = "fatca"
        val form                                = formProvider(FATCA)
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, regime)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)

        "must display title" in {
          doc.title() must include("What is the name of the substantial owner?")
        }

        "must display heading" in {
          doc.select("h1").text() must include("What is the name of the substantial owner?")
        }

        "must display all address fields" in {
          val labels = doc.select(".govuk-label").eachText()
          expectedTitleLabels.foreach {
            label =>
              labels must contain(label.trim())
          }
        }

        "must display all address fields with correct autocomplete attributes" in {
          val expectedAutocompleteAttributes = Map(
            "firstName" -> "given-name",
            "lastName"  -> "family-name"
          )

          expectedAutocompleteAttributes.foreach {
            case (fieldId, expectedValue) =>
              val actualValue = doc.select(s"#$fieldId").attr("autocomplete")
              actualValue mustBe expectedValue
          }
        }
      }
    }
  }
}
