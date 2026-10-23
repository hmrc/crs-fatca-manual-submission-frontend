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
import models.requests.{CPSONameRequest, TaxResidentCountryIdForCPSORequest}
import pages.manual.cpso.{CurrentTaxResidentCountryIndexPage, TaxResidentCountriesListPage}
import play.api.mvc.ActionTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxResidentCountryIdCreationForCPSOActionImpl @Inject() (implicit val executionContext: ExecutionContext)
    extends TaxResidentCountryIdCreationForCPSOAction {

  override protected def transform[A](request: CPSONameRequest[A]): Future[TaxResidentCountryIdForCPSORequest[A]] = {
    given reportId: ReportId = request.reportId

    val cpsoId = request.cpsoId

    val ua = request.userAnswers

    ua.get(CurrentTaxResidentCountryIndexPage(request.cpsoId, reportId)) match {
      case None =>
        val currentIndex = ua.get(TaxResidentCountriesListPage(cpsoId, reportId)).getOrElse(Seq.empty).size
        Future.successful(
          TaxResidentCountryIdForCPSORequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            cpsoId = request.cpsoId,
            cpsoName = request.cpsoName,
            currentIndex = currentIndex
          )
        )
      case Some(id) =>
        Future.successful(
          TaxResidentCountryIdForCPSORequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            cpsoId = request.cpsoId,
            cpsoName = request.cpsoName,
            currentIndex = id
          )
        )
    }
  }
}

trait TaxResidentCountryIdCreationForCPSOAction extends ActionTransformer[CPSONameRequest, TaxResidentCountryIdForCPSORequest]
