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
import models.requests.AccountHolderNameRequest
import models.{CheckMode, ReportId}
import pages.manual.accountHolders.{CurrentTaxResidentCountryIndexPage, TaxResidentCountriesListPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.Results.Redirect
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrentTaxResidentIdController @Inject() (
  repository: DatabaseConnector,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onChangeRedirect(id: Int): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequiredAndAccountHolderNameRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      redirectWithValidIndex(id, controllers.manual.accountHolders.routes.ResidentTaxController.onPageLoad(CheckMode))
  }

  def onRemoveRedirect(id: Int): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequiredAndAccountHolderNameRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      redirectWithValidIndex(id, controllers.manual.accountHolders.routes.RemoveTaxResidentCountryController.onPageLoad(CheckMode))
  }

  private def redirectWithValidIndex(id: Int, successCall: Call)(implicit request: AccountHolderNameRequest[AnyContent], reportId: ReportId): Future[Result] = {

    val existingSize = request.userAnswers.get(TaxResidentCountriesListPage(request.accountHolderId, reportId)).fold(0)(_.size)

    if (id >= 0 && id < existingSize) {
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(CurrentTaxResidentCountryIndexPage(request.accountHolderId, reportId), id))
        _              <- repository.set(updatedAnswers)
      } yield Redirect(successCall)
    } else {
      logger.error(s"Tax Resident Id check failed for id: $id, existing collection size: $existingSize")
      Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

}
