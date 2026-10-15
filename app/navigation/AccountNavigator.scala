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
import controllers.routes
import models.*
import models.SubmissionsConstants.{CRS, FATCA}
import models.viewModels.AccountId
import pages.*
import pages.manual.account.*
import play.api.mvc.Call

import javax.inject.Singleton

@Singleton
object AccountNavigator {

  def accountNavigation(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (page, mode, ua) if accountRoutes.isDefinedAt((page, mode, ua)) => accountRoutes(reportId)(page, mode, ua)
  }

  def accountRoutes(implicit reportId: ReportId): PartialFunction[(Page, Mode, UserAnswers), Call] = {
    case (HaveNumberPage(accountId), mode, ua)                    => haveNumberNavigation(accountId, mode, ua)
    case (NumberTypePage(accountId), mode, ua)                    => NumberTypeNavigation(accountId, mode, ua)
    case (IdentifierPage(_), mode, ua)                            => controllers.manual.account.routes.AccountClosedController.onPageLoad(mode)
    case (WasAccountOpenPage(_), mode, ua)                        => controllers.manual.account.routes.IsJointAccountController.onPageLoad(mode)
    case (IsJointAccountPage(accountId), mode, ua)                => jointAccountRouteLogic(accountId, mode, ua)
    case (HowManyJointAccountHoldersPage(_), mode, ua)            => controllers.manual.account.routes.WhatAccountTypeController.onPageLoad(mode)
    case (AccountClosedPage(accountId), mode, ua)                 => accountClosedNavigation(accountId, mode, ua)
    case (WhatWasTheAccountBalancePage(_), mode, ua)              => accountBalanceRouteLogic(mode)
    case (IsUndocumentedAccountPage(_), mode, ua)                 => controllers.manual.account.routes.IsDormantAccountController.onPageLoad(mode)
    case (WhatWasTheAccountCurrencyPage(_), mode, ua)             => controllers.manual.account.routes.IsUndocumentedAccountController.onPageLoad(mode)
    case (IsDormantAccountPage(_), mode, ua)                      => controllers.manual.account.routes.WasAccountOpenController.onPageLoad(mode)
    case (WhatAccountTypePage(_), mode, ua)                       => controllers.manual.account.routes.HavePaymentsController.onPageLoad(mode)
    case (HavePaymentsPage(accId), mode, ua)                      => havePaymentRouteLogic(mode, ua, accId)
    case (PaymentTypePage(accountId), mode, ua)                   => paymentTypeRouteLogic(mode, ua, accountId)
    case (AccountPaymentsAmountPage(accountId), mode, ua)         => controllers.manual.account.routes.AccountPaymentsController.onPageLoad(mode)
    case (DoYouNeedToAddPaymentsPage(accountId), mode, ua)        => doYouNeedToAddPaymentsRouteLogic(mode, ua, accountId)
    case (RemovePaymentPage(accountId), mode, ua)                 => controllers.manual.account.routes.AccountPaymentsController.onPageLoad(mode)
    case (WhatIsAccountNumberPage(accountId, reportId), mode, ua) => controllers.manual.account.routes.AccountClosedController.onPageLoad(mode)
  }

  private def NumberTypeNavigation(accountId: AccountId, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers.get(NumberTypePage(accountId)) match {
      case Some(_) => controllers.manual.account.routes.WhatIsAccountNumberController.onPageLoad(mode)
      case None    => routes.JourneyRecoveryController.onPageLoad()
    }

  private def accountBalanceRouteLogic(mode: Mode)(implicit reportId: ReportId) =
    if (reportId.regime == FATCA) { controllers.manual.account.routes.HavePaymentsController.onPageLoad(mode) }
    else if (reportId.regime == CRS) { controllers.manual.account.routes.IsUndocumentedAccountController.onPageLoad(mode) }
    else routes.JourneyRecoveryController.onPageLoad()

  private def havePaymentRouteLogic(mode: Mode, ua: UserAnswers, accountId: AccountId)(implicit reportId: ReportId) = {
    val hasAnyPayments = ua.get(AccountPaymentListPage(accountId)).getOrElse(Seq.empty).nonEmpty
    ua.get(HavePaymentsPage(accountId)) match {
      case Some(false) => controllers.manual.account.routes.AccountCheckAnswersController.onPageLoad()
      case Some(havePayment) if havePayment && hasAnyPayments =>
        controllers.manual.account.routes.AccountPaymentsController.onPageLoad(mode)
      case _ =>
        controllers.manual.account.routes.CheckAccountTypeIsDepositoryController.onChangeRedirect(mode)
    }
  }

  private def paymentTypeRouteLogic(mode: Mode, ua: UserAnswers, accountId: AccountId)(implicit reportId: ReportId) =
    ua.get(AccountPaymentListPage(accountId)) match {
      case Some(payments) if payments.nonEmpty => controllers.manual.account.routes.AccountPaymentsAmountController.onPageLoad(mode)
      case _                                   => routes.JourneyRecoveryController.onPageLoad()
    }

  private def doYouNeedToAddPaymentsRouteLogic(mode: Mode, ua: UserAnswers, accountId: AccountId)(implicit reportId: ReportId) =
    ua.get(DoYouNeedToAddPaymentsPage(accountId)) match {
      case Some(true) =>
        controllers.manual.account.routes.CheckAccountTypeIsDepositoryController.onChangeRedirect(mode)
      case _ => controllers.manual.account.routes.AccountCheckAnswersController.onPageLoad()
    }

  private def jointAccountRouteLogic(accountId: AccountId, mode: Mode, userAnswers: UserAnswers)(implicit reportId: ReportId) =
    userAnswers
      .get(IsJointAccountPage(accountId))
      .map {
        isJointAccount =>
          if (isJointAccount) controllers.manual.account.routes.HowManyJointAccountHoldersController.onPageLoad(mode)
          else
            controllers.manual.account.routes.WhatAccountTypeController.onPageLoad(mode)
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

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

}
