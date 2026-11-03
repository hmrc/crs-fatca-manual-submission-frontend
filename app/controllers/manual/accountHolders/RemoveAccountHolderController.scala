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

package controllers.manual.accountHolders

import controllers.actions.*
import forms.manual.accountHolders.RemoveAccountHolderFormProvider

import javax.inject.Inject
import models.{Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{AccountHolderPage, RemoveAccountHolderPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import connectors.DatabaseConnector
import controllers.routes
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.RemoveAccountHolderView

import scala.concurrent.{ExecutionContext, Future}

class RemoveAccountHolderController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: RemoveAccountHolderFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAccountHolderView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequiredAndAccountHolderNameRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      request.userAnswers.get(AccountHolderPage(request.accountHolderId, reportId)) match {
        case None =>
          logger.error("Missing an account holder")
          Redirect(routes.JourneyRecoveryController.onPageLoad())
        case Some(value) =>
          Ok(view(form, mode, reportId.regime.value, request.accountHolderName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequiredAndAccountHolderNameRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      request.userAnswers.get(AccountHolderPage(request.accountHolderId, reportId)) match {
        case None =>
          logger.error("Missing an account holder")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(value) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, reportId.regime.value, request.accountHolderName))),
              value =>
                for {
                  updatedAnswers   <- if(value) Future.fromTry(request.userAnswers.remove(AccountHolderPage(request.accountHolderId, reportId))) else Future.successful(request.userAnswers)
                  _              <- repository.set(updatedAnswers)
                } yield redirectWithFlash(value, mode, updatedAnswers, request.accountHolderName)
            )
      }
  }

  private def redirectWithFlash(value: Boolean, mode: Mode, useranswers: UserAnswers, accountHolder: String)(implicit reportId: ReportId): Result =
    if value then Redirect(navigator.nextPage(RemoveAccountHolderPage(), mode, useranswers)).flashing("account-holder-removed" -> accountHolder)
    else Redirect(navigator.nextPage(RemoveAccountHolderPage(), mode, useranswers))
}
