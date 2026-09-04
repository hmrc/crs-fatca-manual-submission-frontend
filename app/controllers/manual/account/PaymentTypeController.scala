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
import controllers.actions.*
import forms.manual.account.PaymentTypeFormProvider
import models.NumberType.{Iban, Semp}
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.manual.account.WhatAccountType.{Depository, InsuranceOrAnnuityContract, InvestmentEntity}
import models.manual.account.{AccountPayment, PaymentType}
import models.viewModels.AccountId
import models.{Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.account.{AccountPaymentPage, CurrentAccountPaymentIndexPage, PaymentTypePage, WhatAccountTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.PaymentTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PaymentTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: PaymentTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PaymentTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequiredAndAccountPaymentIndexCreation() {
    implicit request =>
      implicit val reportId: ReportId         = request.reportId
      implicit val accountId: AccountId       = request.accountId
      val regimeType                          = reportId.regime
      val showDividendsAndInterestRadioFields = showDividendsAndInterestRadios(accountId, regimeType, request.userAnswers)

      val preparedForm = request.userAnswers.get(AccountPaymentPage(request.currentIndex)) match {
        case None        => form
        case Some(value) => form.fill(value.paymentType)
      }

      Ok(view(preparedForm, mode, regimeType, showDividendsAndInterestRadioFields))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequiredAndAccountPaymentIndexCreation().async {
    implicit request =>
      implicit val reportId: ReportId         = request.reportId
      implicit val accountId: AccountId       = request.accountId
      val regimeType                          = reportId.regime
      val showDividendsAndInterestRadioFields = showDividendsAndInterestRadios(accountId, regimeType, request.userAnswers)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, regimeType, showDividendsAndInterestRadioFields))),
          value =>
            val accountPayment = request.userAnswers
              .get(AccountPaymentPage(request.currentIndex))
              .map(_.copy(paymentType = value))
              .getOrElse(AccountPayment(value))
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(AccountPaymentPage(request.currentIndex), accountPayment))
              updatedAnswersWithCurrentAccountPaymentIndex <- Future
                .fromTry(updatedAnswers.setWithReportId(CurrentAccountPaymentIndexPage(accountId), request.currentIndex))
              _ <- sessionRepository.set(updatedAnswersWithCurrentAccountPaymentIndex)
            } yield Redirect(navigator.nextPage(PaymentTypePage(request.accountId), mode, updatedAnswers))
        )
  }

  private def showDividendsAndInterestRadios(accountId: AccountId, regimeType: RegimeType, ua: UserAnswers)(implicit reportId: ReportId) = {
    val accountType = ua.get(WhatAccountTypePage(accountId))

    if (regimeType == FATCA) false
    else {
      (!accountType.contains(InsuranceOrAnnuityContract) && !accountType.contains(InvestmentEntity))
      && regimeType == CRS
    }
  }
}
