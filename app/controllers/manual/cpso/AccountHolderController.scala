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

import connectors.DatabaseConnector
import controllers.actions.*
import forms.AccountHolderFormProvider
import models.ServiceErrors.NoFiDetailFound
import models.viewModels.{AccountHolders, Accounts}
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.AccountHolderPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ViewFIService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.AccountHolderView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountHolderController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: AccountHolderFormProvider,
  viewFIService: ViewFIService,
  val controllerComponents: MessagesControllerComponents,
  view: AccountHolderView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdCreation().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      viewFIService
        .getFIDetail(request.fatcaId, reportId.fiId)
        .map {
          fiDetail =>
            val accountHolders = AccountHolders
              .existingAccountHolders(request.userAnswers)
              .filter(
                (_, accountHolder) => accountHolder.canHaveCPSO(reportId.regime)
              )
            val existingIds = accountHolders.map(_._1).toSet
            val preparedForm = request.userAnswers
              .get(AccountHolderPage(request.cpsoId, reportId))
              .map(_ intersect existingIds)
              .fold(formProvider(existingIds.toSeq))(formProvider(existingIds.toSeq).fill)
            Ok(view(preparedForm, mode, fiDetail.FIName, AccountHolders.checkboxOptions(accountHolders), reportId.regime.value))
        }
        .recover {
          case NoFiDetailFound =>
            logger.error(s"Failed to retrieve FIDetail for FiID ${reportId.fiId}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdCreation().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      viewFIService
        .getFIDetail(request.fatcaId, reportId.fiId)
        .flatMap {
          fiDetail =>
            val accountHolders = AccountHolders
              .existingAccountHolders(request.userAnswers)
              .filter(
                (_, accountHolder) => accountHolder.canHaveCPSO(reportId.regime)
              )
            val existingIds = accountHolders.map(_._1)
            val form        = formProvider(existingIds)
            val options     = AccountHolders.checkboxOptions(accountHolders)
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiDetail.FIName, options, reportId.regime.value))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(AccountHolderPage(request.cpsoId, reportId), value))
                    _              <- repository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(AccountHolderPage(request.cpsoId, reportId), mode, updatedAnswers))
              )
        }
        .recover {
          case NoFiDetailFound =>
            logger.error(s"Failed to retrieve FIDetail for FiID ${reportId.fiId}")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
  }
}
