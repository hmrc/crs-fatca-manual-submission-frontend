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
import models.SubmissionsConstants.{CRS, FATCA}
import models.viewModels.AccountId
import pages.*
import pages.manual.account.*
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
    case (HaveNumberPage(accountId), mode, ua)        => haveNumberNavigation(accountId, mode, ua)
    case (NumberTypePage(_), mode, ua)                => routes.UnderConstructionController.onPageLoad()
    case (IdentifierPage(_), mode, ua)                => controllers.manual.account.routes.AccountClosedController.onPageLoad(mode)
    case (AccountClosedPage(accountId), mode, ua)     => accountClosedNavigation(accountId, mode, ua)
    case (WhatWasTheAccountBalancePage(_), mode, ua)  => routes.UnderConstructionController.onPageLoad()
    case (WhatWasTheAccountCurrencyPage(_), mode, ua) => routes.UnderConstructionController.onPageLoad()
  }

  private def fillerNavigation: PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (WhatTypeOfFilerPage(), _, _)          => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
    case (WhatTypeOfFilerIsSponsorPage(), _, _) => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
  }

  private def sponsorNavigation(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (HaveSponsorPage(), mode, ua)             => haveSponsorNavigation(mode, ua)
    case (SponsorNamePage(), mode, _)              => controllers.manual.sponsor.routes.WhatIsGIINForSponsorController.onPageLoad(mode)
    case (WhatIsGIINForSponsorPage(), mode, _)     => controllers.manual.sponsor.routes.IsSponsorBasedInUKController.onPageLoad(mode)
    case (IsSponsorBasedInUKPage(), mode, ua)      => handleSponsorBasedUKNavigation(ua, mode)
    case (UKPostcodePage(), mode, ua)              => handleUKPostcodeNavigation(ua, mode)
    case (AddressNonUkPage(), _, _)                => routes.UnderConstructionController.onPageLoad()
    case (WhatIsAddressForSponsorPage(), mode, ua) => handleWhatIsAddressForSponsorNavigation(ua)
    case (IsThisAddressForSponsorPage(), mode, ua) => handleIsThisAddressForSponsorNavigation(ua, mode)
    case (UkAddressPage(), _, _)                   => routes.UnderConstructionController.onPageLoad()
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

  private def accountClosedNavigation(accountId: AccountId, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    (userAnswers.get(AccountClosedPage(accountId)), reportId.regime) match {
      case (Some(false), CRS)   => controllers.manual.account.routes.WhatWasTheAccountCurrencyController.onPageLoad(mode)
      case (Some(false), FATCA) => controllers.manual.account.routes.WhatWasTheAccountBalanceController.onPageLoad(mode)
      case (Some(true), _)      => controllers.manual.account.routes.WhatWasTheAccountBalanceController.onPageLoad(mode)
      case _                    => routes.JourneyRecoveryController.onPageLoad()
    }

  private def haveNumberNavigation(accountId: AccountId, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(HaveNumberPage(accountId)) match {
      case Some(true)  => account.routes.NumberTypeController.onPageLoad(mode)
      case Some(false) => account.routes.IdentifierController.onPageLoad(mode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleSponsorBasedUKNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(IsSponsorBasedInUKPage()) match {
      case Some(true)  => controllers.manual.sponsor.routes.UKPostcodeController.onPageLoad(mode)
      case Some(false) => controllers.manual.sponsor.routes.AddressNonUkController.onPageLoad(mode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleUKPostcodeNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(AddressLookupPage()) match {
      case Some(value) if value.isEmpty          => routes.JourneyRecoveryController.onPageLoad()
      case Some(value) if value.length.equals(1) => controllers.manual.sponsor.routes.IsThisAddressForSponsorController.onPageLoad(mode)
      case Some(value)                           => controllers.manual.sponsor.routes.WhatIsAddressForSponsorController.onPageLoad(mode)
      case None                                  => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleWhatIsAddressForSponsorNavigation(userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(WhatIsAddressForSponsorPage()) match {
      case Some(value) => routes.UnderConstructionController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleIsThisAddressForSponsorNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    (userAnswers.get(IsThisAddressForSponsorPage()), userAnswers.get(WhatIsAddressForSponsorPage())) match {
      case (Some(true), Some(address)) => routes.UnderConstructionController.onPageLoad()
      case (Some(false), Some(_))      => controllers.manual.sponsor.routes.UkAddressController.onPageLoad(mode)
      case (_, _)                      => routes.JourneyRecoveryController.onPageLoad()
    }

}
