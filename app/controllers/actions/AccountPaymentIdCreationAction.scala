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

package controllers.actions

import models.ReportId
import models.requests.{AccountIdRequest, AccountPaymentIndexRequest}
import pages.manual.account.{AccountPaymentListPage, CurrentAccountPaymentIndexPage}
import play.api.mvc.ActionTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountPaymentIdCreationActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends AccountPaymentIdCreationAction {

  override protected def transform[A](request: AccountIdRequest[A]): Future[AccountPaymentIndexRequest[A]] = {
    given reportId: ReportId = request.reportId
    val ua                   = request.userAnswers

    ua.get(CurrentAccountPaymentIndexPage(request.accountId)) match {
      case None =>
        val currentIndex = ua.get(AccountPaymentListPage(request.accountId)).getOrElse(Seq.empty).size
        Future.successful(
          AccountPaymentIndexRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            accountId = request.accountId,
            currentIndex = currentIndex
          )
        )
      case Some(currentId) =>
        Future.successful(
          AccountPaymentIndexRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            accountId = request.accountId,
            currentIndex = currentId
          )
        )
    }

  }
}

trait AccountPaymentIdCreationAction extends ActionTransformer[AccountIdRequest, AccountPaymentIndexRequest]
