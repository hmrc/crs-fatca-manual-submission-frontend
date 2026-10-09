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
import forms.manual.accountHolders.SelfCertificationFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.SelfCertificationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.SelfCertificationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfCertificationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: SelfCertificationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SelfCertificationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountHolderIdRequiredAndCRSOnlyAndAccountHolderNameRequired() {
      implicit request =>
        implicit val reportId: ReportId = request.reportId
        val reportingPeriod             = reportId.reportingYear
        val accountHolderId             = request.accountHolderId

        val preparedForm = request.userAnswers
          .get(SelfCertificationPage(accountHolderId, reportId))
          .fold(form)(form.fill)

        Ok(view(preparedForm, mode, reportingPeriod, request.accountHolderName))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountHolderIdRequiredAndCRSOnlyAndAccountHolderNameRequired().async {
      implicit request =>
        implicit val reportId: ReportId = request.reportId
        val accountHolderId             = request.accountHolderId
        val reportingPeriod             = reportId.reportingYear

        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, reportingPeriod, request.accountHolderName))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(SelfCertificationPage(accountHolderId, reportId), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(SelfCertificationPage(accountHolderId, reportId), mode, updatedAnswers))
          )
    }
}
