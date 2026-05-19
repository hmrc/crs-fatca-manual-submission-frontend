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
import pages._
import models._

@Singleton
class Navigator @Inject() () {

  def nextPage(page: Page, mode: Mode, userData: UserData, year: Option[Int]): Call =
    (mode, page) match {
      case (CheckMode, _) =>
        routes.CheckYourAnswersController.onPageLoad()
      case (_, CRSContractsPage) =>
        year.fold(routes.JourneyRecoveryController.onPageLoad()) {
          reportingYear => controllers.elections.routes.CRSDormantAccountsController.onPageLoad(mode, reportingYear)
        }
      case (_, CRSDormantAccountsPage) =>
        year.fold(routes.JourneyRecoveryController.onPageLoad()) {
          reportingYear => controllers.elections.routes.CRSThresholdsController.onPageLoad(mode, reportingYear)
        }
      case _ => routes.CheckYourAnswersController.onPageLoad()
    }
}
