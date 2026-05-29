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

  private enum ElectionGroup:
    case CRS, FATCA, NONE

  private def hasAny(pages: Set[QuestionPage[Boolean]], userData: UserData): Boolean = pages.exists(userData.get(_).isDefined)

  private def allPresent(pages: Set[QuestionPage[Boolean]], userData: UserData): Boolean = pages.forall(userData.get(_).isDefined)

  private def electionGroup(userData: UserData): ElectionGroup =
    if hasAny(crsAllPages, userData) then ElectionGroup.CRS
    else if hasAny(fatcaPages, userData) then ElectionGroup.FATCA
    else ElectionGroup.NONE

  def validate(userData: UserData, reportingYear: Int): Either[String, Unit] =
    if !isReportingYearValid(reportingYear) then Left(controllers.routes.JourneyRecoveryController.onPageLoad().url)
    else validateElections(userData, reportingYear)

  private def isReportingYearValid(reportingYear: Int) = {
    val maxYear        = Year.now().getValue
    val minAllowedYear = maxYear - 12
    reportingYear >= minAllowedYear && reportingYear <= maxYear
  }

  private def isCrsPagesComplete(userData: UserData, reportingYear: Int): Boolean = {
    val baseComplete = allPresent(crsBasePages, userData)

    if reportingYear < REPORTING_THRESHOLD_YEAR then baseComplete
    else {
      userData.get(CarfGrossProceedsPage) match {
        case None        => false
        case Some(false) => baseComplete
        case Some(true)  => baseComplete && userData.get(CrsGrossProceedsPage).isDefined
      }
    }
  }

  private def validateElections(userData: UserData, reportingYear: Int): Either[String, Unit] =
    electionGroup(userData) match
      case ElectionGroup.CRS   => Either.cond(isCrsPagesComplete(userData, reportingYear), (), crsRedirect(reportingYear))
      case ElectionGroup.FATCA => Either.cond(allPresent(fatcaPages, userData), (), fatcaRedirect(reportingYear))
      case ElectionGroup.NONE  => Left(manageElectionRedirect(reportingYear))

}
