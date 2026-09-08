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
import models.{Countries, Mode, ReportId}
import navigation.ManualSubmissionNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import pages.manual.accountHolders.AccountHolderAddressNonUkPage
import views.html.manual.accountHolders.AccountHolderAddressNonUkView
import forms.manual.accountHolders.AccountHolderAddressNonUkFormProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountHolderAddressNonUkController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: AccountHolderAddressNonUkFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AccountHolderAddressNonUkView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form                     = formProvider()
  val PLACEHOLDERACCOUNTHOLDER = "PLACEHOLDERACCOUNTHOLDER"

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      val preparedForm = request.userAnswers.get(AccountHolderAddressNonUkPage()) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, PLACEHOLDERACCOUNTHOLDER, Countries.nonUkTerritories))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, PLACEHOLDERACCOUNTHOLDER, Countries.nonUkTerritories))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(AccountHolderAddressNonUkPage(), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AccountHolderAddressNonUkPage(), mode, updatedAnswers))
        )
  }
}
