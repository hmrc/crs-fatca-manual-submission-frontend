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

import controllers.manual.reportdetails.routes.*
import controllers.routes
import models.*
import pages.*
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import pages.manual.sponser.{HaveSponserPage, IsSponsorBasedInUKPage, SponserNamePage, WhatIsGIINForSponsorPage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ManualSubmissionNavigator @Inject() () {

  def nextPageWithoutReportId(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page, userAnswers)
    case CheckMode =>
      routes.UnderConstructionController.onPageLoad()
  }

  private def normalRoutes(page: Page, userAnswers: UserAnswers): Call =
    page match {
      case CrsOrFatcaPage    => ReportingYearController.onPageLoad(NormalMode)
      case ReportingYearPage => TypeOfReportController.onPageLoad(NormalMode)
      case TypeOfReportPage  => ReportDetailsCheckAnswersController.onPageLoad()
      case _                 => routes.IndexController.onPageLoad()
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId): Call =
    page match {
      case p if p == HaveSponserPage() =>
        userAnswers.get(HaveSponserPage()) match {
          case Some(true)  => controllers.manual.sponser.routes.SponserNameController.onPageLoad(mode)
          case Some(false) => routes.UnderConstructionController.onPageLoad()
          case None        => routes.JourneyRecoveryController.onPageLoad()
        }
      case p if p == SponserNamePage() => controllers.manual.sponser.routes.WhatIsGIINForSponsorController.onPageLoad(NormalMode)
      case p if p == WhatIsGIINForSponsorPage() =>
        controllers.manual.sponser.routes.IsSponsorBasedInUKController.onPageLoad(NormalMode)
      case p if p == IsSponsorBasedInUKPage() =>
        routes.UnderConstructionController.onPageLoad()
      case _ => routes.IndexController.onPageLoad()
    }
}
