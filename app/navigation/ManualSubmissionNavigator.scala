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
import models.NumberType.{Iban, Semp}
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.viewModels.AccountId
import pages.*
import pages.manual.account.*
import pages.manual.accountHolders.{IndividualNamePage, IndividualOrOrganisationPage}
import pages.manual.filercategory.{WhatTypeOfFilerIsSponsorPage, WhatTypeOfFilerPage}
import pages.manual.reportdetails.{CrsOrFatcaPage, ReportingYearPage, TypeOfReportPage}
import pages.manual.sponsor.*
import play.api.mvc.Call
import pages.manual.cpso.IndividualNamePage

import javax.inject.{Inject, Singleton}

@Singleton
class ManualSubmissionNavigator @Inject() () {

  def nextPageWithoutReportId(page: Page, mode: Mode): Call = mode match {
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

  private def accountNavigation(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (HaveNumberPage(accountId), mode, ua)         => haveNumberNavigation(accountId, mode, ua)
    case (NumberTypePage(accountId), mode, ua)         => NumberTypeNavigation(accountId, mode, ua)
    case (IdentifierPage(_), mode, ua)                 => controllers.manual.account.routes.AccountClosedController.onPageLoad(mode)
    case (WasAccountOpenPage(_), mode, ua)             => controllers.manual.account.routes.IsJointAccountController.onPageLoad(mode)
    case (IsJointAccountPage(accountId), mode, ua)     => jointAccountRouteLogic(accountId, ua)
    case (HowManyJointAccountHoldersPage(_), mode, ua) => controllers.manual.account.routes.WhatAccountTypeController.onPageLoad(mode)
    case (AccountClosedPage(accountId), mode, ua)      => accountClosedNavigation(accountId, mode, ua)
    case (WhatWasTheAccountBalancePage(_), mode, ua)   => accountBalanceRouteLogic()
    case (IsUndocumentedAccountPage(_), mode, ua)      => controllers.manual.account.routes.IsDormantAccountController.onPageLoad(NormalMode)
    case (WhatWasTheAccountCurrencyPage(_), mode, ua)  => controllers.manual.account.routes.IsUndocumentedAccountController.onPageLoad(NormalMode)
    case (IsDormantAccountPage(_), mode, ua)           => controllers.manual.account.routes.WasAccountOpenController.onPageLoad(NormalMode)
    case (WhatAccountTypePage(_), mode, ua)            => routes.UnderConstructionController.onPageLoad()

  }

  private def NumberTypeNavigation(accountId: AccountId, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) = // THIS IS WRONG CORRECT IT
    userAnswers.get(NumberTypePage(accountId)) match {
      case Some(_) => controllers.manual.account.routes.AccountClosedController.onPageLoad(NormalMode) // Should go to account/number when built
      case None    => routes.JourneyRecoveryController.onPageLoad()
    }

  private def accountBalanceRouteLogic()(implicit reportId: ReportId) =
    if (reportId.regime == FATCA) { routes.UnderConstructionController.onPageLoad() }
    else if (reportId.regime == CRS) { controllers.manual.account.routes.IsUndocumentedAccountController.onPageLoad(NormalMode) }
    else routes.JourneyRecoveryController.onPageLoad()

  private def fillerNavigation: PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (WhatTypeOfFilerPage(), _, _)          => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
    case (WhatTypeOfFilerIsSponsorPage(), _, _) => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
  }

  private def jointAccountRouteLogic(accountId: AccountId, useranswers: UserAnswers)(implicit reportId: ReportId) =
    useranswers
      .get(IsJointAccountPage(accountId))
      .map {
        isJointAccount =>
          if (isJointAccount) controllers.manual.account.routes.HowManyJointAccountHoldersController.onPageLoad(NormalMode)
          else
            controllers.manual.account.routes.WhatAccountTypeController.onPageLoad(NormalMode)
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def accountHolderNavigation(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (IndividualOrOrganisationPage(id), mode, ua) =>
      ua.get(IndividualOrOrganisationPage(id)).fold(controllers.routes.JourneyRecoveryController.onPageLoad()) {
        case Individual   => controllers.manual.accountHolders.routes.IndividualNameController.onPageLoad(mode)
        case Organisation => controllers.routes.UnderConstructionController.onPageLoad()
      }
    case (pages.manual.accountHolders.IndividualNamePage(_), _, _) => controllers.routes.UnderConstructionController.onPageLoad()
  }

  private def cpsoNavigation(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (pages.manual.cpso.IndividualOrOrganisationPage(cpsoId), mode, ua) =>
      ua.get(pages.manual.cpso.IndividualOrOrganisationPage(cpsoId)) match {
        case Some(models.manual.cpso.IndividualOrOrganisation.Individual) => controllers.manual.cpso.routes.IndividualNameController.onPageLoad(mode)
        case Some(_)                                                      => routes.UnderConstructionController.onPageLoad()
        case _                                                            => routes.JourneyRecoveryController.onPageLoad()
      }
    case (pages.manual.cpso.IndividualNamePage(cpsoId), mode, ua) =>
      routes.UnderConstructionController.onPageLoad()
  }

  private def sponsorNavigation(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (HaveSponsorPage(), mode, ua)                      => haveSponsorNavigation(mode, ua)
    case (SponsorNamePage(), mode, _)                       => controllers.manual.sponsor.routes.WhatIsGIINForSponsorController.onPageLoad(mode)
    case (WhatIsGIINForSponsorPage(), mode, _)              => controllers.manual.sponsor.routes.IsSponsorBasedInUKController.onPageLoad(mode)
    case (IsSponsorBasedInUKPage(), mode, ua)               => handleSponsorBasedUKNavigation(ua, mode)
    case (UKPostcodePage(), mode, ua)                       => handleUKPostcodeNavigation(ua, mode)
    case (AddressNonUkPage(), mode, ua)                     => handleNavigationToSponsorResidentTaxView(ua, mode)
    case (WhatIsAddressForSponsorPage(), mode, ua)          => handleWhatIsAddressForSponsorNavigation(ua, mode)
    case (IsThisAddressForSponsorPage(), mode, ua)          => handleIsThisAddressForSponsorNavigation(ua, mode)
    case (UkAddressPage(), mode, ua)                        => handleNavigationToSponsorResidentTaxView(ua, mode)
    case (SponsorResidentForTaxPage(_), mode, ua)           => controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(mode)
    case (DoYouWantToAddTaxResidentCountryPage(), mode, ua) => handleTaxResidentCountriesOptionNavigation(ua)
    case (RemoveTaxResidentCountryPage(), mode, _)          => controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(mode)
  }

  private def handleTaxResidentCountriesOptionNavigation(ua: UserAnswers)(implicit reportId: ReportId): Call =
    ua.get(DoYouWantToAddTaxResidentCountryPage()) match {
      case Some(true) => controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(NormalMode)
      case _          => controllers.manual.sponsor.routes.CheckAnswersController.onPageLoad()
    }

  private def handleNavigationToSponsorResidentTaxView(ua: UserAnswers, mode: Mode)(implicit reportId: ReportId): Call =
    val currentIndex = ua.get(TaxResidentCountriesListPage()).getOrElse(Seq.empty).size
    if currentIndex > 0 then controllers.manual.sponsor.routes.TaxResidentCountriesController.onPageLoad(mode)
    else controllers.manual.sponsor.routes.SponsorResidentForTaxController.onPageLoad(mode)

  private def navigation(implicit reportId: ReportId) =
    accountNavigation orElse sponsorNavigation orElse fillerNavigation orElse accountHolderNavigation orElse cpsoNavigation

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId): Call =
    navigation
      .lift((page, mode, userAnswers))
      .getOrElse(routes.IndexController.onPageLoad())

  private def haveSponsorNavigation(mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(HaveSponsorPage()) match {
      case Some(true)  => controllers.manual.sponsor.routes.SponsorNameController.onPageLoad(mode)
      case Some(false) => controllers.manual.sponsor.routes.CheckAnswersController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def accountClosedNavigation(accountId: AccountId, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    (userAnswers.get(AccountClosedPage(accountId)), reportId.regime) match {
      case (Some(true), CRS)   => controllers.manual.account.routes.WhatWasTheAccountCurrencyController.onPageLoad(mode)
      case (Some(true), FATCA) => controllers.manual.account.routes.WhatWasTheAccountBalanceController.onPageLoad(mode)
      case (Some(false), _)    => controllers.manual.account.routes.WhatWasTheAccountBalanceController.onPageLoad(mode)
      case _                   => routes.JourneyRecoveryController.onPageLoad()
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
