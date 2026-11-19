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
import forms.manual.cpso.ControllingPersonsOrSubstantialOwnersFormProvider
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.manual.cpso.IndividualName
import models.viewModels.manual.cpso.{CPSOId, ControllingOrSubstantialOwner}
import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.cpso.ControllingPersonsOrSubstantialOwnersView

import scala.jdk.CollectionConverters.*

class ControllingPersonsOrSubstantialOwnersViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: ControllingPersonsOrSubstantialOwnersView            = application.injector.instanceOf[ControllingPersonsOrSubstantialOwnersView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  private val formProvider                                               = new ControllingPersonsOrSubstantialOwnersFormProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val fiName             = "Acme Bank"
  private val individualName     = IndividualName("Jane", "Doe")
  private val individualFullName = individualName.fullName
  private val organisationName   = "Acme Holdings Ltd"
  private val cpsoIdOne          = CPSOId("cpso-1")
  private val cpsoIdTwo          = CPSOId("cpso-2")

  private val oneCpso: Seq[(CPSOId, ControllingOrSubstantialOwner)] = Seq(
    cpsoIdOne -> ControllingOrSubstantialOwner(individualName = Some(individualName))
  )

  private val twoCpsos: Seq[(CPSOId, ControllingOrSubstantialOwner)] = Seq(
    cpsoIdOne -> ControllingOrSubstantialOwner(individualName = Some(individualName)),
    cpsoIdTwo -> ControllingOrSubstantialOwner(organisationName = Some(organisationName))
  )

  private def formFor(regimeType: RegimeType): Form[Boolean] = formProvider(regimeType)

  private def render(
    boundForm: Form[_] = formFor(CRS),
    mode: Mode = NormalMode,
    regimeType: RegimeType = CRS,
    accountHoldersWithoutCPSO: Seq[String] = Seq.empty,
    cpsoList: Seq[(CPSOId, ControllingOrSubstantialOwner)] = Seq.empty
  ): Document =
    Jsoup.parse(
      view(boundForm, mode, fiName, regimeType, accountHoldersWithoutCPSO, cpsoList).body
    )

  private def linkHref(doc: Document, text: String): Option[String] =
    doc.select("a").asScala.find(_.text() == text).map(_.attr("href"))

  private def clearCpsoUrl(mode: Mode): String =
    controllers.manual.cpso.routes.ClearCpsoIdAndRedirectToAddCpsoController.onRedirect(mode).url

  private def sendAReportUrl: String =
    controllers.manual.routes.SendAReportController.onPageLoad().url

  private def changeUrl(cpsoId: CPSOId): String =
    controllers.manual.cpso.routes.CurrentCpsoIdController.onChangeRedirect(cpsoId.value).url

  private def removeUrl(cpsoId: CPSOId): String =
    controllers.manual.cpso.routes.CurrentCpsoIdController.onRemoveRedirect(cpsoId.value).url

  "ControllingPersonsOrSubstantialOwnersView" - {

    "page title" - {

      "must use the empty controlling persons heading for a CRS report" in {
        render().title() mustBe
          "You have not added any controlling persons - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the empty substantial owners heading for a FATCA report" in {
        render(regimeType = FATCA).title() mustBe
          "You have not added any substantial owners - Send a CRS or FATCA report - GOV.UK"
      }

      "must use the singular controlling person heading for one CRS controlling person" in {
        render(cpsoList = oneCpso).title() mustBe
          "You have added 1 controlling person - Send a CRS or FATCA report - GOV.UK"
      }

      "must include the number of controlling persons in the CRS title" in {
        render(cpsoList = twoCpsos).title() mustBe
          "You have added 2 controlling persons - Send a CRS or FATCA report - GOV.UK"
      }

      "must include the number of substantial owners in the FATCA title" in {
        render(regimeType = FATCA, cpsoList = twoCpsos).title() mustBe
          "You have added 2 substantial owners - Send a CRS or FATCA report - GOV.UK"
      }
    }

    "page heading" - {

      "must render a single h1" in {
        render().select("h1").size() mustBe 1
      }

      "must use the controlling persons wording when no controlling persons have been added" in {
        render().select("h1").text() mustBe "You have not added any controlling persons"
      }

      "must use the substantial owners wording when no substantial owners have been added" in {
        render(regimeType = FATCA).select("h1").text() mustBe "You have not added any substantial owners"
      }

      "must use the singular heading for one controlling person" in {
        render(cpsoList = oneCpso).select("h1").text() mustBe "You have added 1 controlling person"
      }

      "must include the number of controlling persons" in {
        render(cpsoList = twoCpsos).select("h1").text() mustBe "You have added 2 controlling persons"
      }

      "must use the substantial owners wording for multiple FATCA owners" in {
        render(regimeType = FATCA, cpsoList = twoCpsos).select("h1").text() mustBe
          "You have added 2 substantial owners"
      }
    }

    "when there are no controlling persons and no account holders without cpso" - {

      "must tell the user to add a controlling person for a CRS report" in {
        render().select("p.govuk-body").eachText().asScala.toList must contain(
          "You must add at least one controlling person."
        )
      }

      "must not show the CRS paragraph for a FATCA report" in {
        render(regimeType = FATCA).select("p.govuk-body").eachText().asScala.toList must not contain
          "You must add at least one controlling person."
      }

      "must render the add controlling person link for a CRS report" in {
        val doc = render()

        doc.select("a.govuk-button").text() mustBe "Add a controlling person"
        doc.select("a.govuk-button").attr("href") mustBe clearCpsoUrl(NormalMode)
      }

      "must render the add substantial owner link for a FATCA report" in {
        val doc = render(regimeType = FATCA)

        doc.select("a.govuk-button").text() mustBe "Add a substantial owner"
        doc.select("a.govuk-button").attr("href") mustBe clearCpsoUrl(NormalMode)
      }

      "must carry the mode into the add controlling person link" in {
        render(mode = CheckMode).select("a.govuk-button").attr("href") mustBe clearCpsoUrl(CheckMode)
      }

      "must not render any radios" in {
        render().select("input[type=radio]").size() mustBe 0
      }

      "must not render the controlling persons summary list" in {
        render().select(".govuk-summary-list").size() mustBe 0
      }

      "must not render the send a report link" in {
        linkHref(render(), s"Back to send a CRS report for $fiName") mustBe None
      }
    }

    "when exactly one account holder is missing a controlling person" - {

      "must explain which account holder is missing a controlling person" in {
        render(accountHoldersWithoutCPSO = Seq("Jane Smith")).select("p.govuk-body").eachText().asScala.toList must contain(
          "The account holder Jane Smith is missing a controlling person."
        )
      }

      "must use the substantial owner wording for a FATCA report" in {
        render(regimeType = FATCA, accountHoldersWithoutCPSO = Seq("Jane Smith")).select("p.govuk-body").eachText().asScala.toList must contain(
          "The account holder Jane Smith is missing a substantial owner."
        )
      }

      "must tell the user how to resolve the missing controlling person" in {
        render(accountHoldersWithoutCPSO = Seq("Jane Smith")).select("ul.govuk-list--bullet li").eachText().asScala.toList mustBe List(
          "change or add at least one controlling person to link to the account holder",
          "remove the account holder"
        )
      }

      "must render the add controlling person link" in {
        val doc = render(accountHoldersWithoutCPSO = Seq("Jane Smith"))

        doc.select("a.govuk-button").text() mustBe "Add a controlling person"
        doc.select("a.govuk-button").attr("href") mustBe clearCpsoUrl(NormalMode)
      }

      "must link back to the send a report page" in {
        val doc = render(accountHoldersWithoutCPSO = Seq("Jane Smith"))

        linkHref(doc, s"Back to send a CRS report for $fiName") mustBe Some(sendAReportUrl)
      }

      "must not render the yes no radios" in {
        render(accountHoldersWithoutCPSO = Seq("Jane Smith")).select("input[type=radio]").size() mustBe 0
      }
    }

    "when more than one account holder is missing a controlling person" - {

      "must list the account holders missing a controlling person" in {
        val doc = render(accountHoldersWithoutCPSO = Seq("Jane Smith", "John Smith"))

        doc.select("p.govuk-body").eachText().asScala.toList must contain(
          "These account holders are missing a controlling person:"
        )
        doc.select("ul.govuk-list--bullet li").eachText().asScala.toList must contain allOf (
          "Jane Smith",
          "John Smith"
        )
      }

      "must use the substantial owner wording for a FATCA report" in {
        render(regimeType = FATCA, accountHoldersWithoutCPSO = Seq("Jane Smith", "John Smith")).select("p.govuk-body").eachText().asScala.toList must contain(
          "These account holders are missing a substantial owner:"
        )
      }

      "must tell the user how to resolve the missing controlling persons" in {
        render(accountHoldersWithoutCPSO = Seq("Jane Smith", "John Smith")).select("ul.govuk-list--bullet li").eachText().asScala.toList must contain allOf (
          "change or add at least one controlling person to link to the account holders",
          "remove the account holders"
        )
      }

      "must render the add controlling person link" in {
        val doc = render(accountHoldersWithoutCPSO = Seq("Jane Smith", "John Smith"))

        doc.select("a.govuk-button").text() mustBe "Add a controlling person"
        doc.select("a.govuk-button").attr("href") mustBe clearCpsoUrl(NormalMode)
      }

      "must link back to the send a report page" in {
        val doc = render(accountHoldersWithoutCPSO = Seq("Jane Smith", "John Smith"))

        linkHref(doc, s"Back to send a CRS report for $fiName") mustBe Some(sendAReportUrl)
      }

      "must not render the yes no radios" in {
        render(accountHoldersWithoutCPSO = Seq("Jane Smith", "John Smith")).select("input[type=radio]").size() mustBe 0
      }
    }

    "when controlling persons exist" - {

      "must list each controlling person" in {
        val rows = render(cpsoList = twoCpsos).select(".govuk-summary-list__row").asScala.toList

        rows.map(_.select(".govuk-summary-list__key").text()) mustBe List(individualFullName, organisationName)
      }

      "must link each controlling person to the change and remove pages" in {
        val rows = render(cpsoList = twoCpsos).select(".govuk-summary-list__row").asScala.toList

        rows.flatMap(_.select("a").asScala.map(_.attr("href"))) mustBe List(
          changeUrl(cpsoIdOne),
          removeUrl(cpsoIdOne),
          changeUrl(cpsoIdTwo),
          removeUrl(cpsoIdTwo)
        )
      }

      "must label the change and remove links for assistive technology" in {
        val firstRow = render(cpsoList = twoCpsos).select(".govuk-summary-list__row").asScala.head

        firstRow.select(".govuk-visually-hidden").eachText().asScala.toList mustBe List(
          s"Change controlling person $individualFullName",
          s"Remove controlling person $individualFullName"
        )
      }

      "must ask whether the user needs to add another controlling person" in {
        render(cpsoList = twoCpsos).select(".govuk-fieldset__legend--m").text() mustBe
          "Do you need to add another controlling person?"
      }

      "must use the substantial owner wording for the radio legend in a FATCA report" in {
        render(regimeType = FATCA, cpsoList = twoCpsos).select(".govuk-fieldset__legend--m").text() mustBe
          "Do you need to add another substantial owner?"
      }

      "must render yes and no radios" in {
        val doc = render(cpsoList = twoCpsos)

        doc.select("input[name=value]").asScala.map(_.attr("value")).toList mustBe List("true", "false")
      }

      "must render the save and continue button inside the form" in {
        val doc = render(cpsoList = twoCpsos)

        doc.select("form button.govuk-button").text() mustBe "Continue"
        doc.select("form button.govuk-button").attr("type") mustBe "submit"
      }

      "must not render the add controlling person link" in {
        render(cpsoList = twoCpsos).select("a.govuk-button").size() mustBe 0
      }

      "must not repeat the empty controlling persons paragraph" in {
        render(cpsoList = twoCpsos).select("p.govuk-body").eachText().asScala.toList must not contain
          "You must add at least one controlling person."
      }
    }

    "when controlling persons exist and one account holder is missing cpso" - {

      "must render the controlling persons summary and the missing account holder guidance" in {
        val doc = render(cpsoList = oneCpso, accountHoldersWithoutCPSO = Seq("Jane Smith"))

        doc.select(".govuk-summary-list__row").size() mustBe 1
        doc.select("p.govuk-body").eachText().asScala.toList must contain(
          "The account holder Jane Smith is missing a controlling person."
        )
        doc.select("input[type=radio]").size() mustBe 0
      }

      "must still offer the add controlling person link" in {
        render(cpsoList = oneCpso, accountHoldersWithoutCPSO = Seq("Jane Smith")).select("a.govuk-button").text() mustBe
          "Add a controlling person"
      }
    }

    "when controlling persons exist and more than one account holder is missing cpso" - {

      "must render the controlling persons summary and the missing account holders guidance" in {
        val doc = render(cpsoList = oneCpso, accountHoldersWithoutCPSO = Seq("Jane Smith", "John Smith"))

        doc.select(".govuk-summary-list__row").size() mustBe 1
        doc.select("p.govuk-body").eachText().asScala.toList must contain(
          "These account holders are missing a controlling person:"
        )
        doc.select("ul.govuk-list--bullet li").eachText().asScala.toList must contain allOf (
          "Jane Smith",
          "John Smith"
        )
        doc.select("input[type=radio]").size() mustBe 0
      }

      "must use the substantial owner wording for a FATCA report" in {
        val doc = render(regimeType = FATCA, cpsoList = oneCpso, accountHoldersWithoutCPSO = Seq("Jane Smith", "John Smith"))

        doc.select("p.govuk-body").eachText().asScala.toList must contain(
          "These account holders are missing a substantial owner:"
        )
        doc.select("ul.govuk-list--bullet li").eachText().asScala.toList must contain(
          "change or add at least one substantial owner to link to the account holders"
        )
      }
    }

    "error summary" - {

      "must show the CRS validation error when the radios are submitted without a selection" in {
        val boundForm = formFor(CRS).bind(Map("value" -> ""))
        val doc       = render(boundForm = boundForm, cpsoList = oneCpso)

        doc.select(".govuk-error-summary").size() mustBe 1
        doc.select(".govuk-error-summary__title").text() mustBe "There is a problem"
        doc.select(".govuk-error-summary a").attr("href") mustBe "#value"
        doc.select(".govuk-error-summary__list").text() mustBe
          "Select yes if you need to add another controlling person"
      }

      "must show the FATCA validation error when the radios are submitted without a selection" in {
        val boundForm = formFor(FATCA).bind(Map("value" -> ""))
        val doc       = render(boundForm = boundForm, cpsoList = oneCpso, regimeType = FATCA)

        doc.select(".govuk-error-summary__list").text() mustBe
          "Select yes if you need to add another substantial owner"
      }
    }

    "notification banner" - {

      "for CRS" in {
        implicit val request: FakeRequest[AnyContent] = FakeRequest().withFlash("cpso-removed" -> "test Name")
        val renderedHtml: HtmlFormat.Appendable       = view(formFor(CRS), NormalMode, fiName, CRS, Seq.empty, Seq.empty)
        lazy val doc                                  = Jsoup.parse(renderedHtml.body)
        doc.select("h3").text() mustBe "Controlling person test Name removed."
      }

      "for FATCA" in {
        implicit val request: FakeRequest[AnyContent] = FakeRequest().withFlash("cpso-removed" -> "test Name")
        val renderedHtml: HtmlFormat.Appendable       = view(formFor(FATCA), NormalMode, fiName, FATCA, Seq.empty, Seq.empty)
        lazy val doc                                  = Jsoup.parse(renderedHtml.body)
        doc.select("h3").text() mustBe "Substantial owner test Name removed."
      }
    }
  }
}
