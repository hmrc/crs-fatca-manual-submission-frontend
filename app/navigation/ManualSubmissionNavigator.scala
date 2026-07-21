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
import pages.manual.account.HaveNumberPage
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import pages.manual.sponsor.{HaveSponsorPage, IsSponsorBasedInUKPage, SponsorNamePage, UKPostcodePage, WhatIsGIINForSponsorPage}
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
      case HaveSponsorPage()          => haveSponsorNavigation(mode, userAnswers)
      case SponsorNamePage()          => controllers.manual.sponsor.routes.WhatIsGIINForSponsorController.onPageLoad(NormalMode)
      case WhatIsGIINForSponsorPage() => controllers.manual.sponsor.routes.IsSponsorBasedInUKController.onPageLoad(NormalMode)
      case IsSponsorBasedInUKPage()   => handleSponsorBasedUKNavigation(userAnswers, mode)
      case UKPostcodePage()           => routes.UnderConstructionController.onPageLoad()
      case IsSponsorBasedInUKPage()   => routes.UnderConstructionController.onPageLoad()
      case HaveNumberPage()           => haveNumberNavigation(mode, userAnswers)
      case NumberTypePage()           => routes.UnderConstructionController.onPageLoad()
      case UkAddressPage()            => routes.UnderConstructionController.onPageLoad()
      case _                          => routes.IndexController.onPageLoad()
    }

  private def haveSponsorNavigation(mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(HaveSponsorPage()) match {
      case Some(true)  => controllers.manual.sponsor.routes.SponsorNameController.onPageLoad(mode)
      case Some(false) => routes.UnderConstructionController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def haveNumberNavigation(mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(HaveNumberPage()) match {
      case Some(true)  => controllers.manual.account.routes.NumberTypeController.onPageLoad(mode)
      case Some(false) => routes.UnderConstructionController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleSponsorBasedUKNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(IsSponsorBasedInUKPage()) match {
      case Some(true)  => controllers.manual.sponsor.routes.UKPostcodeController.onPageLoad(mode)
      case Some(false) => routes.UnderConstructionController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }
}
