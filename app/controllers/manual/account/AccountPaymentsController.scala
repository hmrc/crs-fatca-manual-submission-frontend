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

import controllers.actions.*
import forms.manual.account.AccountPaymentsFormProvider

import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.account.{AccountPaymentListPage, DoYouNeedToAddPaymentsPage, HavePaymentsPage, PaymentsAddedPreviouslyPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import models.manual.account.AccountPayment
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.AccountPaymentsView

import scala.concurrent.{ExecutionContext, Future}

class AccountPaymentsController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: AccountPaymentsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AccountPaymentsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequired() {
    implicit request =>

      implicit val reportId: ReportId              = request.reportId
      val reportingPeriod                          = reportId.reportingYear.toString
      val regime                                   = reportId.regime.value.toLowerCase()
      val accountPaymentsList: Seq[AccountPayment] = request.userAnswers.get(AccountPaymentListPage(request.accountId)).getOrElse(Seq.empty)
      val paymentsAddedPreviously = request.userAnswers.get(PaymentsAddedPreviouslyPage(request.accountId)).exists(_ == true)
      val form                                     = formProvider(accountPaymentsList.size)
      val lastAccountNotComplete:Boolean = accountPaymentsList.lastOption.exists(_.accountPaymentsAmount.isEmpty)
      (paymentsAddedPreviously, accountPaymentsList.isEmpty) match {
        case (false, true)  => Redirect(controllers.manual.account.routes.PaymentTypeController.onPageLoad(mode))
        case (false, false) if lastAccountNotComplete => Redirect(controllers.manual.account.routes.CurrentAccountPaymentIndexController.onChangeRedirect(accountPaymentsList.size - 1))
        case _ =>
          val preparedForm = request.userAnswers.get(DoYouNeedToAddPaymentsPage(request.accountId)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, mode, accountPaymentsList, reportingPeriod, regime))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId              = request.reportId
      val reportingPeriod                          = reportId.reportingYear.toString
      val regime                                   = reportId.regime.value.toLowerCase()
      val accountPaymentsList: Seq[AccountPayment] = request.userAnswers.get(AccountPaymentListPage(request.accountId)).getOrElse(Seq.empty)
      val form                                     = formProvider(accountPaymentsList.size)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, accountPaymentsList, reportingPeriod, regime))),
          value =>
            val updateHavePayments = !value && accountPaymentsList.isEmpty
            for {
              ua <- Future.fromTry(request.userAnswers.setWithReportId(DoYouNeedToAddPaymentsPage(request.accountId), value))
              updatedAnswers <-
                if (updateHavePayments) Future.fromTry(ua.setWithReportId(HavePaymentsPage(request.accountId), value)) else Future.successful(ua)
              _ <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(DoYouNeedToAddPaymentsPage(request.accountId), mode, updatedAnswers))
        )
  }
}
