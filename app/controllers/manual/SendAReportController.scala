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

import connectors.DatabaseConnector
import controllers.actions.*
import models.viewModels.TaskStatus.*
import models.viewModels.{AccountId, Accounts, SendAReportSections}
import pages.manual.account.{AccountsPage, CurrentAccountIdPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SendAReportView

import javax.inject.Inject

class SendAReportController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  repository: DatabaseConnector,
  val controllerComponents: MessagesControllerComponents,
  view: SendAReportView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>
      implicit val reportId = request.reportId
      val existingIds       = request.userAnswers.get(AccountsPage()).map(_.accounts.keySet).getOrElse(Set.empty[String])
      request.userAnswers
        .get(CurrentAccountIdPage())
        .fold {
          request.userAnswers
            .set(CurrentAccountIdPage(), AccountId.generate(existingIds))
            .foreach(repository.set)
        }(
          _ => ()
        )
      val sections = SendAReportSections(
        reportDetails = Some(NotStarted),
        financialInstitutionDetails = Some(NotStarted),
        sponsorDetails = Some(NotStarted),
        filerCategory = Some(NotStarted),
        accounts = Some(NotStarted),
        accountHolders = Some(NoStatus),
        controllingPersons = Some(Completed),
        tbc1 = Some(Incomplete),
        tbc2 = Some(Incomplete)
      )
      Ok(view(sections))
  }
}
