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
import controllers.actions.Actions
import forms.manual.accountHolders.IsThisTheAddressForAccountHoldersFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.IsThisTheAddressForAccountHoldersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisTheAddressForAccountHoldersController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: IsThisTheAddressForAccountHoldersFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsThisTheAddressForAccountHoldersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountHolderIdRequiredAndAccountHolderNameRequired() {
      implicit request =>

        implicit val reportId: ReportId = request.reportId

        val preparedForm = request.userAnswers
          .get(IsThisTheAddressForAccountHoldersPage(request.accountHolderId, reportId))
          .fold(form)(form.fill)

        val address = request.userAnswers
          .get(AddressLookupForAccountHolderPage(request.accountHolderId, reportId))
          .flatMap(_.headOption.flatMap(_.toAddress))

        address
          .map(
            addr => Ok(view(preparedForm, mode, addr, request.accountHolderName))
          )
          .getOrElse {
            logger.warn("Missing address for account holder")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountHolderIdRequiredAndAccountHolderNameRequired().async {
      implicit request =>

        implicit val reportId: ReportId = request.reportId

        val address = request.userAnswers
          .get(AddressLookupForAccountHolderPage(request.accountHolderId, reportId))
          .flatMap(_.headOption.flatMap(_.toAddress))

        address
          .map {
            addr =>
              form
                .bindFromRequest()
                .fold(
                  formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, addr, request.accountHolderName))),
                  value =>
                    for {
                      ua <- Future.fromTry(
                        request.userAnswers.setWithReportId(IsThisTheAddressForAccountHoldersPage(request.accountHolderId, reportId), value)
                      )
                      answersWithMaybeAddress <-
                        if (value) Future.fromTry(ua.setWithReportId(WhatIsAddressForAccountHolderPage(request.accountHolderId, reportId), addr))
                        else Future.successful(ua)
                      _ <- repository.set(answersWithMaybeAddress)
                    } yield Redirect(
                      navigator.nextPage(IsThisTheAddressForAccountHoldersPage(request.accountHolderId, reportId), mode, answersWithMaybeAddress)
                    )
                )
          }
          .getOrElse {
            logger.warn("Missing address for account holder")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
    }
}
