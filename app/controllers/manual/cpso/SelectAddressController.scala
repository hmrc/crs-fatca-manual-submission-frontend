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

package controllers.manual.cpso

import controllers.actions.*
import forms.manual.cpso.SelectAddressFormProvider

import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{AddressLookupPage, SelectAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import play.api.Logging
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.SelectAddressView

import scala.concurrent.{ExecutionContext, Future}

class SelectAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: SelectAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SelectAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val cpsoId                      = request.cpsoId
      val form                        = formProvider(reportId.regime)
      val regime                      = reportId.regime.value.toLowerCase()
      (for {
        addresses <- request.userAnswers.get(AddressLookupPage(cpsoId, reportId))
      } yield {
        val preparedForm = request.userAnswers.get(SelectAddressPage(cpsoId, reportId)) match {
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

        Ok(view(preparedForm, mode, options, request.cpsoName, regime))
      }).getOrElse {
        logger.error(s"Unable to find address look up value for cpso id ${request.cpsoId}")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val cpsoId                      = request.cpsoId
      val form                        = formProvider(reportId.regime)
      val regime                      = reportId.regime.value.toLowerCase()
      (for {
        addresses <- request.userAnswers.get(AddressLookupPage(cpsoId, reportId))
      } yield {
        val options: Seq[RadioItem] = addresses.map(
          address => RadioItem(content = Text(s"${address.formatRadios}"), value = Some(s"${address.format}"))
        )

        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, options, request.cpsoName, regime))),
            selectedValue =>
              addresses.find(_.format == selectedValue).flatMap(_.toAddress) match {
                case None =>
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
                case Some(address) =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(SelectAddressPage(cpsoId, reportId), address))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(SelectAddressPage(cpsoId, reportId), mode, updatedAnswers))
              }
          )
      }).getOrElse {
        logger.error(s"Unable to find address lookup value for cpsoId $cpsoId")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
      }

  }
}
