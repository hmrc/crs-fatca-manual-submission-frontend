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
import forms.manual.account.RemoveAccountFormProvider
import models.SubmissionsConstants.CRS
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.manual.account.AccountsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.RemoveAccountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAccountController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  repository: DatabaseConnector,
  formProvider: RemoveAccountFormProvider,
  accountsService: AccountsService,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAccountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountIdRequired() {
      implicit request =>

        val selectedAccount = accountsService
          .orderedAccounts(request.userAnswers, request.reportId)
          .find(_._1 == request.accountId)

        selectedAccount match {
          case Some((_, accountNumber)) =>
            Ok(
              view(
                form,
                accountNumber,
                request.reportId.regime == CRS
              )
            )

          case None =>
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
        }
    }

  def onSubmit(): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountIdRequired().async {
      implicit request =>

        val selectedAccount = accountsService
          .orderedAccounts(request.userAnswers, request.reportId)
          .find(_._1 == request.accountId)

        selectedAccount match {
          case Some((_, accountNumber)) =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(
                        formWithErrors,
                        accountNumber,
                        request.reportId.regime == CRS
                      )
                    )
                  ),
                removeAccount =>
                  if (removeAccount) {
                    for {
                      updatedAnswers <- Future.fromTry(
                        accountsService.removeAccount(
                          request.userAnswers,
                          request.reportId,
                          request.accountId
                        )
                      )
                      _ <- repository.set(updatedAnswers)
                    } yield Redirect(
                      controllers.manual.account.routes.AccountsController
                        .onPageLoad()
                    ).flashing("account-removed" -> accountNumber)
                  } else {
                    Future.successful(
                      Redirect(
                        controllers.manual.account.routes.AccountsController
                          .onPageLoad()
                      )
                    )
                  }
              )

          case None =>
            Future.successful(
              Redirect(
                controllers.routes.JourneyRecoveryController.onPageLoad()
              )
            )
        }
    }
}
