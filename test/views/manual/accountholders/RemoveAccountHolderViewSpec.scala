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
import forms.manual.accountHolders.RemoveAccountHolderFormProvider
import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.accountHolders.RemoveAccountHolderView

import scala.jdk.CollectionConverters.*

class RemoveAccountHolderViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: RemoveAccountHolderView = application.injector.instanceOf[RemoveAccountHolderView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val formProvider        = new RemoveAccountHolderFormProvider()
  private val form: Form[Boolean] = formProvider()

  private val accountHolderName = "Alpha Holdings"

  private def render(
                      boundForm: Form[_] = form,
                      mode: Mode = NormalMode,
                      regime: String = "FATCA",
                      name: String = accountHolderName
                    ): Document =
    Jsoup.parse(view(boundForm, mode, regime, name).body)

  private def submitPath(mode: Mode): String =
    controllers.manual.accountHolders.routes.RemoveAccountHolderController.onSubmit(mode).url

  "RemoveAccountHolderView" - {

    "page title" - {

      "must name the page" in {
        render().title() mustBe
          "Remove this account holder - Send a CRS or FATCA report - GOV.UK"
      }
    }

    "page heading" - {

      "must render a single h1" in {
        render().select("h1").size() mustBe 1
      }

      "must name the page" in {
        render().select("h1").text() mustBe "Remove this account holder"
      }
    }

    "guidance" - {

      "must say what has to be in place before the report can be sent" in {
        render().select("p.govuk-body").eachText().asScala.toList must contain(
          "Before you can send your report, you must make sure that each:"
        )
      }

      "must list the requirements using the substantial owner wording for a fatca report" in {
        render(regime = "FATCA").select("ul.govuk-list--bullet li").eachText().asScala.toList mustBe List(
          "account has at least one account holder",
          "substantial owner links to at least one account holder"
        )
      }

      "must list the requirements using the controlling person wording for a crs report" in {
        val bullets = render(regime = "CRS").select("ul.govuk-list--bullet li").eachText().asScala.toList

        bullets must contain("account has at least one account holder")
        bullets must contain("controlling person links to at least one account holder")
        bullets must not contain "substantial owner links to at least one account holder"
      }
    }

    "confirmation radios" - {

      "must ask for confirmation naming the account holder" in {
        render().select(".govuk-fieldset__legend--m").text() mustBe
          "Are you sure you want to remove the account holder Alpha Holdings plc?"
      }

      "must use the supplied account holder name" in {
        render(name = "Beta Ltd").select(".govuk-fieldset__legend--m").text() mustBe
          "Are you sure you want to remove the account holder Beta Ltd plc?"
      }

      "must style the legend as a medium heading" in {
        val legend = render().select(".govuk-fieldset__legend--m")

        legend.size() mustBe 1
        legend.hasClass("wrappable") mustBe true
      }

      "must render a yes and a no option bound to the value field" in {
        val radios = render().select("input[type=radio]")

        radios.size() mustBe 2
        radios.eachAttr("name").asScala.toSet mustBe Set("value")
        radios.eachAttr("value").asScala.toList mustBe List("true", "false")
      }

      "must label the options Yes and No" in {
        val doc = render()

        doc.select("label[for=value]").text() mustBe "Yes"
        doc.select("label[for=value-no]").text() mustBe "No"
      }
      
    }

    "submit button" - {

      "must render a single save and continue button inside the form" in {
        val doc = render()

        doc.select("form button.govuk-button").size() mustBe 1
        doc.select(".govuk-button").text() mustBe "Save and continue"
      }
    }
    
  }
}
