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
import models.manual.accountHolders.TaxResidentInformation
import models.requests.{AccountHolderNameRequest, TaxResidentCountryIdForAccountHolderRequest}
import models.response.Country
import models.viewModels.AccountHolderId
import models.{ReportId, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.manual.accountHolders.{CurrentTaxResidentCountryIndexPage, TaxResidentCountriesListPage}
import play.api.test.FakeRequest

import scala.concurrent.Future

class TaxResidentCountryIdCreationForAccountHolderActionSpec extends SpecBase with MockitoSugar {

  class Harness extends TaxResidentCountryIdCreationForAccountHolderActionImpl() {

    def callTransform[A](request: AccountHolderNameRequest[A]): Future[TaxResidentCountryIdForAccountHolderRequest[A]] =
      transform(request)
  }

  private val userId            = "user-id"
  private val fatcaId           = "FATCAID"
  private val accountHolderId   = AccountHolderId("holder-id")
  private val accountHolderName = "Test Account Holder"

  private val reportId = ReportId(
    regime = FATCA,
    reportingYear = 2025,
    uploadedTime = None,
    fiId = "FIID"
  )

  private val taxResidentCountries = Seq(
    TaxResidentInformation(Country("GB", "United Kingdom")),
    TaxResidentInformation(Country("US", "United States"))
  )

  private def accountHolderNameRequest(userAnswers: UserAnswers): AccountHolderNameRequest[_] =
    AccountHolderNameRequest(
      request = FakeRequest(),
      userId = userId,
      userAnswers = userAnswers,
      fatcaId = fatcaId,
      reportId = reportId,
      accountHolderId = accountHolderId,
      accountHolderName = accountHolderName
    )

  "TaxResidentCountryIdCreationForAccountHolderAndAccountHolderNameAction" - {

    "when there is no currentTaxResidentCountryId in the userAnswers" - {

      "must set currentIndex to 0 when there are no tax resident countries" in {
        val action      = new Harness()
        val userAnswers = emptyUserAnswers

        val result = action.callTransform(accountHolderNameRequest(userAnswers)).futureValue

        result.userId mustBe userId
        result.userAnswers mustBe userAnswers
        result.fatcaId mustBe fatcaId
        result.reportId mustBe reportId
        result.accountHolderId mustBe accountHolderId
        result.accountHolderName mustBe accountHolderName
        result.currentIndex mustBe 0
      }

      "must set currentIndex to the size of the existing tax resident countries list" in {
        val action = new Harness()
        val userAnswers = emptyUserAnswers
          .withPage(TaxResidentCountriesListPage(accountHolderId, reportId), taxResidentCountries)

        val result = action.callTransform(accountHolderNameRequest(userAnswers)).futureValue

        result.userId mustBe userId
        result.userAnswers mustBe userAnswers
        result.fatcaId mustBe fatcaId
        result.reportId mustBe reportId
        result.accountHolderId mustBe accountHolderId
        result.accountHolderName mustBe accountHolderName
        result.currentIndex mustBe taxResidentCountries.size
      }
    }

    "when there is a currentTaxResidentCountryId in the userAnswers" - {

      "must use the existing id as the currentIndex" in {
        val action = new Harness()
        val userAnswers = emptyUserAnswers
          .withPage(CurrentTaxResidentCountryIndexPage(accountHolderId, reportId), 2)
          .withPage(TaxResidentCountriesListPage(accountHolderId, reportId), taxResidentCountries)

        val result = action.callTransform(accountHolderNameRequest(userAnswers)).futureValue

        result.userId mustBe userId
        result.userAnswers mustBe userAnswers
        result.fatcaId mustBe fatcaId
        result.reportId mustBe reportId
        result.accountHolderId mustBe accountHolderId
        result.accountHolderName mustBe accountHolderName
        result.currentIndex mustBe 2
      }
    }
  }
}
