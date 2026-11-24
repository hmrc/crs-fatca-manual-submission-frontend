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
import models.viewModels.manual.cpso.CPSOId
import pages.*
import pages.manual.cpso.{
  AddressLookupPage as AddressLookupPageForCPSO,
  AddressUkPage,
  CPSOIndividualDateOfBirthPage,
  CPSOWhereAreTheyBasedPage,
  CpsoOrganisationNamePage,
  CpsoSelfCertificationPage,
  IsThisTheAddressPage,
  SelectAddressPage as SelectAddressPageForCPSO,
  UkPostCodePage as UkPostCodePageForCPSO,
  *
}
import play.api.mvc.Call

import javax.inject.Singleton

@Singleton
object CPSONavigator {

  def navigate(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (page, mode, ua) if cpsoRoutes.isDefinedAt((page, mode, ua)) => cpsoRoutes(reportId)(page, mode, ua)
  }

  private def cpsoRoutes(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (IndividualOrOrganisationPage(cpsoId), mode, ua) =>
      ua.get(IndividualOrOrganisationPage(cpsoId)) match {
        case Some(models.manual.cpso.IndividualOrOrganisation.Individual) =>
          controllers.manual.cpso.routes.IndividualNameController.onPageLoad(mode)
        case Some(models.manual.cpso.IndividualOrOrganisation.Organisation) =>
          controllers.manual.cpso.routes.CpsoOrganisationNameController.onPageLoad(mode)
        case _ => routes.JourneyRecoveryController.onPageLoad()
      }
    case (IndividualNamePage(cpsoId), mode, _) =>
      controllers.manual.cpso.routes.IndividualHaveDateOfBirthController.onPageLoad(mode)
    case (CpsoOrganisationNamePage(cpsoId, _), mode, _) =>
      controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(mode)
    case (CpsoSelfCertificationPage(cpsoId, _), mode, _) =>
      controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(mode)
    case (CPSOIndividualDateOfBirthPage(cpsoId, _), mode, _) =>
      controllers.manual.cpso.routes.IndividualHavePlaceOfBirthController.onPageLoad(mode)
    case (IndividualHavePlaceOfBirthPage(cpsoId, reportId), mode, ua) =>
      ua.get(IndividualHavePlaceOfBirthPage(cpsoId, reportId)) match {
        case Some(true) => controllers.manual.cpso.routes.IndividualPlaceOfBirthController.onPageLoad(mode)
        case Some(false) =>
          reportId.regime match {
            case FATCA => controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(mode)
            case CRS   => controllers.manual.cpso.routes.ControllingPersonTypeController.onPageLoad(mode)
            case _     => routes.UnderConstructionController.onPageLoad()
          }
        case None => routes.JourneyRecoveryController.onPageLoad()
      }
    case (IndividualPlaceOfBirthPage(cpsoId, _), mode, _) =>
      reportId.regime match {
        case FATCA => controllers.manual.cpso.routes.CPSOWhereAreTheyBasedController.onPageLoad(mode)
        case CRS   => controllers.manual.cpso.routes.ControllingPersonTypeController.onPageLoad(mode)
        case _     => routes.UnderConstructionController.onPageLoad()
      }
    case (CPSOWhereAreTheyBasedPage(cpsoId, reportId), mode, ua) =>
      ua.get(CPSOWhereAreTheyBasedPage(cpsoId, reportId)) match {
        case Some(false) => controllers.manual.cpso.routes.CPSOAddressNonUkController.onPageLoad(mode)
        case Some(true)  => controllers.manual.cpso.routes.UkPostCodeController.onPageLoad(mode)
        case None        => routes.JourneyRecoveryController.onPageLoad()
      }
    case (CPSOAddressNonUkPage(cpsoId, _), mode, ua)     => handleAddressNonUkNavigation(ua, mode, cpsoId)
    case (UkPostCodePageForCPSO(cpsoId, _), mode, ua)    => handleUKPostcodeNavigation(ua, mode, cpsoId)
    case (IsThisTheAddressPage(cpsoId, _), mode, ua)     => handleIsThisTheAddressNavigation(ua, mode, cpsoId)
    case (SelectAddressPageForCPSO(cpsoId, _), mode, ua) => handleSelectAddressNavigation(ua, mode, cpsoId)
    case (AddressUkPage(cpsoId, _), mode, ua)            => handleAddressUkNavigation(ua, mode, cpsoId)
    case (IndividualHaveDateOfBirthPage(reportId, cpsoId), mode, ua) =>
      ua.get(IndividualHaveDateOfBirthPage(reportId, cpsoId)) match {
        case Some(false) => controllers.manual.cpso.routes.IndividualHavePlaceOfBirthController.onPageLoad(mode)
        case Some(true)  => controllers.manual.cpso.routes.CPSOIndividualDateOfBirthController.onPageLoad(mode)
        case None        => routes.JourneyRecoveryController.onPageLoad()
      }
    case (ControllingPersonTypePage(_, _), mode, _) =>
      controllers.manual.cpso.routes.CpsoSelfCertificationController.onPageLoad(mode)
    case (ResidentForTaxPage(_, cpsoId, _), mode, _) => handleResidentForTaxNavigation(mode)
    case (HaveTaxIdentificationNumberPage(index, cpsoId, _), mode, ua) =>
      handleHaveTaxIdentificationNumberNavigation(index, ua, mode, cpsoId)
    case (TaxIdentificationNumberPage(index, cpsoId, reportId), mode, ua) =>
      handleTaxIdentificationNumberPageNavigation(index, ua, mode, cpsoId)
    case (DoYouWantToAddTaxResidentCountryPage(cpsoId, reportId), mode, ua) =>
      handleDoYouWantToAddTaxResidentCountryNavigation(ua, mode, cpsoId)
    case (RemoveTaxResidentCountryPage(cpsoId, _), mode, _) =>
      controllers.manual.cpso.routes.TaxResidentCountriesController.onPageLoad(mode)
    case (AccountHolderIdsPage(cpsoId, reportId), mode, ua) =>
      handleAccountHolderNavigation(ua, mode, cpsoId)
    case (IsCpsoSectionCompletePage(_), _, _) =>
      controllers.manual.routes.SendAReportController.onPageLoad()
    case (DoYouWantToAddAnotherCpsoPage(), mode, ua) =>
      handleDoYouWantToAddAnotherCpso(ua, mode)
    case (pages.manual.cpso.RemoveCPSOPage(_, _), mode, ua) =>
      controllers.manual.cpso.routes.ControllingPersonsOrSubstantialOwnersController.onPageLoad(mode)
  }

  private def handleUKPostcodeNavigation(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    userAnswers.get(AddressLookupPageForCPSO(cpsoId, reportId)) match {
      case Some(value) if value.isEmpty          => routes.JourneyRecoveryController.onPageLoad()
      case Some(value) if value.length.equals(1) => controllers.manual.cpso.routes.IsThisTheAddressController.onPageLoad(mode)
      case Some(_)                               => controllers.manual.cpso.routes.SelectAddressController.onPageLoad(mode)
      case None                                  => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleIsThisTheAddressNavigation(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    userAnswers.get(IsThisTheAddressPage(cpsoId, reportId)) match {
      case Some(true) =>
        userAnswers.get(TaxResidentCountriesListPage(cpsoId, reportId)) match {
          case Some(countries) if countries.isEmpty => controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(mode)
          case Some(_)                              => controllers.manual.cpso.routes.TaxResidentCountriesController.onPageLoad(mode)
          case None                                 => controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(mode)
        }
      case Some(false) => controllers.manual.cpso.routes.AddressUkController.onPageLoad(mode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleSelectAddressNavigation(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    handleAddressTaxResidentNavigation(userAnswers, mode, cpsoId)

  private def handleAddressUkNavigation(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    handleAddressTaxResidentNavigation(userAnswers, mode, cpsoId)

  private def handleAddressNonUkNavigation(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    handleAddressTaxResidentNavigation(userAnswers, mode, cpsoId)

  private def handleAddressTaxResidentNavigation(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    userAnswers.get(TaxResidentCountriesListPage(cpsoId, reportId)) match {
      case Some(countries) if countries.isEmpty => controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(mode)
      case Some(_)                              => controllers.manual.cpso.routes.TaxResidentCountriesController.onPageLoad(NormalMode)
      case None                                 => controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(mode)
    }

  private def handleResidentForTaxNavigation(mode: Mode)(implicit reportId: ReportId) =
    if (reportId.regime == CRS) controllers.manual.cpso.routes.HaveTaxIdentificationNumberController.onPageLoad(mode)
    else controllers.manual.cpso.routes.TaxIdentificationNumberController.onPageLoad(mode)

  private def handleHaveTaxIdentificationNumberNavigation(index: Int, userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit
    reportId: ReportId
  ) =
    userAnswers.get(HaveTaxIdentificationNumberPage(index, cpsoId, reportId)) match {
      case Some(true)  => controllers.manual.cpso.routes.TaxIdentificationNumberController.onPageLoad(mode)
      case Some(false) => controllers.manual.cpso.routes.TaxResidentCountriesController.onPageLoad(mode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleTaxIdentificationNumberPageNavigation(index: Int, userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit
    reportId: ReportId
  ) =
    userAnswers.get(TaxIdentificationNumberPage(index, cpsoId, reportId)) match {
      case Some(_) => controllers.manual.cpso.routes.TaxResidentCountriesController.onPageLoad(mode)
      case None    => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleDoYouWantToAddTaxResidentCountryNavigation(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit
    reportId: ReportId
  ) =
    userAnswers.get(DoYouWantToAddTaxResidentCountryPage(cpsoId, reportId)) match {
      case Some(true)  => controllers.manual.cpso.routes.ResidentForTaxController.onPageLoad(mode)
      case Some(false) => controllers.manual.cpso.routes.CPSOCheckAnswersController.onPageLoad()
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleAccountHolderNavigation(userAnswers: UserAnswers, mode: Mode, cpsoId: CPSOId)(implicit reportId: ReportId) =
    userAnswers.get(AccountHolderIdsPage(cpsoId, reportId)) match {
      case Some(_) =>
        reportId.regime match {
          case CRS   => controllers.manual.cpso.routes.IndividualNameController.onPageLoad(mode)
          case FATCA => controllers.manual.cpso.routes.IndividualOrOrganisationController.onPageLoad(mode)
          case _     => routes.JourneyRecoveryController.onPageLoad()
        }
      case None => routes.JourneyRecoveryController.onPageLoad()
    }

  private def handleDoYouWantToAddAnotherCpso(userAnswers: UserAnswers, mode: Mode)(implicit reportId: ReportId) =
    userAnswers.get(DoYouWantToAddAnotherCpsoPage()) match {
      case Some(true)  => controllers.manual.cpso.routes.AccountHolderController.onPageLoad(mode)
      case Some(false) => controllers.manual.cpso.routes.IsCpsoSectionCompleteController.onPageLoad()
      case None        => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
}
