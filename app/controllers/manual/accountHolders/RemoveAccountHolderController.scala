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

import connectors.DatabaseConnector
import controllers.actions.*
import controllers.routes
import forms.manual.accountHolders.RemoveAccountHolderFormProvider
import models.viewModels.AccountHolderId
import models.viewModels.manual.cpso.{CPSOId, ControllingOrSubstantialOwners}
import models.{Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{AccountHolderPage, RemoveAccountHolderPage}
import pages.manual.cpso.{AccountHolderIdsPage, CPSOPage, CurrentCPSOIdPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.RemoveAccountHolderView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

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
                val answersAfterRemoval =
                  if value then removeAccountHolderAndLinkedCpsos(request.userAnswers, request.accountHolderId)
                  else Success(request.userAnswers)

                for {
                  updatedAnswers <- Future.fromTry(answersAfterRemoval)
                  _              <- repository.set(updatedAnswers)
                } yield redirectWithFlash(value, mode, updatedAnswers, request.accountHolderName)
            )
      }
  }

  private def removeAccountHolderAndLinkedCpsos(
    userAnswers: UserAnswers,
    accountHolderId: AccountHolderId
  )(implicit reportId: ReportId): Try[UserAnswers] =
    userAnswers
      .remove(AccountHolderPage(accountHolderId, reportId))
      .flatMap(unlinkAccountHolderFromCpsos(_, accountHolderId))

  private def unlinkAccountHolderFromCpsos(
    userAnswers: UserAnswers,
    accountHolderId: AccountHolderId
  )(implicit reportId: ReportId): Try[UserAnswers] =
    cpsoIdsLinkedTo(userAnswers, accountHolderId).foldLeft(Try(userAnswers)) {
      (answers, cpsoId) =>
        answers.flatMap {
          current =>
            val remaining = current.get(AccountHolderIdsPage(cpsoId, reportId)).getOrElse(Set.empty) - accountHolderId

            if remaining.isEmpty then
              for {
                withoutCpso <- current.remove(CPSOPage(cpsoId))
                withoutCurrent <-
                  if withoutCpso.get(CurrentCPSOIdPage()).contains(cpsoId) then withoutCpso.remove(CurrentCPSOIdPage())
                  else Success(withoutCpso)
              } yield withoutCurrent
            else current.setWithReportId(AccountHolderIdsPage(cpsoId, reportId), remaining)
        }
    }

  private def cpsoIdsLinkedTo(userAnswers: UserAnswers, accountHolderId: AccountHolderId)(implicit
    reportId: ReportId
  ): Seq[CPSOId] =
    ControllingOrSubstantialOwners
      .existingCpsos(userAnswers)
      .map(_._1)
      .filter(
        cpsoId => userAnswers.get(AccountHolderIdsPage(cpsoId, reportId)).exists(_.contains(accountHolderId))
      )

  private def redirectWithFlash(value: Boolean, mode: Mode, useranswers: UserAnswers, accountHolder: String)(implicit reportId: ReportId): Result =
    if value then Redirect(navigator.nextPage(RemoveAccountHolderPage(), mode, useranswers)).flashing("account-holder-removed" -> accountHolder)
    else Redirect(navigator.nextPage(RemoveAccountHolderPage(), mode, useranswers))
}
