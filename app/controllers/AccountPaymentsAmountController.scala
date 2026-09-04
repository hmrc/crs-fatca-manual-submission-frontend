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

package controllers

import connectors.DatabaseConnector
import controllers.actions.*
import forms.AccountPaymentsAmountFormProvider
import models.manual.account.AccountPayment
import models.viewModels.AccountId
import models.{AccountPaymentsAmount, Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.AccountPaymentsAmountPage
import pages.manual.account.AccountPaymentPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AccountPaymentsAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountPaymentsAmountController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: AccountPaymentsAmountFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AccountPaymentsAmountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequiredAndAccountPaymentIndexRequired() {
    implicit request =>
      val regime = request.reportId.regime
      val form   = formProvider(regime)

      implicit val reportId: ReportId   = request.reportId
      implicit val accountId: AccountId = request.accountId

      val preparedForm: Form[AccountPaymentsAmount] =
        request.userAnswers
          .get(AccountPaymentPage(request.currentIndex))
          .flatMap(_.accountPaymentsAmount)
          .fold(form)(form.fill)

      Ok(view(preparedForm, mode, regime))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequiredAndAccountPaymentIndexRequired().async {
    implicit request =>
      val regime = request.reportId.regime
      val form   = formProvider(regime)

      implicit val reportId: ReportId   = request.reportId
      implicit val accountId: AccountId = request.accountId

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, regime))),
          value => {
            val maybeUpdated = request.userAnswers
              .get(AccountPaymentPage(request.currentIndex))
              .map(_.copy(accountPaymentsAmount = Some(value)))

            maybeUpdated match {
              case None =>
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

              case Some(accountPayment) =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(AccountPaymentPage(request.currentIndex), accountPayment))
                  _              <- repository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AccountPaymentsAmountPage(request.accountId), mode, updatedAnswers))
            }
          }
        )
  }
}
