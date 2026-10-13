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
    case (page, NormalMode, ua) if normalRoutes.isDefinedAt((page, ua)) => normalRoutes(reportId)(page, ua)
    case (page, CheckMode, ua) =>
      normalRoutes(reportId)(page, ua)
  }

  def normalRoutes(implicit reportId: ReportId): PartialFunction[(Page, UserAnswers), Call] = {
    case (HaveNumberPage(accountId), ua)                    => haveNumberNavigation(accountId, NormalMode, ua)
    case (NumberTypePage(accountId), ua)                    => NumberTypeNavigation(accountId, NormalMode, ua)
    case (IdentifierPage(_), ua)                            => controllers.manual.account.routes.AccountClosedController.onPageLoad(NormalMode)
    case (WasAccountOpenPage(_), ua)                        => controllers.manual.account.routes.IsJointAccountController.onPageLoad(NormalMode)
    case (IsJointAccountPage(accountId), ua)                => jointAccountRouteLogic(accountId, ua)
    case (HowManyJointAccountHoldersPage(_), ua)            => controllers.manual.account.routes.WhatAccountTypeController.onPageLoad(NormalMode)
    case (AccountClosedPage(accountId), ua)                 => accountClosedNavigation(accountId, NormalMode, ua)
    case (WhatWasTheAccountBalancePage(_), ua)              => accountBalanceRouteLogic(NormalMode)
    case (IsUndocumentedAccountPage(_), ua)                 => controllers.manual.account.routes.IsDormantAccountController.onPageLoad(NormalMode)
    case (WhatWasTheAccountCurrencyPage(_), ua)             => controllers.manual.account.routes.IsUndocumentedAccountController.onPageLoad(NormalMode)
    case (IsDormantAccountPage(_), ua)                      => controllers.manual.account.routes.WasAccountOpenController.onPageLoad(NormalMode)
    case (WhatAccountTypePage(_), ua)                       => controllers.manual.account.routes.HavePaymentsController.onPageLoad(NormalMode)
    case (HavePaymentsPage(accId), ua)                      => havePaymentRouteLogic(NormalMode, ua, accId)
    case (PaymentTypePage(accountId), ua)                   => paymentTypeRouteLogic(NormalMode, ua, accountId)
    case (AccountPaymentsAmountPage(accountId), ua)         => controllers.manual.account.routes.AccountPaymentsController.onPageLoad(NormalMode)
    case (DoYouNeedToAddPaymentsPage(accountId), ua)        => doYouNeedToAddPaymentsRouteLogic(NormalMode, ua, accountId)
    case (RemovePaymentPage(accountId), ua)                 => controllers.manual.account.routes.AccountPaymentsController.onPageLoad(NormalMode)
    case (WhatIsAccountNumberPage(accountId, reportId), ua) => controllers.manual.account.routes.AccountClosedController.onPageLoad(NormalMode)
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
