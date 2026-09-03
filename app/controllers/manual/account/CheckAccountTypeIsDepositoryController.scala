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

package controllers.manual.account

import connectors.DatabaseConnector
import controllers.actions.Actions
import models.manual.account.{AccountPayment, PaymentType}
import models.{Mode, ReportId}
import models.manual.account.WhatAccountType.Depository
import models.viewModels.AccountId
import navigation.ManualSubmissionNavigator
import pages.manual.account.{AccountPaymentPage, CurrentAccountPaymentIndexPage, PaymentTypePage, WhatAccountTypePage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckAccountTypeIsDepositoryController @Inject() (
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onChangeRedirect(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequiredAndAccountPaymentIndexCreation().async {
    implicit request =>

      implicit val reportId: ReportId      = request.reportId
      implicit val accountId: AccountId    = request.accountId
      val ua                               = request.userAnswers
      val accountType                      = ua.get(WhatAccountTypePage(accountId))
      val shouldUpdatePaymentToCRSInterest = accountType.contains(Depository)

      if (shouldUpdatePaymentToCRSInterest) {
        val currentIndex = request.currentIndex
        val accountPayment = ua
          .get(AccountPaymentPage(currentIndex))
          .map(_.copy(paymentType = PaymentType.CRSInterest))
          .getOrElse(AccountPayment(PaymentType.CRSInterest))
        for {
          userAnswer                <- Future.fromTry(ua.setWithReportId(AccountPaymentPage(currentIndex), accountPayment))
          updatedUAWithCurrentIndex <- Future.fromTry(userAnswer.setWithReportId(CurrentAccountPaymentIndexPage(accountId), currentIndex))
          _                         <- sessionRepository.set(updatedUAWithCurrentIndex)
        } yield Redirect(navigator.nextPage(PaymentTypePage(request.accountId), mode, updatedUAWithCurrentIndex))
      } else {
        Future.successful(Redirect(controllers.manual.account.routes.PaymentTypeController.onPageLoad(mode)))
      }
  }
}
