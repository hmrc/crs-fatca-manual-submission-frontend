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

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId): Call =
    page match {
      case HaveSponsorPage()              => haveSponsorNavigation(mode, userAnswers)
      case HaveNumberPage()               => haveNumberNavigation(mode, userAnswers)
      case NumberTypePage()               => routes.UnderConstructionController.onPageLoad()
      case SponsorNamePage()              => controllers.manual.sponsor.routes.WhatIsGIINForSponsorController.onPageLoad(NormalMode)
      case WhatIsGIINForSponsorPage()     => controllers.manual.sponsor.routes.IsSponsorBasedInUKController.onPageLoad(NormalMode)
      case IsSponsorBasedInUKPage()       => handleSponsorBasedUKNavigation(userAnswers, mode)
      case UKPostcodePage()               => handleUKPostcodeNavigation(userAnswers, mode)
      case WhatTypeOfFilerPage()          => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
      case WhatTypeOfFilerIsSponsorPage() => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
      case WhatIsAddressForSponsorPage()  => handleWhatIsAddressForSponsorNavigation(userAnswers, NormalMode)
      case IsThisAddressForSponsorPage()  => handleIsThisAddressForSponsorNavigation(userAnswers, NormalMode)
      case _                              => routes.IndexController.onPageLoad()
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

  private def handleUKPostcodeNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(AddressLookupPage()) match {
      case Some(value) if value.isEmpty          => routes.JourneyRecoveryController.onPageLoad()
      case Some(value) if value.length.equals(1) => controllers.manual.sponsor.routes.IsThisAddressForSponsorController.onPageLoad(mode)
      case Some(value)                           => controllers.manual.sponsor.routes.WhatIsAddressForSponsorController.onPageLoad(mode)
      case None                                  => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleWhatIsAddressForSponsorNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(WhatIsAddressForSponsorPage()) match {
      case Some(value) => routes.UnderConstructionController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleIsThisAddressForSponsorNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    (userAnswers.get(IsThisAddressForSponsorPage()), userAnswers.get(WhatIsAddressForSponsorPage())) match {
      case (Some(true), Some(address)) => routes.UnderConstructionController.onPageLoad()
      case (Some(false), Some(_))      => routes.UnderConstructionController.onPageLoad()
      case (_, _)                      => routes.JourneyRecoveryController.onPageLoad()
    }

}
