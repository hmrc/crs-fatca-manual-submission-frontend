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
import forms.manual.accountHolders.AccountHolderListFormProvider
import models.manual.accountHolders.AccountHolderTypes.{PassiveNFFEWithSubstantial, PassiveNonFinancialWithOneOrMoreControllingPerson}
import models.manual.accountHolders.IndividualOrOrganisation.Organisation
import models.viewModels.{AccountHolder, AccountHolderId}
import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, Flash, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.accountHolders.AccountHolderListView

import scala.jdk.CollectionConverters.*

class AccountHolderListViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: AccountHolderListView = application.injector.instanceOf[AccountHolderListView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val formProvider        = new AccountHolderListFormProvider()
  private val form: Form[Boolean] = formProvider()

  private val fiName = "First FI"

  private val alpha = AccountHolderId("21") -> AccountHolder(
    individualOrOrganisation = Some(Organisation),
    organisationName = Some("Alpha Holdings"),
    accountHolderType = Some(PassiveNonFinancialWithOneOrMoreControllingPerson)
  )

  private val beta = AccountHolderId("22") -> AccountHolder(
    individualOrOrganisation = Some(Organisation),
    organisationName = Some("Beta Holdings"),
    accountHolderType = Some(PassiveNFFEWithSubstantial)
  )

  private val oneAccount  = Seq("GB11AAAA11111111111111")
  private val twoAccounts = Seq("GB11AAAA11111111111111", "GB22BBBB22222222222222")

  private def render(
    boundForm: Form[_] = form,
    mode: Mode = NormalMode,
    holders: Seq[(AccountHolderId, AccountHolder)] = Seq(alpha),
    accountsWithoutHolders: Seq[String] = oneAccount,
    regime: String = "CRS",
    flash: Flash = Flash.emptyCookie
  ): Document =
    Jsoup.parse(view(boundForm, mode, fiName, holders, accountsWithoutHolders, regime)(request, messages, flash).body)

  private def pageHeading(doc: Document): String =
    doc.select("h1.govuk-heading-l").text()

  private def paragraphTexts(doc: Document): List[String] =
    doc.select("p.govuk-body").eachText().asScala.toList

  private def linkWithText(doc: Document, text: String) =
    doc.select("a").asScala.find(_.text() == text)

  private def addAccountHolderPath(mode: Mode): String =
    controllers.manual.accountHolders.routes.AccountController.onPageLoad(mode).url

  "AccountHolderListView" - {

    "page title" - {

      "must say no account holders have been added when there are none" in {
        render(holders = Seq.empty, accountsWithoutHolders = Seq.empty).title() mustBe
          "You have not added any account holders - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the singular for one account holder" in {
        render(holders = Seq(alpha)).title() mustBe
          "You have added 1 account holder - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the plural for more than one account holder" in {
        render(holders = Seq(alpha, beta)).title() mustBe
          "You have added 2 account holders - Send a CRS or FATCA report - GOV.UK"
      }
    }

    "page heading" - {

      "must say no account holders have been added when there are none" in {
        pageHeading(render(holders = Seq.empty, accountsWithoutHolders = Seq.empty)) mustBe
          "You have not added any account holders"
      }

      "must use the singular for one account holder" in {
        pageHeading(render(holders = Seq(alpha))) mustBe "You have added 1 account holder"
      }

      "must use the plural for more than one account holder" in {
        pageHeading(render(holders = Seq(alpha, beta))) mustBe "You have added 2 account holders"
      }
    }

    "when there are no account holders and no accounts without one" - {

      "must not render the account holders table" in {
        render(holders = Seq.empty, accountsWithoutHolders = Seq.empty).select(".govuk-summary-list").size() mustBe 0
      }

      "must not render any radios or buttons" in {
        val doc = render(holders = Seq.empty, accountsWithoutHolders = Seq.empty)

        doc.select("input[type=radio]").size() mustBe 0
        doc.select(".govuk-button").size() mustBe 0
      }
    }

    "one account without an account holder" - {

      "must name the account in the warning" in {
        paragraphTexts(render(holders = Seq.empty, accountsWithoutHolders = oneAccount)) must contain(
          "The account GB11AAAA11111111111111 is missing an account holder."
        )
      }

      "must list the options for what to do" in {
        val doc = render(holders = Seq.empty, accountsWithoutHolders = oneAccount)

        paragraphTexts(doc) must contain("You must either:")
        doc.select("ul.govuk-list--bullet li").eachText().asScala.toList mustBe List(
          "change or add at least one account holder to link to the account",
          "remove the account"
        )
      }

      "must link to adding an account holder in normal mode" in {
        val links = render(holders = Seq.empty, accountsWithoutHolders = oneAccount).select("a.govuk-button")

        links.size() mustBe 1
        links.text() mustBe "Add an account holder"
        links.attr("href") mustBe addAccountHolderPath(NormalMode)
      }

      "must link to adding an account holder in check mode" in {
        val href = render(holders = Seq.empty, accountsWithoutHolders = oneAccount, mode = CheckMode)
          .select("a.govuk-button")
          .attr("href")

        href mustBe addAccountHolderPath(CheckMode)
      }

      "must link back to the send a report page" in {
        val doc = render(holders = Seq.empty, accountsWithoutHolders = oneAccount)

        linkWithText(doc, s"Back to send a CRS report for $fiName").map(_.attr("href")) mustBe
          Some(controllers.manual.routes.SendAReportController.onPageLoad().url)
      }
    }

    "more than one account without an account holder" - {

      "must list every account without an account holder" in {
        val doc  = render(holders = Seq.empty, accountsWithoutHolders = twoAccounts)
        val list = doc.select("ul.govuk-list--bullet li").eachText().asScala.toList

        paragraphTexts(doc) must contain("These accounts are missing an account holder:")
        list must contain("GB11AAAA11111111111111")
        list must contain("GB22BBBB22222222222222")
      }

      "must link to adding an account holder" in {
        val links = render(holders = Seq.empty, accountsWithoutHolders = twoAccounts).select("a.govuk-button")

        links.size() mustBe 1
        links.text() mustBe "Add an account holder"
        links.attr("href") mustBe addAccountHolderPath(NormalMode)
      }
    }

    "account holders table" - {

      "must render one row per account holder" in {
        val rows = render(holders = Seq(alpha, beta)).select(".govuk-summary-list__row").asScala.toList

        rows.map(_.select("dt").text().trim) mustBe List("Alpha Holdings", "Beta Holdings")
      }

      "must link to change and remove each account holder" in {
        val row   = render(holders = Seq(alpha)).select(".govuk-summary-list__row")
        val links = row.select("a.govuk-link")

        links.size() mustBe 2
        links.get(0).attr("href") mustBe
          controllers.manual.accountHolders.routes.CurrentAccountHolderIdController
            .onChangeRedirect(alpha._1.value)
            .url
        links.get(1).attr("href") mustBe
          controllers.manual.accountHolders.routes.CurrentAccountHolderIdController
            .onRemoveRedirect(alpha._1.value)
            .url
      }

      "must not be rendered when there are no account holders" in {
        render(holders = Seq.empty, accountsWithoutHolders = oneAccount).select(".govuk-summary-list__row").size() mustBe 0
      }
    }

    "when every account has an account holder" - {

      "must ask whether another account holder is needed" in {
        val doc = render(holders = Seq(alpha), accountsWithoutHolders = Seq.empty)

        doc.select(".govuk-fieldset__legend--l h1").text() mustBe "Do you need to add another account holder?"
      }

      "must render a yes and a no option bound to the value field" in {
        val radios = render(holders = Seq(alpha), accountsWithoutHolders = Seq.empty).select("input[type=radio]")

        radios.size() mustBe 2
        radios.eachAttr("name").asScala.toSet mustBe Set("value")
        radios.eachAttr("value").asScala.toList mustBe List("true", "false")
      }

      "must render the save and continue button" in {
        val button = render(holders = Seq(alpha), accountsWithoutHolders = Seq.empty).select("button.govuk-button")

        button.size() mustBe 1
        button.text() mustBe "Save and continue"
        button.attr("type") mustBe "submit"
      }
    }

    "an account without an account holder alongside existing account holders" - {

      "must render the table and the single account warning instead of the radios" in {
        val doc = render(holders = Seq(alpha), accountsWithoutHolders = oneAccount)

        doc.select(".govuk-summary-list__row").size() mustBe 1
        paragraphTexts(doc) must contain("The account GB11AAAA11111111111111 is missing an account holder.")
        doc.select("input[type=radio]").size() mustBe 0
      }

      "must render the table and the list of accounts instead of the radios when several are missing" in {
        val doc = render(holders = Seq(alpha), accountsWithoutHolders = twoAccounts)

        doc.select(".govuk-summary-list__row").size() mustBe 1
        paragraphTexts(doc) must contain("These accounts are missing an account holder:")
        doc.select("input[type=radio]").size() mustBe 0
      }
    }

    "success notification" - {

      "must render a notification banner when an account holder has been removed" in {
        val doc = render(flash = Flash(Map("account-holder-removed" -> "Alpha Holdings")))

        doc.select(".govuk-notification-banner--success").size() mustBe 1
        doc.select(".govuk-notification-banner__title").text() mustBe "Success"
        doc.select(".govuk-notification-banner__heading").text() mustBe
          "Account holder Alpha Holdings removed."
      }

      "must not render a notification banner when no account holder has been removed" in {
        render().select(".govuk-notification-banner").size() mustBe 0
      }
    }
  }
}
