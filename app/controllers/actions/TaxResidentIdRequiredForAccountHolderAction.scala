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
import models.requests.{AccountHolderNameRequest, TaxResidentCountryIdForAccountHolderRequest}
import pages.manual.accountHolders.CurrentTaxResidentCountryIndexPage
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxResidentIdRequiredForAccountHolderImpl @Inject() (implicit
  val executionContext: ExecutionContext
) extends TaxResidentIdRequiredForAccountHolderAction
    with Logging {

  override protected def refine[A](request: AccountHolderNameRequest[A]): Future[Either[Result, TaxResidentCountryIdForAccountHolderRequest[A]]] =
    Future.successful {
      request.userAnswers.get(CurrentTaxResidentCountryIndexPage(request.accountHolderId, request.reportId)) match {
        case Some(currentIndex) => Right(toTaxResidentCountryIdForAccountHolderRequest(request, currentIndex))
        case None               => Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  private def toTaxResidentCountryIdForAccountHolderRequest[A](request: AccountHolderNameRequest[A], index: Int) =
    TaxResidentCountryIdForAccountHolderRequest(
      request = request.request,
      userId = request.userId,
      userAnswers = request.userAnswers,
      fatcaId = request.fatcaId,
      reportId = request.reportId,
      accountHolderId = request.accountHolderId,
      accountHolderName = request.accountHolderName,
      currentIndex = index
    )
}

trait TaxResidentIdRequiredForAccountHolderAction extends ActionRefiner[AccountHolderNameRequest, TaxResidentCountryIdForAccountHolderRequest]
