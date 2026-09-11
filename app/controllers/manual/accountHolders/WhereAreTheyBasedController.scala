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
import forms.manual.accountHolders.WhereAreTheyBasedFormProvider

import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{IndividualNamePage, WhereAreTheyBasedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.WhereAreTheyBasedView

import scala.concurrent.{ExecutionContext, Future}

class WhereAreTheyBasedController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: WhereAreTheyBasedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhereAreTheyBasedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      request.userAnswers.get(IndividualNamePage(request.accountHolderId)) match {
        case Some(name) =>
          val preparedForm = request.userAnswers.get(WhereAreTheyBasedPage(request.accountHolderId)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, mode, name.fullName))
        case None =>
          logger.error("Individual Name value is missing")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      // TODO: ORG NAME SHOULD BE ADDED ONCE IMPLEMENTED
      request.userAnswers.get(IndividualNamePage(request.accountHolderId)) match {
        case Some(name) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name.fullName))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(WhereAreTheyBasedPage(request.accountHolderId), value))
                  _              <- repository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(WhereAreTheyBasedPage(request.accountHolderId), mode, updatedAnswers))
            )
        case None =>
          logger.error("Individual Name value is missing")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }

  }
}
