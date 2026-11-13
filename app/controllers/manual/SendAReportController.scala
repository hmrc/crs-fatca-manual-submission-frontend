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

package controllers.manual

import controllers.actions.*
import models.viewModels.TaskStatus.*
import models.viewModels.{AccountId, SendAReportSections}
import pages.manual.account.AccountsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SendAReportService
import services.manual.account.AccountsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SendAReportView

import javax.inject.Inject

class SendAReportController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  accountsService: AccountsService,
  val controllerComponents: MessagesControllerComponents,
  service: SendAReportService,
  view: SendAReportView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>
      val reportingFIStatus = service.reportingFIStatus(request.userAnswers, request.reportId)
      val cpsoCompleteStatus = service.reportingCPSOCompleteStatus(request.userAnswers, request.reportId)
      val accountIds = request.userAnswers
        .get(AccountsPage(request.reportId))
        .map(_.accounts.keys.toSeq.map(AccountId.apply))
        .getOrElse(Seq.empty)

      val accountsStatus =
        if (accountIds.isEmpty) {
          NotStarted
        } else if (
          accountIds.forall {
            accountId =>
              accountsService.isComplete(
                request.userAnswers,
                request.reportId,
                accountId
              )
          }
        ) {
          Completed
        } else {
          Incomplete
        }
      val sections = SendAReportSections(
        reportDetails = Some(NotStarted),
        financialInstitutionDetails = Some(reportingFIStatus),
        sponsorDetails = Some(NotStarted),
        filerCategory = Some(NotStarted),
        accounts = Some(accountsStatus),
        accountHolders = Some(NoStatus),
        controllingPersons = Some(cpsoCompleteStatus),
        tbc1 = Some(Incomplete),
        tbc2 = Some(Incomplete),
        userAnswers = request.userAnswers,
        reportId = request.reportId
      )
      Ok(view(sections))
  }
}
