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
import forms.manual.accountHolders.SelectAddressFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{AddressLookupForAccountHolderPage, SelectAddressPage}
import pages.manual.accountHolders.AccountHolderIndividualNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.SelectAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectAddressController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: SelectAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SelectAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired() {
    implicit request =>

      val reportId: ReportId = request.reportId

      (for {
        addresses <- request.userAnswers.get(AddressLookupForAccountHolderPage(request.accountHolderId, reportId))
        name      <- request.userAnswers.get(AccountHolderIndividualNamePage(request.accountHolderId)(reportId)) // TODO : Need to implement Organisation Name
      } yield {
        val preparedForm = request.userAnswers.get(SelectAddressPage(request.accountHolderId, reportId)) match {
          case None => form
          case Some(savedAddress) =>
            addresses.find(_.toAddress.contains(savedAddress)) match {
              case Some(matched) => form.fill(matched.format)
              case None          => form
            }
        }

        val options: Seq[RadioItem] = addresses.map(
          address => RadioItem(content = Text(s"${address.formatRadios}"), value = Some(s"${address.format}"))
        )

        Ok(view(preparedForm, mode, name.fullName, options))
      }).getOrElse {
        logger.error(s"Unable to find address or name for account holder id ${request.accountHolderId}")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      (for {
        addresses <- request.userAnswers.get(AddressLookupForAccountHolderPage(request.accountHolderId, reportId))
        name      <- request.userAnswers.get(AccountHolderIndividualNamePage(request.accountHolderId)) // TODO : Need to implement Organisation Name
      } yield {
        val options: Seq[RadioItem] = addresses.map(
          address => RadioItem(content = Text(s"${address.formatRadios}"), value = Some(s"${address.format}"))
        )

        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name.fullName, options))),
            selectedValue =>
              addresses.find(_.format == selectedValue).flatMap(_.toAddress) match {
                case None =>
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
                case Some(address) =>
                  for {
                    updatedAnswers <-
                      Future.fromTry(request.userAnswers.setWithReportId(SelectAddressPage(request.accountHolderId, reportId), address))
                    _ <- repository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(SelectAddressPage(request.accountHolderId, reportId), mode, updatedAnswers))
              }
          )
      }).getOrElse {
        logger.error(s"Unable to find address or name for account holder id ${request.accountHolderId}")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
      }
  }
}
