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
import play.api.Logging
import utils.ReportingConstants

@Singleton
class Navigator @Inject() () extends Logging {

  private val normalRoutes: Page => UserData => Call = {
    case CRSContractsPage       => _ => controllers.elections.routes.CRSDormantAccountsController.onPageLoad(NormalMode)
    case CRSDormantAccountsPage => _ => controllers.elections.routes.CRSThresholdsController.onPageLoad(NormalMode)
    case CRSThresholdsPage      => userAnswers => thresholdsNavigation(userAnswers)
    case CarfGrossProceedsPage  => userAnswers => crsCarfGrossProceedsRedirect(userAnswers)
    case CrsGrossProceedsPage   => _ => routes.CheckYourAnswersController.onPageLoad()
    case _                      => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserData => Call = {
    case CRSContractsPage       => _ => controllers.elections.routes.CRSDormantAccountsController.onPageLoad(CheckMode)
    case CRSDormantAccountsPage => _ => controllers.elections.routes.CRSThresholdsController.onPageLoad(CheckMode)
    case CarfGrossProceedsPage  => userAnswers => crsCarfGrossProceedsRedirect(userAnswers)
    case _                      => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userData: UserData): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userData)
    case CheckMode =>
      checkRouteMap(page)(userData)
  }

  private def thresholdsNavigation(userAnswers: UserData): Call =
    userAnswers.get(CRSReportingPeriodPage) match {
      case Some(year) =>
        if (year >= ReportingConstants.ThresholdDate.getYear)
          controllers.elections.routes.CarfGrossProceedsController.onPageLoad(NormalMode)
        else
          routes.CheckYourAnswersController.onPageLoad()
      case _ =>
        logger.error("CRS Reporting period not found in user answers when navigating from thresholds page")
        routes.JourneyRecoveryController.onPageLoad()
    }

  private def crsCarfGrossProceedsRedirect(userAnswers: UserData) =
    userAnswers.get(CarfGrossProceedsPage) match {
      case Some(true)  => controllers.elections.routes.CrsGrossProceedsController.onPageLoad(NormalMode)
      case Some(false) => routes.CheckYourAnswersController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }
}
