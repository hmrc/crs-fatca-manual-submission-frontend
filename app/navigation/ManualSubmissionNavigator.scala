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
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import pages.manual.sponsor.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ManualSubmissionNavigator @Inject() () {

  def nextPageWithoutReportId(page: Page, mode: Mode): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)
      case CheckMode =>
        routes.UnderConstructionController.onPageLoad()
    }

  private def normalRoutes(page: Page): Call =
    page match {
      case CrsOrFatcaPage    => ReportingYearController.onPageLoad(NormalMode)
      case ReportingYearPage => TypeOfReportController.onPageLoad(NormalMode)
      case TypeOfReportPage  => ReportDetailsCheckAnswersController.onPageLoad()
      case _                 => routes.IndexController.onPageLoad()
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId): Call =
    sectionNavigation
      .lift((page, mode, userAnswers))
      .getOrElse(routes.IndexController.onPageLoad())

  private def sectionNavigation(implicit reportId: ReportId) =
    AccountNavigator.accountNavigation orElse sponsorNavigation orElse fillerNavigation orElse AccountHolderNavigator.navigate orElse CPSONavigator.navigate

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
    case (AddressNonUkPage(), mode, ua)            => handleNavigationToSponsorResidentTaxView(ua, mode)
    case (WhatIsAddressForSponsorPage(), mode, ua) => handleWhatIsAddressForSponsorNavigation(ua, mode)
    case (IsThisAddressForSponsorPage(), mode, ua) => handleIsThisAddressForSponsorNavigation(ua, mode)
    case (UkAddressPage(), mode, ua)               => handleNavigationToSponsorResidentTaxView(ua, mode)
    case (SponsorResidentForTaxPage(_), mode, ua)  => controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(mode)
    case (pages.manual.sponsor.DoYouWantToAddTaxResidentCountryPage(), mode, ua) => handleTaxResidentCountriesOptionNavigation(ua)
    case (pages.manual.sponsor.RemoveTaxResidentCountryPage(), mode, _) => controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(mode)
  }

  private def handleTaxResidentCountriesOptionNavigation(ua: UserAnswers)(implicit reportId: ReportId): Call =
    ua.get(pages.manual.sponsor.DoYouWantToAddTaxResidentCountryPage()) match {
      case Some(true) => controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)
      case _          => controllers.manual.sponsor.routes.CheckAnswersController.onPageLoad()
    }

  private def handleNavigationToSponsorResidentTaxView(ua: UserAnswers, mode: Mode)(implicit reportId: ReportId): Call =
    val currentIndex = ua.get(pages.manual.sponsor.TaxResidentCountriesListPage()).getOrElse(Seq.empty).size
    if currentIndex > 0 then controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(mode)
    else controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(mode)

  private def haveSponsorNavigation(mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(HaveSponsorPage()) match {
      case Some(true)  => controllers.manual.sponsor.routes.SponsorNameController.onPageLoad(mode)
      case Some(false) => controllers.manual.sponsor.routes.CheckAnswersController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleSponsorBasedUKNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(IsSponsorBasedInUKPage()) match {
      case Some(true)  => controllers.manual.sponsor.routes.UKPostcodeController.onPageLoad(mode)
      case Some(false) => controllers.manual.sponsor.routes.AddressNonUkController.onPageLoad(mode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleUKPostcodeNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(pages.manual.sponsor.AddressLookupPage()) match {
      case Some(value) if value.isEmpty          => routes.JourneyRecoveryController.onPageLoad()
      case Some(value) if value.length.equals(1) => controllers.manual.sponsor.routes.IsThisAddressForSponsorController.onPageLoad(mode)
      case Some(value)                           => controllers.manual.sponsor.routes.WhatIsAddressForSponsorController.onPageLoad(mode)
      case None                                  => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleWhatIsAddressForSponsorNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(WhatIsAddressForSponsorPage()) match {
      case Some(value) => handleNavigationToSponsorResidentTaxView(userAnswers, mode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleIsThisAddressForSponsorNavigation(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    (userAnswers.get(IsThisAddressForSponsorPage()), userAnswers.get(WhatIsAddressForSponsorPage())) match {
      case (Some(true), Some(address)) => handleNavigationToSponsorResidentTaxView(userAnswers, mode)
      case (Some(false), _)            => controllers.manual.sponsor.routes.UkAddressController.onPageLoad(mode)
      case (_, _)                      => routes.JourneyRecoveryController.onPageLoad()
    }

}
