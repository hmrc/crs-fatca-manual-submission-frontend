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
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.account.WhatAccountType
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.viewModels.AccountHolderId
import models.viewModels.manual.cpso.CPSOId
import pages.*
import pages.manual.account.WhatAccountTypePage
import pages.manual.accountHolders.{
  AccountHolderAddressNonUkPage,
  AccountHolderIndividualNamePage,
  AccountHolderOrganisationNamePage,
  AccountPage,
  AddressLookupForAccountHolderPage,
  EquityInterestTypePage,
  HaveTaxIdenticationNumberPage,
  IndividualHavePlaceOfBirthPage,
  IndividualOrOrganisationPage,
  IndividualPlaceOfBirthPage,
  IsThisTheAddressForAccountHoldersPage,
  OrganisationCrsPage,
  OrganisationFatcaPage,
  ProvideEquityInterestTypePage,
  ResidentTaxPage,
  SelectAddressPage,
  SelfCertificationPage as AccountHolderSelfCertificationPage,
  TaxIdentificationNumberPage,
  UkAddressPage as AccountHolderUkAddressPage,
  UkPostCodeForAccountHolderPage,
  WhereAreTheyBasedPage
}
import pages.manual.cpso.{
  AddressLookupPage as AddressLookupPageForCPSO,
  AddressUkPage,
  CPSOIndividualDateOfBirthPage,
  CPSOWhereAreTheyBasedPage,
  CpsoOrganisationNamePage,
  CpsoSelfCertificationPage,
  IndividualNamePage,
  IsThisTheAddressPage,
  SelectAddressPage as SelectAddressPageForCPSO,
  UkPostCodePage as UkPostCodePageForCPSO,
  *
}
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
    AccountNavigator.accountNavigation orElse sponsorNavigation orElse fillerNavigation orElse accountHolderNavigation orElse cpsoNavigation

  private def fillerNavigation: PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (WhatTypeOfFilerPage(), _, _)          => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
    case (WhatTypeOfFilerIsSponsorPage(), _, _) => controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad()
  }

  private def accountHolderNavigation(implicit
    reportId: ReportId
  ): PartialFunction[(Page, Mode, UserAnswers), Call] = {

    case (pages.manual.accountHolders.AccountPage(reportId, id), mode, ua) =>
      ua.get(pages.manual.accountHolders.AccountPage(reportId, id)) match {
        case Some(_) =>
          controllers.manual.accountHolders.routes.IndividualOrOrganisationController.onPageLoad(mode)
        case None =>
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

    case (IndividualOrOrganisationPage(id), mode, ua) =>
      ua.get(IndividualOrOrganisationPage(id))
        .fold(controllers.routes.JourneyRecoveryController.onPageLoad()) {
          case Individual =>
            controllers.manual.accountHolders.routes.IndividualNameController.onPageLoad(mode)
          case Organisation =>
            controllers.manual.accountHolders.routes.OrganisationNameController.onPageLoad(mode)
        }
    case (AccountHolderIndividualNamePage(id), mode, ua) =>
      ua.get(AccountHolderIndividualNamePage(id)) match {
        case Some(_) =>
          controllers.manual.accountHolders.routes.IndividualHaveDateOfBirthController.onPageLoad(mode)
        case None =>
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

    case (AccountHolderOrganisationNamePage(id), mode, ua) =>
      ua.get(AccountHolderOrganisationNamePage(id)) match {
        case Some(_) =>
          reportId.regime match {
            case CRS =>
              controllers.manual.accountHolders.routes.OrganisationCrsController.onPageLoad(mode)

            case _ =>
              controllers.manual.accountHolders.routes.OrganisationFatcaController.onPageLoad(mode)
          }

        case None =>
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    case (OrganisationCrsPage(id), mode, ua) =>
      ua.get(OrganisationCrsPage(id)) match {
        case Some(_) =>
          controllers.manual.accountHolders.routes.WhereAreTheyBasedController.onPageLoad(mode)
        case None =>
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    case (OrganisationFatcaPage(id), mode, ua) =>
      ua.get(OrganisationFatcaPage(id)) match {
        case Some(_) =>
          controllers.manual.accountHolders.routes.WhereAreTheyBasedController.onPageLoad(mode)
        case None =>
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }

    case (pages.manual.accountHolders.IndividualHavePlaceOfBirthPage(id), mode, ua) => handleIndividualHavePlaceOfBirthNavigation(id, mode, ua)
    case (pages.manual.accountHolders.WhereAreTheyBasedPage(id), mode, ua) =>
      ua.get(WhereAreTheyBasedPage(id)) match {
        case Some(true)  => controllers.manual.accountHolders.routes.UkPostCodeForAccountHolderController.onPageLoad(mode)
        case Some(false) => controllers.manual.accountHolders.routes.AccountHolderAddressNonUkController.onPageLoad(mode)
        case None        => controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    case (pages.manual.accountHolders.IndividualHaveDateOfBirthPage(id), mode, ua) =>
      ua.get(pages.manual.accountHolders.IndividualHaveDateOfBirthPage(id)) match {
        case Some(true) =>
          controllers.manual.accountHolders.routes.IndividualDateOfBirthController.onPageLoad(mode)
        case Some(false) =>
          controllers.manual.accountHolders.routes.IndividualHavePlaceOfBirthController.onPageLoad(mode)
        case None =>
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    case (pages.manual.accountHolders.IndividualDateOfBirthPage(id), mode, ua) =>
      ua.get(pages.manual.accountHolders.IndividualDateOfBirthPage(id)) match {
        case Some(_) => controllers.manual.accountHolders.routes.IndividualHavePlaceOfBirthController.onPageLoad(mode)
        case None =>
          controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    case (UkPostCodeForAccountHolderPage(accountHolderId, reportId), mode, ua) => handleUKPostcodeNavigationForAccountHolders(ua, mode, accountHolderId)
    case (IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId), mode, ua) =>
      handleIsThisTheAddressForAccountHoldersRouting(ua, mode, accountHolderId)
    case (AccountHolderUkAddressPage(accountHolderId, reportId), mode, ua) =>
      handleAddressUKPageNavigationForAccountHolders(ua, mode, accountHolderId)
    case (SelectAddressPage(accountHolderId, reportId), mode, ua) => handleSelectAddressPageNavigationForAccountHolders(ua, mode, accountHolderId)
    case (AccountHolderAddressNonUkPage(accountHolderId, reportId), mode, ua) =>
      handleAccountHolderAddressNonUkPageNavigationForAccountHolders(ua, mode, accountHolderId)
    case (AccountHolderSelfCertificationPage(accountHolderId, reportId), mode, ua) =>
      handleAccountHolderSelfCertificationPageNavigationForAccountHolders(ua, mode, accountHolderId)
    case (IndividualPlaceOfBirthPage(_, _), mode, _) => controllers.manual.accountHolders.routes.WhereAreTheyBasedController.onPageLoad(mode)
    case (ProvideEquityInterestTypePage(id, reportId), mode, ua) =>
      ua.get(ProvideEquityInterestTypePage(id, reportId)) match {
        case Some(true)  => controllers.manual.accountHolders.routes.EquityInterestTypeController.onPageLoad(mode)
        case Some(false) => controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(mode)
        case None        => routes.JourneyRecoveryController.onPageLoad()
      }
    case (EquityInterestTypePage(_, _), mode, _)                   => controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(mode)
    case (ResidentTaxPage(_, accountHolderId, reportId), mode, ua) => handleResidentTaxPageNavigationForAccountHolders(mode)
    case (HaveTaxIdenticationNumberPage(index, accountHolderId, reportId), mode, ua) =>
      handleHaveTaxIdenticationNumberPageNavigationForAccountHolders(ua, mode, accountHolderId, index)
    case (TaxIdentificationNumberPage(_, _, _), mode, ua) => routes.UnderConstructionController.onPageLoad()
  }

  private def cpsoNavigation(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (pages.manual.cpso.IndividualOrOrganisationPage(cpsoId), mode, ua) =>
      ua.get(pages.manual.cpso.IndividualOrOrganisationPage(cpsoId)) match {
        case Some(models.manual.cpso.IndividualOrOrganisation.Individual)   => controllers.manual.cpso.routes.IndividualNameController.onPageLoad(mode)
        case Some(models.manual.cpso.IndividualOrOrganisation.Organisation) => controllers.manual.cpso.routes.CpsoOrganisationNameController.onPageLoad(mode)
        case _                                                              => routes.JourneyRecoveryController.onPageLoad()
      }
    case (pages.manual.cpso.IndividualNamePage(cpsoId), mode, ua) => controllers.manual.cpso.routes.IndividualHaveDateOfBirthController.onPageLoad(mode)
    case (CpsoOrganisationNamePage(cpsoId, reportId), mode, ua)   => controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(mode)
    case (CpsoSelfCertificationPage(cpsoId, reportId), mode, ua)  => controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(mode)
    case (CPSOIndividualDateOfBirthPage(cpsoId, reportId), mode, ua) =>
      controllers.manual.cpso.routes.IndividualHavePlaceOfBirthController.onPageLoad(mode)
    case (pages.manual.cpso.IndividualHavePlaceOfBirthPage(cpsoId, reportId), mode, ua) =>
      ua.get(pages.manual.cpso.IndividualHavePlaceOfBirthPage(cpsoId, reportId)) match {
        case Some(true) => controllers.manual.cpso.routes.IndividualPlaceOfBirthController.onPageLoad(mode)
        case Some(false) =>
          reportId.regime match {
            case SubmissionsConstants.FATCA => controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(mode)
            case SubmissionsConstants.CRS   => controllers.manual.cpso.routes.ControllingPersonTypeController.onPageLoad(mode)
            case _                          => routes.UnderConstructionController.onPageLoad()
          }
        case None => routes.JourneyRecoveryController.onPageLoad()
      }
    case (pages.manual.cpso.IndividualPlaceOfBirthPage(cpsoId, reportId), mode, ua) =>
      reportId.regime match {
        case SubmissionsConstants.FATCA => controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(mode)
        case SubmissionsConstants.CRS   => controllers.manual.cpso.routes.ControllingPersonTypeController.onPageLoad(mode)
        case _                          => routes.UnderConstructionController.onPageLoad()
      }
    case (CPSOWhereAreTheyBasedPage(cpsoId, reportId), mode, ua) =>
      ua.get(CPSOWhereAreTheyBasedPage(cpsoId, reportId)) match {
        case Some(false) => controllers.manual.cpso.routes.CPSOAddressNonUkController.onPageLoad(mode)
        case Some(true)  => controllers.manual.cpso.routes.UkPostCodeController.onPageLoad(mode)
        case None        => routes.JourneyRecoveryController.onPageLoad()
      }
    case (CPSOAddressNonUkPage(cpsoId, reportId), mode, ua) =>
      routes.UnderConstructionController.onPageLoad()
    case (UkPostCodePageForCPSO(cpsoId, reportId), mode, ua)    => handleUKPostcodeNavigationForCPSO(ua, mode, cpsoId)
    case (IsThisTheAddressPage(cpsoId, reportId), mode, ua)     => handleIsThisTheAddressNavigationForCPSO(ua, mode, cpsoId)
    case (SelectAddressPageForCPSO(cpsoId, reportId), mode, ua) => handleSelectAddressNavigationForCPSO(ua, mode, cpsoId)
    case (AddressUkPage(cpsoId, reportId), mode, ua)            => routes.UnderConstructionController.onPageLoad()

    case (pages.manual.cpso.IndividualHaveDateOfBirthPage(reportId, cpsoId), mode, ua) =>
      ua.get(pages.manual.cpso.IndividualHaveDateOfBirthPage(reportId, cpsoId)) match {
        case Some(false) => controllers.manual.cpso.routes.IndividualHavePlaceOfBirthController.onPageLoad(mode)
        case Some(true)  => controllers.manual.cpso.routes.CPSOIndividualDateOfBirthController.onPageLoad(mode)
        case None        => routes.JourneyRecoveryController.onPageLoad()
      }
    case (pages.manual.cpso.ControllingPersonTypePage(reportId, cpsoId), mode, ua) =>
      controllers.manual.cpso.routes.CpsoSelfCertificationController.onPageLoad(mode)
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

  private def handleAccountHolderAddressNonUkPageNavigationForAccountHolders(userAnswers: UserAnswers, mode: Mode, accountHolderId: AccountHolderId)(implicit
    reportId: ReportId
  ) =
    if (reportId.regime == FATCA) {
      userAnswers.get(pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, reportId)) match {
        case Some(countries) if countries.isEmpty => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
        case Some(_)                              => controllers.routes.UnderConstructionController.onPageLoad()
        case None                                 => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
      }
    } else {
      handleEquityInterestRouting(userAnswers, mode, accountHolderId)
    }

  private def handleSelectAddressPageNavigationForAccountHolders(userAnswers: UserAnswers, mode: Mode, accountHolderId: AccountHolderId)(implicit
    reportId: ReportId
  ) =
    if (reportId.regime == FATCA) {
      userAnswers.get(pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, reportId)) match {
        case Some(countries) if countries.isEmpty => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
        case Some(_)                              => controllers.routes.UnderConstructionController.onPageLoad()
        case None                                 => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
      }
    } else {
      handleEquityInterestRouting(userAnswers, mode, accountHolderId)
    }

  private def handleHaveTaxIdenticationNumberPageNavigationForAccountHolders(userAnswers: UserAnswers,
                                                                             mode: Mode,
                                                                             accountHolderId: AccountHolderId,
                                                                             index: Int
  )(implicit
    reportId: ReportId
  ) = userAnswers.get(HaveTaxIdenticationNumberPage(index, accountHolderId, reportId)) match {
    case Some(true)  => controllers.manual.accountHolders.routes.TaxIdentificationNumberController.onPageLoad(mode)
    case Some(false) => routes.UnderConstructionController.onPageLoad()
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def handleResidentTaxPageNavigationForAccountHolders(mode: Mode)(implicit
    reportId: ReportId
  ) =
    if (reportId.regime == CRS) controllers.manual.accountHolders.routes.HaveTaxIdenticationNumberController.onPageLoad(mode)
    else controllers.manual.accountHolders.routes.TaxIdentificationNumberController.onPageLoad(mode)

  private def handleAccountHolderSelfCertificationPageNavigationForAccountHolders(userAnswers: UserAnswers, mode: Mode, accountHolderId: AccountHolderId)(
    implicit reportId: ReportId
  ) =
    userAnswers.get(pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, reportId)) match {
      case Some(countries) if countries.isEmpty => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
      case Some(_)                              => controllers.routes.UnderConstructionController.onPageLoad()
      case None                                 => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
    }

  private def handleAddressUKPageNavigationForAccountHolders(userAnswers: UserAnswers, mode: Mode, accountHolderId: AccountHolderId)(implicit
    reportId: ReportId
  ) =
    if (reportId.regime == FATCA) {
      userAnswers.get(pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, reportId)) match {
        case Some(countries) if countries.isEmpty => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
        case Some(_)                              => controllers.routes.UnderConstructionController.onPageLoad()
        case None                                 => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
      }
    } else {
      handleEquityInterestRouting(userAnswers, mode, accountHolderId)
    }

  private def handleUKPostcodeNavigationForAccountHolders(userAnswers: UserAnswers, mode: Mode, accountHolderId: AccountHolderId)(implicit reportId: ReportId) =
    userAnswers.get(AddressLookupForAccountHolderPage(accountHolderId, reportId)) match {
      case Some(value) if value.isEmpty          => routes.JourneyRecoveryController.onPageLoad()
      case Some(value) if value.length.equals(1) => controllers.manual.accountHolders.routes.IsThisTheAddressForAccountHoldersController.onPageLoad(mode)
      case Some(value)                           => controllers.manual.accountHolders.routes.SelectAddressController.onPageLoad(mode)
      case None                                  => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleUKPostcodeNavigationForCPSO(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    userAnswers.get(AddressLookupPageForCPSO(cpsoId, reportId)) match {
      case Some(value) if value.isEmpty          => routes.JourneyRecoveryController.onPageLoad()
      case Some(value) if value.length.equals(1) => controllers.manual.cpso.routes.IsThisTheAddressController.onPageLoad(mode)
      case Some(value)                           => controllers.manual.cpso.routes.SelectAddressController.onPageLoad(mode)
      case None                                  => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleIsThisTheAddressNavigationForCPSO(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    userAnswers.get(IsThisTheAddressPage(cpsoId, reportId)) match {
      case Some(value) if value => controllers.routes.UnderConstructionController.onPageLoad()
      case Some(_)              => controllers.manual.cpso.routes.AddressUkController.onPageLoad(mode)
      case None                 => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleSelectAddressNavigationForCPSO(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    userAnswers.get(SelectAddressPageForCPSO(cpsoId, reportId)) match {
      case Some(value) => controllers.routes.UnderConstructionController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleIsThisTheAddressForAccountHoldersRouting(userAnswers: UserAnswers, mode: Mode, accountHolderId: AccountHolderId)(implicit
    reportId: ReportId
  ) =
    userAnswers.get(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId)) match {
      case Some(true) =>
        if (reportId.regime == FATCA) {
          userAnswers.get(pages.manual.accountHolders.TaxResidentCountriesListPage(accountHolderId, reportId)) match {
            case Some(countries) if countries.isEmpty => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
            case Some(_)                              => controllers.routes.UnderConstructionController.onPageLoad()
            case None                                 => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
          }
        } else {
          handleEquityInterestRouting(userAnswers, mode, accountHolderId)
        }

      case Some(false) => controllers.manual.accountHolders.routes.UkAddressController.onPageLoad(mode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleEquityInterestRouting(ua: UserAnswers, mode: Mode, accountHolderId: AccountHolderId)(implicit reportId: ReportId): Call =
    reportId.regime match {
      case CRS =>
        ua.get(AccountPage(reportId, accountHolderId)) match {
          case Some(accountIds) if accountIds.nonEmpty =>
            val accountTypes = accountIds.map(
              id => ua.get(WhatAccountTypePage(id))
            )

            if (accountTypes.exists(_.isEmpty)) routes.JourneyRecoveryController.onPageLoad()
            else if (accountTypes.flatten.contains(WhatAccountType.InvestmentEntity))
              controllers.manual.accountHolders.routes.ProvideEquityInterestTypeController.onPageLoad(mode)
            else
              controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(mode)

          case _ => routes.JourneyRecoveryController.onPageLoad()
        }
      case _ => routes.JourneyRecoveryController.onPageLoad()
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

  private def handleIndividualHavePlaceOfBirthNavigation(accountId: AccountHolderId, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(IndividualHavePlaceOfBirthPage(accountId)) match {
      case Some(true)  => controllers.manual.accountHolders.routes.IndividualPlaceOfBirthController.onPageLoad(mode)
      case Some(false) => controllers.manual.accountHolders.routes.WhereAreTheyBasedController.onPageLoad(mode)
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

}
