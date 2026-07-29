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
import forms.manual.account.HaveNumberFormProvider
import models.requests.AccountIdRequest
import models.{Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.account.{AccountIdPage, HaveNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.HaveNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HaveNumberController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  formProvider: HaveNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: HaveNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdCreation() {
    implicit request =>
      given reportId: ReportId = request.reportId
      val preparedForm = request.userAnswers.get(HaveNumberPage(request.accountId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdCreation().async {
    implicit request =>
      given reportId: ReportId = request.reportId
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers              <- Future.fromTry(request.userAnswers.setWithReportId(HaveNumberPage(request.accountId), value))
              updatedAnswersWithAccountId <- checkAndSetAccountId(request, updatedAnswers)
              _                           <- repository.set(updatedAnswersWithAccountId)
            } yield Redirect(navigator.nextPage(HaveNumberPage(request.accountId), mode, updatedAnswersWithAccountId))
        )
  }

  private def checkAndSetAccountId(request: AccountIdRequest[AnyContent], updatedAnswers: UserAnswers)(implicit reportId: ReportId) =
    Future.fromTry {
      updatedAnswers.get(AccountIdPage(request.accountId)) match {
        case Some(_) => scala.util.Success(updatedAnswers)
        case None    => updatedAnswers.setWithReportId(AccountIdPage(request.accountId), request.accountId)
      }
    }
}
