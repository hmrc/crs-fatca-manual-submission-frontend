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

package navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages.*
import models.*
import pages.elections.{CRSContractsPage, CRSDormantAccountsPage, CRSThresholdsPage}
import utils.ReportingConstants

@Singleton
class Navigator @Inject() () {

  def nextPage(page: Page, mode: Mode, userData: UserData, year: Option[Int]): Call =
    (page, mode) match {
      case (IsUsTreasuryRegulatedPage, NormalMode) =>
        year.fold(routes.JourneyRecoveryController.onPageLoad())(
          y => controllers.elections.routes.IsApplyingThresholdsController.onPageLoad(NormalMode, y)
        )
      case (IsApplyingThresholdsPage, NormalMode) =>
        routes.JourneyRecoveryController.onPageLoad()
      case (CRSContractsPage, NormalMode) =>
        year.fold(routes.JourneyRecoveryController.onPageLoad()) {
          reportingYear => controllers.elections.routes.CRSDormantAccountsController.onPageLoad(mode, reportingYear)
        }
      case (CRSDormantAccountsPage, NormalMode) =>
        year.fold(routes.JourneyRecoveryController.onPageLoad()) {
          reportingYear => controllers.elections.routes.CRSThresholdsController.onPageLoad(mode, reportingYear)
        }
      case (CRSThresholdsPage, NormalMode) =>
        year.fold(routes.JourneyRecoveryController.onPageLoad()) {
          reportingYear => thresholdsNavigation(reportingYear)
        }
      case (CarfGrossProceedsPage, _) =>
        year.fold(routes.JourneyRecoveryController.onPageLoad()) {
          reportingYear => crsCarfGrossProceedsRedirect(userData, reportingYear, mode)
        }
      case (CrsGrossProceedsPage, NormalMode) =>
        year.fold(routes.JourneyRecoveryController.onPageLoad()) {
          reportingYear => routes.CheckYourAnswersController.onPageLoad()
        }
      case (_, CheckMode) =>
        routes.CheckYourAnswersController.onPageLoad()
      case _ =>
        routes.IndexController.onPageLoad()
    }

  private def thresholdsNavigation(year: Int): Call =
    if (year >= ReportingConstants.REPORTING_THRESHOLD_YEAR) {
      controllers.elections.routes.CarfGrossProceedsController.onPageLoad(NormalMode, year)
    } else {
      routes.CheckYourAnswersController.onPageLoad()
    }

  private def crsCarfGrossProceedsRedirect(userAnswers: UserData, reportingYear: Int, mode: Mode) =
    userAnswers.get(CarfGrossProceedsPage) match {
      case Some(true)  => controllers.elections.routes.CrsGrossProceedsController.onPageLoad(mode, reportingYear)
      case Some(false) => routes.CheckYourAnswersController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }
}
