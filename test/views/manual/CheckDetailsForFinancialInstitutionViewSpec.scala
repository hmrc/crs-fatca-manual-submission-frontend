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

package views.manual

import base.SpecBase
import forms.manual.HasFinishedCheckingFormProvider
import models.SubmissionsConstants.{CRS, FATCA}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import viewmodels.govuk.all.SummaryListViewModel
import views.html.CheckDetailsForFinancialInstitutionView

class CheckDetailsForFinancialInstitutionViewSpec extends SpecBase {

  private val application = applicationBuilder().build()
  private val view        = application.injector.instanceOf[CheckDetailsForFinancialInstitutionView]
  private val form        = new HasFinishedCheckingFormProvider().apply()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messages(application)

  private val list             = SummaryListViewModel(Seq.empty)
  private val fiName           = "someFinancialInstitution"
  private val changeDetailsUrl = "/change-answers/someFiId"
  private val utr              = Some("1234567890")

  private def renderView(
    regime: models.SubmissionsConstants.RegimeType,
    hasGiin: Boolean,
    fiIsUser: Boolean = false,
    formToRender: play.api.data.Form[Boolean] = form
  ) =
    Jsoup.parse(
      view(
        formToRender,
        list,
        fiName,
        regime,
        hasGiin,
        fiIsUser,
        utr,
        changeDetailsUrl
      ).body
    )

  "CheckDetailsForFinancialInstitutionView" - {

    "when regime is CRS" - {
      "must render the page details" in {
        val doc = renderView(CRS, hasGiin = false, fiIsUser = true)

        doc.title() must include(messages("CheckDetailsForFinancialInstitution.title"))
        doc.select("h1").text() mustBe messages("CheckDetailsForFinancialInstitution.heading", fiName)
        doc.text() must include("We’ll use these details for your report.")
        doc.text() must include(messages("CheckDetailsForFinancialInstitution.para.2.crsFiIsUser", utr.get))
        doc.select("a[href=" + changeDetailsUrl + "]").text() must include(s"change these details for $fiName if needed")
        doc.select(".govuk-radios__item").size() mustBe 2
        doc.text() must include(messages("hasFinishedChecking.subheading", fiName))
        doc.text() must include(messages("hasFinishedChecking.para"))
        doc.select("#submit").text() mustBe messages("site.continue")
      }
      "must render the error summary and mark the radios as invalid when the answer is missing" in {
        val formWithErrors = form.bind(Map.empty[String, String])
        val doc            = renderView(CRS, hasGiin = false, fiIsUser = true, formToRender = formWithErrors)

        doc.select(".govuk-error-summary__list").text() must include(messages("hasFinishedChecking.error.required"))
        doc.select(".govuk-error-message").text() must include(messages("hasFinishedChecking.error.required"))
      }

    }
    "when regime is FATCA" - {

      "must show the GIIN requirement and hide the checking question for FATCA without a GIIN" in {
        val doc            = renderView(FATCA, hasGiin = false)
        val changeLinkText = messages("CheckDetailsForFinancialInstitution.para.1.link.fatcaNoGiin", fiName)

        doc.text() must include(messages("CheckDetailsForFinancialInstitution.para.1.fatcaNoGiin", changeLinkText))
        doc.select("a[href=" + changeDetailsUrl + "]").text() must include(
          messages("CheckDetailsForFinancialInstitution.para.1.link.fatcaNoGiin", fiName)
        )
        doc.select(".govuk-radios").isEmpty mustBe true
        doc.select("#submit").isEmpty mustBe true
      }

      "must show the checking question for FATCA when a GIIN is present" in {
        val doc = renderView(FATCA, hasGiin = true)

        doc.select(".govuk-radios__item").size() mustBe 2
        doc.text() must include(messages("hasFinishedChecking.subheading", fiName))
        doc.text() must include(messages("hasFinishedChecking.para"))
        doc.text() must not include messages("CheckDetailsForFinancialInstitution.para.2.crsFiIsUser", utr)
      }
    }
  }
}
