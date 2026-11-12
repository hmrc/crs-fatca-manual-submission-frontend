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
import forms.manual.accountHolders.IsAccountHoldersSectionCompleteFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.IsAccountHoldersSectionCompletePage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ViewFIService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.IsAccountHoldersSectionCompleteView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsAccountHoldersSectionCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: IsAccountHoldersSectionCompleteFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsAccountHoldersSectionCompleteView,
  viewFIService: ViewFIService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired().async {
    implicit request =>
      viewFIService
        .getFIDetail(request.fatcaId, request.reportId.fiId)
        .map {
          fiDetail =>
            val preparedForm = request.userAnswers
              .get(IsAccountHoldersSectionCompletePage(request.reportId))
              .fold(form)(form.fill)
            Ok(view(preparedForm, mode, fiDetail.FIName))
        }
        .recover {
          case e =>
            logger.error(s"Failed to retrieve FIDetail for FiID ${request.reportId.fiId}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      viewFIService
        .getFIDetail(request.fatcaId, reportId.fiId)
        .flatMap {
          fiDetail =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiDetail.FIName))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(IsAccountHoldersSectionCompletePage(reportId), value))
                    _              <- repository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(IsAccountHoldersSectionCompletePage(reportId), mode, updatedAnswers))
              )
        }
        .recover {
          case e =>
            logger.error(s"Failed to retrieve FIDetail for FiID ${reportId.fiId}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }
}
