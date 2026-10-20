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

package controllers.actions

import base.SpecBase
import models.SubmissionsConstants.FATCA
import models.TaxResidentInformation
import models.requests.{CPSONameRequest, TaxResidentCountryIdForCPSORequest}
import models.response.Country
import models.viewModels.manual.cpso.CPSOId
import models.{ReportId, UserAnswers}
import pages.manual.cpso.{CurrentTaxResidentCountryIndexPage, TaxResidentCountriesListPage}
import play.api.test.FakeRequest

import scala.concurrent.Future

class TaxResidentCountryIdCreationForCPSOActionSpec extends SpecBase {

  class Harness extends TaxResidentCountryIdCreationForCPSOActionImpl() {
    def callTransform[A](request: CPSONameRequest[A]): Future[TaxResidentCountryIdForCPSORequest[A]] = transform(request)
  }

  private val userId   = "user-id"
  private val fatcaId  = "FATCAID"
  private val cpsoId   = CPSOId("TestCpsoId")
  private val cpsoName = "TestCpsoName"

  private val reportId = ReportId(
    regime = FATCA,
    reportingYear = 2025,
    uploadedTime = None,
    fiId = "FIID"
  )

  private def cpsoNameRequest(userAnswers: UserAnswers): CPSONameRequest[_] =
    CPSONameRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId,
      cpsoId = cpsoId,
      cpsoName = cpsoName
    )

  private def taxResidentCountries(count: Int): Seq[TaxResidentInformation] =
    (1 to count).map(
      index => TaxResidentInformation(Country(s"GB$index", s"Country $index"))
    )

  "TaxResidentCountryIdCreationForCPSOAction" - {

    "when there is no currentTaxResidentCountryIndex in the user answers" - {

      "must derive the index from the number of tax resident countries already recorded" in {
        val userAnswers = emptyUserAnswers
          .withPage(TaxResidentCountriesListPage(cpsoId, reportId), taxResidentCountries(2))

        val result = new Harness().callTransform(cpsoNameRequest(userAnswers)).futureValue

        result.currentIndex mustBe 2
      }

      "must use index 0 when no tax resident countries are recorded" in {
        val result = new Harness().callTransform(cpsoNameRequest(emptyUserAnswers)).futureValue

        result.currentIndex mustBe 0
      }

      "must ignore tax resident countries recorded against another CP-SO" in {
        val otherCpsoId = CPSOId("OtherCpsoId")

        val userAnswers = emptyUserAnswers
          .withPage(TaxResidentCountriesListPage(otherCpsoId, reportId), taxResidentCountries(3))
          .withPage(TaxResidentCountriesListPage(cpsoId, reportId), taxResidentCountries(1))

        val result = new Harness().callTransform(cpsoNameRequest(userAnswers)).futureValue

        result.currentIndex mustBe 1
      }
    }

    "when there is a currentTaxResidentCountryIndex in the user answers" - {

      "must reuse the stored index rather than the number of recorded countries" in {
        val userAnswers = emptyUserAnswers
          .withPage(TaxResidentCountriesListPage(cpsoId, reportId), taxResidentCountries(3))
          .withPage(CurrentTaxResidentCountryIndexPage(cpsoId, reportId), 1)

        val result = new Harness().callTransform(cpsoNameRequest(userAnswers)).futureValue

        result.currentIndex mustBe 1
      }
    }

    "must carry the incoming request through unchanged" in {
      val userAnswers = emptyUserAnswers
        .withPage(TaxResidentCountriesListPage(cpsoId, reportId), taxResidentCountries(1))

      val request = cpsoNameRequest(userAnswers)
      val result  = new Harness().callTransform(request).futureValue

      result.request mustBe request.request
      result.userId mustBe userId
      result.fatcaId mustBe fatcaId
      result.reportId mustBe reportId
      result.userAnswers mustBe userAnswers
      result.cpsoId mustBe cpsoId
      result.cpsoName mustBe cpsoName
    }
  }
}
