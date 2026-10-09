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
import models.requests.{AccountHolderIdRequest, AccountHolderNameRequest, TaxResidentCountryIdForAccountHolderRequest}
import pages.manual.accountHolders.{CurrentTaxResidentCountryIndexPage, TaxResidentCountriesListPage}
import play.api.mvc.ActionTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxResidentCountryIdCreationForAccountHolderActionImpl @Inject() (implicit val executionContext: ExecutionContext)
    extends TaxResidentCountryIdCreationForAccountHolderAction {

  override protected def transform[A](request: AccountHolderNameRequest[A]): Future[TaxResidentCountryIdForAccountHolderRequest[A]] = {
    given reportId: ReportId = request.reportId

    val accountHolderId = request.accountHolderId

    val ua = request.userAnswers

    ua.get(CurrentTaxResidentCountryIndexPage(request.accountHolderId, reportId)) match {
      case None =>
        val currentIndex = ua.get(TaxResidentCountriesListPage(accountHolderId, reportId)).getOrElse(Seq.empty).size
        Future.successful(
          TaxResidentCountryIdForAccountHolderRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            accountHolderId = request.accountHolderId,
            accountHolderName = request.accountHolderName,
            currentIndex = currentIndex
          )
        )
      case Some(id) =>
        Future.successful(
          TaxResidentCountryIdForAccountHolderRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            accountHolderId = request.accountHolderId,
            accountHolderName = request.accountHolderName,
            currentIndex = id
          )
        )
    }
  }
}

trait TaxResidentCountryIdCreationForAccountHolderAction extends ActionTransformer[AccountHolderNameRequest, TaxResidentCountryIdForAccountHolderRequest]
