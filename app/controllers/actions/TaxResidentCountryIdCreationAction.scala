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

import models.ReportId
import models.requests.{SponsorNameRequest, SponsorTaxResidentIdRequest}
import pages.manual.sponsor.{CurrentTaxResidentCountryIndexPage, TaxResidentCountriesListPage}
import play.api.mvc.ActionTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxResidentCountryIdCreationActionImpl @Inject() (implicit val executionContext: ExecutionContext) extends TaxResidentCountryIdCreationAction {

  override protected def transform[A](request: SponsorNameRequest[A]): Future[SponsorTaxResidentIdRequest[A]] = {
    given reportId: ReportId = request.reportId

    val ua = request.userAnswers
    ua.get(CurrentTaxResidentCountryIndexPage()) match {
      case None =>
        val currentIndex = ua.get(TaxResidentCountriesListPage()).getOrElse(Seq.empty).size
        Future.successful(
          SponsorTaxResidentIdRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = ua,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            sponsorName = request.sponsorName,
            currentId = currentIndex
          )
        )
      case Some(id) =>
        Future.successful(
          SponsorTaxResidentIdRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = ua,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            sponsorName = request.sponsorName,
            currentId = id
          )
        )
    }
  }

}

trait TaxResidentCountryIdCreationAction extends ActionTransformer[SponsorNameRequest, SponsorTaxResidentIdRequest]
