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
import forms.manual.accountHolders.IsThisTheAddressForAccountHoldersFormProvider

import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{AddressLookupForAccountHolderPage, IsThisTheAddressForAccountHoldersPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import models.viewModels.AccountHolderId
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.IsThisTheAddressForAccountHoldersView

import scala.concurrent.{ExecutionContext, Future}

class IsThisTheAddressForAccountHoldersController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         repository: DatabaseConnector,
                                         navigator: ManualSubmissionNavigator,
                                         actions: Actions,
                                         formProvider: IsThisTheAddressForAccountHoldersFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: IsThisTheAddressForAccountHoldersView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val accountHolderId: AccountHolderId = request.accountHolderId

      val preparedForm = request.userAnswers.get(IsThisTheAddressForAccountHoldersPage(accountHolderId, reportId))
        .fold(form)(form.fill)

     val address = request.userAnswers.get(AddressLookupForAccountHolderPage(request.accountHolderId, request.reportId))
       .flatMap(_.headOption.flatMap(_.toAddress))

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(IsThisTheAddressForAccountHoldersPage(request.accountHolderId, reportId), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(IsThisTheAddressForAccountHoldersPage(request.accountHolderId, reportId), mode, updatedAnswers))
      )
  }
}
