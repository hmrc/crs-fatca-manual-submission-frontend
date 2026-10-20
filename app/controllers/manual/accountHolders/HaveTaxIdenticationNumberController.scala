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
import forms.manual.accountHolders.HaveTaxIdenticationNumberFormProvider

import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{HaveTaxIdenticationNumberPage, ResidentTaxPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.HaveTaxIdenticationNumberView

import scala.concurrent.{ExecutionContext, Future}

class HaveTaxIdenticationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: HaveTaxIdenticationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: HaveTaxIdenticationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderNameRequiredAndTaxResidentIdRequiredAndCRSOnlyCreation() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      val preparedForm = request.userAnswers.get(HaveTaxIdenticationNumberPage(request.currentIndex, request.accountHolderId, reportId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(ResidentTaxPage(request.currentIndex, request.accountHolderId, reportId)) match {
        case Some(country) =>
          Ok(view(preparedForm, mode, request.accountHolderName, country))
        case None =>
          logger.error("missing country name")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderNameRequiredAndTaxResidentIdRequiredAndCRSOnlyCreation().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      request.userAnswers.get(ResidentTaxPage(request.currentIndex, request.accountHolderId, reportId)) match {
        case Some(country) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.accountHolderName, country))),
              value =>
                for {
                  updatedAnswers <- Future
                    .fromTry(request.userAnswers.setWithReportId(HaveTaxIdenticationNumberPage(request.currentIndex, request.accountHolderId, reportId), value))
                  _ <- repository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(HaveTaxIdenticationNumberPage(request.currentIndex, request.accountHolderId, reportId), mode, updatedAnswers)
                )
            )
        case None =>
          logger.error("missing country name")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }

  }
}
