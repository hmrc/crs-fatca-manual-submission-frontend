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

import models.{NormalMode, UserData}
import pages.elections.{CRSContractsPage, CRSDormantAccountsPage, CRSThresholdsPage}
import pages.*
import utils.ReportingConstants.REPORTING_THRESHOLD_YEAR

import java.time.Year
import javax.inject.Inject

class CheckYourAnswersValidatorService @Inject() {

  private val crsBasePages: Set[QuestionPage[Boolean]]     = Set(CRSContractsPage, CRSDormantAccountsPage, CRSThresholdsPage)
  private val crsOptionalPages: Set[QuestionPage[Boolean]] = Set(CarfGrossProceedsPage, CrsGrossProceedsPage)
  private val crsAllPages: Set[QuestionPage[Boolean]]      = crsBasePages ++ crsOptionalPages
  private val fatcaPages: Set[QuestionPage[Boolean]]       = Set(IsUsTreasuryRegulatedPage, IsApplyingThresholdsPage)

  private def crsRedirect(reportingYear: Int)   = controllers.elections.routes.CRSContractsController.onPageLoad(NormalMode, reportingYear).url
  private def fatcaRedirect(reportingYear: Int) = controllers.elections.routes.IsUsTreasuryRegulatedController.onPageLoad(NormalMode, reportingYear).url
  private def manageElectionRedirect(reportingYear: Int) = controllers.routes.ManageElectionsController.onPageLoad(reportingYear).url

  def validate(userData: UserData, reportingYear: Int): Option[String] = {

    def isReportingYearValid = {
      val maxYear        = Year.now().getValue
      val minAllowedYear = maxYear - 12
      reportingYear >= minAllowedYear && reportingYear <= maxYear
    }

    def hasAny(pages: Set[QuestionPage[Boolean]]): Boolean = pages.exists(userData.get(_).isDefined)

    def allPresent(pages: Set[QuestionPage[Boolean]]): Boolean = pages.forall(userData.get(_).isDefined)

    def isCrsPagesComplete: Boolean = {
      val baseComplete = allPresent(crsBasePages)

      val requireCARFGrossProceed = reportingYear >= REPORTING_THRESHOLD_YEAR

      if !requireCARFGrossProceed then baseComplete
      else
        userData.get(CarfGrossProceedsPage).fold(false) {
          value =>
            if value then baseComplete && userData.get(CrsGrossProceedsPage).isDefined
            else baseComplete
        }
    }

    def isFatcaPagesComplete: Boolean = allPresent(fatcaPages)

    if !isReportingYearValid then Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
    else
      val hasCrsPages   = hasAny(crsAllPages)
      val hasFatcaPages = hasAny(fatcaPages)

      (hasCrsPages, hasFatcaPages) match {

        case (true, false) => if (isCrsPagesComplete) None else Some(crsRedirect(reportingYear))

        case (false, true) => if (isFatcaPagesComplete) None else Some(fatcaRedirect(reportingYear))

        case _ => Some(manageElectionRedirect(reportingYear))
      }
  }

}
