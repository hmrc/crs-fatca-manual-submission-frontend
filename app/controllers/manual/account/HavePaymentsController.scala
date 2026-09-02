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
import forms.manual.account.HavePaymentsFormProvider

import javax.inject.Inject
import models.{Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.account.{AccountPaymentListPage, AccountPaymentPage, CurrentAccountPaymentIndexPage, HavePaymentsPage, NumberTypePage, WhatAccountTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import models.NumberType.{Iban, Semp}
import models.SubmissionsConstants.{CRS, RegimeType}
import models.manual.account.{AccountPayment, PaymentType}
import models.viewModels.AccountId
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.HavePaymentsView

import scala.concurrent.{ExecutionContext, Future}

class HavePaymentsController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: HavePaymentsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: HavePaymentsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequired() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val reportingPeriod             = reportId.reportingYear.toString
      val form                        = formProvider(reportId.regime, reportingPeriod)

      val preparedForm = request.userAnswers.get(HavePaymentsPage(request.accountId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, reportId.regime, reportingPeriod))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequired().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val reportingPeriod             = reportId.reportingYear.toString
      val form                        = formProvider(reportId.regime, reportingPeriod)
      val regime                      = reportId.regime

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, regime, reportingPeriod))),
          value =>
            for {
              ua <- Future.fromTry(request.userAnswers.setWithReportId(HavePaymentsPage(request.accountId), value))
              _  <- repository.set(ua)
            } yield Redirect(navigator.nextPage(HavePaymentsPage(request.accountId), mode, ua))
        )
  }
}
