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

import controllers.routes
import models.*
import models.SubmissionsConstants.{CRS, FATCA}
import models.manual.accountHolders.IndividualOrOrganisation
import models.viewModels.{AccountHolderId, AccountId}
import pages.*
import pages.manual.accountHolders.{
  AccountHolderAddressNonUkPage,
  AccountHolderIndividualNamePage,
  AccountHolderOrganisationNamePage,
  AccountPage,
  AddressLookupForAccountHolderPage,
  DoYouWantToAddTaxResidentCountryPage as AccHolderDoYouWantToAddTaxResidentCountryPage,
  HaveTaxIdenticationNumberPage,
  IndividualHavePlaceOfBirthPage,
  IndividualOrOrganisationPage,
  IndividualPlaceOfBirthPage,
  IsThisTheAddressForAccountHoldersPage,
  OrganisationCrsPage,
  OrganisationFatcaPage,
  RemoveTaxResidentCountryPage as AccRemoveTaxResidentCountryPage,
  ResidentTaxPage,
  SelectAddressPage,
  SelfCertificationPage as AccountHolderSelfCertificationPage,
  TaxIdentificationNumberPage,
  UkAddressPage as AccountHolderUkAddressPage,
  UkPostCodeForAccountHolderPage,
  WhereAreTheyBasedPage
}
import play.api.mvc.Call

import javax.inject.Singleton

@Singleton
object AccountHolderNavigator {

  def navigate(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (page, mode, ua) if accHolderRoute.isDefinedAt((page, mode, ua)) => accHolderRoute(reportId)(page, mode, ua)
  }

  private def accHolderRoute(implicit
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
          case IndividualOrOrganisation.Individual =>
            controllers.manual.accountHolders.routes.IndividualNameController.onPageLoad(mode)
          case IndividualOrOrganisation.Organisation =>
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

            case FATCA =>
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
    case (IndividualPlaceOfBirthPage(_, _), mode, _)               => controllers.manual.accountHolders.routes.WhereAreTheyBasedController.onPageLoad(mode)
    case (ResidentTaxPage(_, accountHolderId, reportId), mode, ua) => handleResidentTaxPageNavigationForAccountHolders(mode)
    case (HaveTaxIdenticationNumberPage(index, accountHolderId, reportId), mode, ua) =>
      handleHaveTaxIdenticationNumberPageNavigationForAccountHolders(ua, mode, accountHolderId, index)
    case (TaxIdentificationNumberPage(_, _, _), mode, ua) => controllers.manual.accountHolders.routes.TaxResidentCountriesController.onPageLoad(mode)
    case (AccHolderDoYouWantToAddTaxResidentCountryPage(accountHolderId, reportId), mode, ua) =>
      ua.get(AccHolderDoYouWantToAddTaxResidentCountryPage(accountHolderId, reportId)) match {
        case Some(true)  => controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(mode)
        case Some(false) => controllers.routes.UnderConstructionController.onPageLoad()
        case None        => controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    case (AccRemoveTaxResidentCountryPage(_, _), mode, _) => controllers.manual.accountHolders.routes.TaxResidentCountriesController.onPageLoad(mode)
  }

  private def handleIndividualHavePlaceOfBirthNavigation(accountId: AccountHolderId, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(IndividualHavePlaceOfBirthPage(accountId)) match {
      case Some(true)  => controllers.manual.accountHolders.routes.IndividualPlaceOfBirthController.onPageLoad(mode)
      case Some(false) => controllers.manual.accountHolders.routes.WhereAreTheyBasedController.onPageLoad(mode)
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleUKPostcodeNavigationForAccountHolders(userAnswers: UserAnswers, mode: Mode, accountHolderId: AccountHolderId)(implicit reportId: ReportId) =
    userAnswers.get(AddressLookupForAccountHolderPage(accountHolderId, reportId)) match {
      case Some(value) if value.isEmpty          => routes.JourneyRecoveryController.onPageLoad()
      case Some(value) if value.length.equals(1) => controllers.manual.accountHolders.routes.IsThisTheAddressForAccountHoldersController.onPageLoad(mode)
      case Some(value)                           => controllers.manual.accountHolders.routes.SelectAddressController.onPageLoad(mode)
      case None                                  => routes.JourneyRecoveryController.onPageLoad()
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
          controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(mode)
        }

      case Some(false) => controllers.manual.accountHolders.routes.UkAddressController.onPageLoad(mode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
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
      controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(mode)
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
      controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(mode)
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
      controllers.manual.accountHolders.routes.SelfCertificationController.onPageLoad(mode)
    }

  private def handleHaveTaxIdenticationNumberPageNavigationForAccountHolders(userAnswers: UserAnswers,
                                                                             mode: Mode,
                                                                             accountHolderId: AccountHolderId,
                                                                             index: Int
  )(implicit
    reportId: ReportId
  ) = userAnswers.get(HaveTaxIdenticationNumberPage(index, accountHolderId, reportId)) match {
    case Some(true)  => controllers.manual.accountHolders.routes.TaxIdentificationNumberController.onPageLoad(mode)
    case Some(false) => controllers.manual.accountHolders.routes.TaxResidentCountriesController.onPageLoad(mode)
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
}
