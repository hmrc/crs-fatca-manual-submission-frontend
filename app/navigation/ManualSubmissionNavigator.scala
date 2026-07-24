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

import controllers.manual.account
import controllers.manual.reportdetails.routes.*
import controllers.routes
import models.*
import pages.*
import pages.manual.account.{HaveNumberPage, IdentifierPage, NumberTypePage}
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import pages.manual.sponsor.*
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

  private def accountNavigation(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (HaveNumberPage(), mode, ua) => haveNumberNavigation(mode, ua)
    case (NumberTypePage(), mode, ua) => routes.UnderConstructionController.onPageLoad()
    case (IdentifierPage(), mode, ua) => routes.UnderConstructionController.onPageLoad()
  }

  private def fillerNavigation: PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (WhatTypeOfFilerPage(), _, _)          => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
    case (WhatTypeOfFilerIsSponsorPage(), _, _) => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
    case (IdentifierPage(), _, _)               => routes.UnderConstructionController.onPageLoad()
  }

  private def sponsorNavigation(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (HaveSponsorPage(), mode, ua)         => haveSponsorNavigation(mode, ua)
    case (SponsorNamePage(), mode, _)          => controllers.manual.sponsor.routes.WhatIsGIINForSponsorController.onPageLoad(mode)
    case (WhatIsGIINForSponsorPage(), mode, _) => controllers.manual.sponsor.routes.IsSponsorBasedInUKController.onPageLoad(mode)
    case (IsSponsorBasedInUKPage(), mode, ua)  => handleSponsorBasedUKNavigation(ua, mode)
    case (UKPostcodePage(), _, _)              => routes.UnderConstructionController.onPageLoad()
  }

  private def navigation(implicit reportId: ReportId) =
    accountNavigation orElse sponsorNavigation orElse fillerNavigation

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId): Call =
    navigation
      .lift((page, mode, userAnswers))
      .getOrElse(routes.IndexController.onPageLoad())

  private def haveSponsorNavigation(mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(HaveSponsorPage()) match {
      case Some(true)  => controllers.manual.sponsor.routes.SponsorNameController.onPageLoad(mode)
      case Some(false) => routes.UnderConstructionController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def haveNumberNavigation(mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(HaveNumberPage()) match {
      case Some(true)  => account.routes.NumberTypeController.onPageLoad(mode)
      case Some(false) => account.routes.IdentifierController.onPageLoad(mode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleSponsorBasedUKNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(IsSponsorBasedInUKPage()) match {
      case Some(true)  => controllers.manual.sponsor.routes.UKPostcodeController.onPageLoad(mode)
      case Some(false) => routes.UnderConstructionController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }
}
