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
import forms.manual.accountHolders.UkAddressFormProvider
import models.UkAddress.from
import models.response.{Address, AddressLookup}
import models.viewModels.AccountHolderId
import models.{Countries, Mode, ReportId, UkAddress, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{
  AccountHolderIndividualNamePage,
  AddressLookupForAccountHolderPage,
  SelectAddressPage,
  UkAddressPage,
  UkPostCodeForAccountHolderPage
}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.UkAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkAddressController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: UkAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UkAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      request.userAnswers.get(AccountHolderIndividualNamePage(request.accountHolderId)) match {
        case Some(name) =>
          val preparedForm = resolveAddress(request.userAnswers, request.accountHolderId).fold(form)(form.fill)
          Ok(view(preparedForm, mode, name.fullName, Countries.ukTerritories))
        case None =>
          logger.error("Account Holder Name is missing")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  private def resolveAddress(userAnswers: UserAnswers, accountHolderId: AccountHolderId)(implicit reportId: ReportId): Option[UkAddress] =
    userAnswers
      .get(UkAddressPage(accountHolderId, reportId))
      .orElse(
        userAnswers.get(SelectAddressPage(accountHolderId, reportId)).map(_.ukAddress)
      )
      .orElse(
        userAnswers
          .get(AddressLookupForAccountHolderPage(accountHolderId, reportId))
          .collect {
            case Seq(singleAddress) =>
              singleAddress.toAddress.map(_.ukAddress)
          }
          .flatten
      )
      .orElse(
        userAnswers.get(UkPostCodeForAccountHolderPage(accountHolderId, reportId)).map(from)
      )

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      request.userAnswers.get(AccountHolderIndividualNamePage(request.accountHolderId)) match {
        case Some(name) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name.fullName, Countries.ukTerritories))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(UkAddressPage(request.accountHolderId, reportId), value))
                  _              <- repository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(UkAddressPage(request.accountHolderId, reportId), mode, updatedAnswers))
            )
        case None =>
          logger.error("Account Holder Name is missing")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }

  }
}
