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

package services

import base.SpecBase
import models.NormalMode
import pages.elections.{CRSContractsPage, CRSDormantAccountsPage, CRSThresholdsPage}
import pages.{CarfGrossProceedsPage, CrsGrossProceedsPage, IsApplyingThresholdsPage, IsUsTreasuryRegulatedPage}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Year

class CheckYourAnswersValidatorServiceSpec extends SpecBase {
  private val service = new CheckYourAnswersValidatorService()

  given HeaderCarrier = HeaderCarrier()

  "CheckYourAnswersValidatorService" - {

    def crsRedirectUrl(year: Int): String    = controllers.elections.routes.CRSContractsController.onPageLoad(NormalMode, year).url
    def fatcaRedirectUrl(year: Int): String  = controllers.elections.routes.IsUsTreasuryRegulatedController.onPageLoad(NormalMode, year).url
    def manageElectionUrl(year: Int): String = controllers.routes.ManageElectionsController.onPageLoad(year).url

    "validate - reporting year validation" - {
      "should redirect when reporting year is older than 12 years" in {

        val crsData = emptyUserData
          .withPage(CRSContractsPage, true)
          .withPage(CRSDormantAccountsPage, true)
          .withPage(CRSThresholdsPage, true)

        val reportingYear = Year.now().getValue - 13

        service.validate(crsData, reportingYear) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

      "should redirect when reporting year is greater than current years" in {

        val crsData = emptyUserData
          .withPage(CRSContractsPage, true)
          .withPage(CRSDormantAccountsPage, true)
          .withPage(CRSThresholdsPage, true)

        val reportingYear = Year.now().getValue + 1

        service.validate(crsData, reportingYear) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
    val year2025 = 2025
    "validate - CRS Regime" - {
      "should return None when reporting year is 2025 & crs pages are complete" in {

        val crsData = emptyUserData
          .withPage(CRSContractsPage, true)
          .withPage(CRSDormantAccountsPage, true)
          .withPage(CRSThresholdsPage, true)

        service.validate(crsData, year2025) mustBe None
      }

      val currentYear = Year.now().getValue
      "should return None when reporting year is 2026 & crs pages are complete & carfGross is false" in {

        val crsData = emptyUserData
          .withPage(CRSContractsPage, true)
          .withPage(CRSDormantAccountsPage, true)
          .withPage(CRSThresholdsPage, true)
          .withPage(CarfGrossProceedsPage, false)

        service.validate(crsData, currentYear) mustBe None
      }

      "should return None when reporting year is 2026 & crs pages are complete" in {

        val crsData = emptyUserData
          .withPage(CRSContractsPage, true)
          .withPage(CRSDormantAccountsPage, true)
          .withPage(CRSThresholdsPage, true)
          .withPage(CarfGrossProceedsPage, true)
          .withPage(CrsGrossProceedsPage, true)

        service.validate(crsData, currentYear) mustBe None
      }

      "should return Some(CRSRedirectUrl) when reporting year is 2026 & carfGross is true & crsGross is None" in {

        val crsData = emptyUserData
          .withPage(CRSContractsPage, true)
          .withPage(CRSDormantAccountsPage, true)
          .withPage(CRSThresholdsPage, true)
          .withPage(CarfGrossProceedsPage, true)

        service.validate(crsData, currentYear) mustBe Some(crsRedirectUrl(currentYear))
      }

      "should return Some(CRSRedirectUrl) when reporting year is 2026 & carfGross is None" in {

        val crsData = emptyUserData
          .withPage(CRSContractsPage, true)
          .withPage(CRSDormantAccountsPage, true)
          .withPage(CRSThresholdsPage, true)

        service.validate(crsData, currentYear) mustBe Some(crsRedirectUrl(currentYear))
      }

      "should return Some(CRSRedirectUrl) when reporting year is 2026 & crsContract is None" in {

        val crsData = emptyUserData
          .withPage(CRSDormantAccountsPage, true)
          .withPage(CRSThresholdsPage, true)

        service.validate(crsData, currentYear) mustBe Some(crsRedirectUrl(currentYear))
      }

      "should return Some(CRSRedirectUrl) when reporting year is 2026 & crsDormant is None" in {

        val crsData = emptyUserData
          .withPage(CRSContractsPage, true)
          .withPage(CRSThresholdsPage, true)

        service.validate(crsData, currentYear) mustBe Some(crsRedirectUrl(currentYear))
      }

      "should return Some(CRSRedirectUrl) when reporting year is 2026 & crsTheshold is None" in {

        val crsData = emptyUserData
          .withPage(CRSContractsPage, true)
          .withPage(CRSDormantAccountsPage, true)

        service.validate(crsData, currentYear) mustBe Some(crsRedirectUrl(currentYear))
      }
    }
    "validate - FATCA Regime" - {
      "should return None when fatca pages are complete" in {

        val fatcaData = emptyUserData
          .withPage(IsUsTreasuryRegulatedPage, true)
          .withPage(IsApplyingThresholdsPage, true)

        service.validate(fatcaData, year2025) mustBe None
      }
      "should return Some(fatcaRedirect) when IsApplyingThreshold Page is None" in {

        val fatcaData = emptyUserData
          .withPage(IsUsTreasuryRegulatedPage, true)

        service.validate(fatcaData, year2025) mustBe Some(fatcaRedirectUrl(year2025))
      }
      "should return Some(fatcaRedirect) when IsUsTreasuryRegulatedPage Page is None" in {

        val fatcaData = emptyUserData
          .withPage(IsApplyingThresholdsPage, true)

        service.validate(fatcaData, year2025) mustBe Some(fatcaRedirectUrl(year2025))
      }
    }
    "validate - No Regime" - {
      "should return Some(manageReport) when Partial pages are present" in {
        val fatcaData = emptyUserData
          .withPage(IsUsTreasuryRegulatedPage, true)
          .withPage(CRSContractsPage, true)

        service.validate(fatcaData, year2025) mustBe Some(manageElectionUrl(year2025))
      }
      "should return Some(manageReport) when UserAnswer is empty" in {
        service.validate(emptyUserData, year2025) mustBe Some(manageElectionUrl(year2025))
      }
    }
  }
}
