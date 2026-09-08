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
import forms.manual.account.RemovePaymentFormProvider
import models.manual.account.AccountPayment
import models.viewModels.AccountId
import models.{Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.account.{AccountPaymentListPage, AccountPaymentPage, RemovePaymentPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.RemovePaymentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemovePaymentController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: RemovePaymentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemovePaymentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequiredAndAccountPaymentIndexRequired() {
    implicit request =>

      implicit val reportId: ReportId   = request.reportId
      implicit val accountId: AccountId = request.accountId

      request.userAnswers.get(AccountPaymentPage(request.currentIndex)) match {
        case None => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Some(accountPayment) =>
          Ok(view(form, mode, accountPayment))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequiredAndAccountPaymentIndexRequired().async {
    implicit request =>

      implicit val reportId: ReportId   = request.reportId
      implicit val accountId: AccountId = request.accountId

      request.userAnswers
        .get(AccountPaymentPage(request.currentIndex))
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
          accountPayment =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, accountPayment))),
                value =>
                  val ua                                      = request.userAnswers
                  val accountPaymentList: Seq[AccountPayment] = ua.get(AccountPaymentListPage(accountId)).getOrElse(Seq())
                  val updatedAccountPaymentList               = if (value) accountPaymentList.patch(request.currentIndex, Nil, 1) else accountPaymentList

                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(AccountPaymentListPage(accountId), updatedAccountPaymentList))
                    _              <- repository.set(updatedAnswers)
                  } yield redirectWithFlash(value, mode, updatedAnswers, accountPayment, accountId)
              )
        }
  }

  private def redirectWithFlash(value: Boolean, mode: Mode, useranswers: UserAnswers, accountPayment: AccountPayment, accountId: AccountId)(implicit
    reportId: ReportId,
    messages: Messages
  ): Result = {
    val message = s"${accountPayment.paymentsAmountDescription()} ${messages(s"account.paymentType.${accountPayment.paymentType.toString}").toLowerCase}"
    if value then Redirect(navigator.nextPage(RemovePaymentPage(accountId), mode, useranswers)).flashing("account-payment-removed" -> message)
    else Redirect(navigator.nextPage(RemovePaymentPage(accountId), mode, useranswers))
  }
}
