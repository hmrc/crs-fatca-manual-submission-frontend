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
import models.requests.{AccountIdRequest, ReportIdRequest}
import models.viewModels.AccountId
import models.{ReportId, UserAnswers}
import pages.manual.account.{AccountsPage, CurrentAccountIdPage}
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountIdCreationActionImpl @Inject() (repository: DatabaseConnector)(implicit val executionContext: ExecutionContext) extends AccountIdCreationAction {

  override protected def transform[A](request: ReportIdRequest[A]): Future[AccountIdRequest[A]] = {
    given reportId: ReportId = request.reportId
    request.userAnswers.get(CurrentAccountIdPage()) match {
      case None =>
        val accountId: AccountId = createAndSetAccountId(request)
        Future.successful(
          AccountIdRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            accountId = accountId
          )
        )
      case Some(accId) =>
        Future.successful(
          AccountIdRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            accountId = accId
          )
        )
    }
  }

  private def createAndSetAccountId[A](request: ReportIdRequest[A])(implicit reportId: ReportId) = {
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val existingIds = request.userAnswers
      .get(AccountsPage())
      .map(
        acc => acc.accounts.keySet
      )
      .getOrElse(Set.empty)
    val accountId = AccountId.generate(existingIds)

    request.userAnswers
      .get(CurrentAccountIdPage())
      .fold {
        request.userAnswers
          .set(CurrentAccountIdPage(), accountId)
          .foreach(repository.set)
      }(
        _ => ()
      )
    accountId
  }
}

trait AccountIdCreationAction extends ActionTransformer[ReportIdRequest, AccountIdRequest]
