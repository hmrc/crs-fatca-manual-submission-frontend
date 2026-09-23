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
import forms.manual.accountHolders.SelfCertificationFormProvider
import models.manual.accountHolders.IndividualName
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{AccountHolderIndividualNamePage, SelfCertificationPage}
import play.api.Logging
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
  accountHolderCRSOnlyFilterAction: AccountHolderCRSOnlyFilterAction,
  view: SelfCertificationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (actions.withReportIdRequiredAndAccountHolderIdRequired() andThen accountHolderCRSOnlyFilterAction) {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val reportingPeriod             = reportId.reportingYear
      val accountHolderId             = request.accountHolderId

      val preparedForm = request.userAnswers
        .get(SelfCertificationPage(accountHolderId, reportId))
        .fold(form)(form.fill)

      // todo will need to cater for organization name
      val individualName: Option[IndividualName] = request.userAnswers.get(AccountHolderIndividualNamePage(request.accountHolderId))

      individualName match {
        case Some(indName) => Ok(view(preparedForm, mode, reportingPeriod, indName.fullName))
        case _ =>
          logger.warn("Missing individual name")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (actions.withReportIdRequiredAndAccountHolderIdRequired() andThen accountHolderCRSOnlyFilterAction).async {
    implicit request =>
      implicit val reportId: ReportId            = request.reportId
      val accountHolderId                        = request.accountHolderId
      val reportingPeriod                        = reportId.reportingYear
      val individualName: Option[IndividualName] = request.userAnswers.get(AccountHolderIndividualNamePage(request.accountHolderId))

      individualName match {
        case None =>
          logger.warn("Missing individual name")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        case Some(indName) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, reportingPeriod, indName.fullName))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(SelfCertificationPage(accountHolderId, reportId), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(SelfCertificationPage(accountHolderId, reportId), mode, updatedAnswers))
            )
      }

  }
}
