/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.actions

import connectors.DatabaseConnector
import models.requests.{AccountHolderIdRequest, ReportIdRequest}
import models.viewModels.{AccountHolderId, AccountHolders}
import models.{ReportId, UserAnswers}
import pages.manual.accountHolders.{AccountHoldersPage, CurrentAccountHolderIdPage}
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountHolderIdCreationActionImpl @Inject() (repository: DatabaseConnector)(implicit val executionContext: ExecutionContext)
    extends AccountHolderIdCreationAction {

  override protected def transform[A](request: ReportIdRequest[A]): Future[AccountHolderIdRequest[A]] = {
    given reportId: ReportId = request.reportId
    request.userAnswers.get(CurrentAccountHolderIdPage()) match {
      case None =>
        val accountHolderId: AccountHolderId = createAndSetAccountHolderId(request)
        Future.successful(
          AccountHolderIdRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            accountHolderId = accountHolderId
          )
        )
      case Some(accountHolderId) =>
        Future.successful(
          AccountHolderIdRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            accountHolderId = accountHolderId
          )
        )
    }
  }

  private def createAndSetAccountHolderId[A](request: ReportIdRequest[A])(implicit reportId: ReportId) = {
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val existingIds = request.userAnswers
      .get(AccountHoldersPage())
      .map(
        acc => acc.accountHolders.keySet
      )
      .getOrElse(Set.empty)
    val accountHolderId = AccountHolderId.generate(existingIds)

    request.userAnswers
      .get(CurrentAccountHolderIdPage())
      .fold {
        request.userAnswers
          .set(CurrentAccountHolderIdPage(), accountHolderId)
          .foreach(repository.set)
      }(
        _ => ()
      )
    accountHolderId
  }
}

trait AccountHolderIdCreationAction extends ActionTransformer[ReportIdRequest, AccountHolderIdRequest]
