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

import controllers.routes
import models.requests.{AccountIdRequest, ReportIdRequest}
import models.viewModels.AccountId
import pages.manual.account.CurrentAccountIdPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountIdRequiredActionImpl @Inject() (implicit
  val executionContext: ExecutionContext
) extends AccountIdRequiredAction {

  override protected def refine[A](request: ReportIdRequest[A]): Future[Either[Result, AccountIdRequest[A]]] =
    Future.successful {
      request.userAnswers.get(CurrentAccountIdPage()(request.reportId)) match {
        case Some(accountId) =>
          Right(toAccountIdRequest(request, accountId))

        case None =>
          Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  private def toAccountIdRequest[A](
    request: ReportIdRequest[A],
    accountId: AccountId
  ): AccountIdRequest[A] =
    AccountIdRequest(
      request = request.request,
      userId = request.userId,
      userAnswers = request.userAnswers,
      fatcaId = request.fatcaId,
      reportId = request.reportId,
      accountId = accountId
    )

}

trait AccountIdRequiredAction extends ActionRefiner[ReportIdRequest, AccountIdRequest]
