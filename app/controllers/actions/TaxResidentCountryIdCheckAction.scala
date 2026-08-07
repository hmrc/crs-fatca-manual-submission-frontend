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

import models.requests.SponsorNameRequest
import pages.manual.sponsor.TaxResidentCountriesListPage
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxResidentCountryIdCheckActionImpl @Inject() (implicit
  val executionContext: ExecutionContext
) extends TaxResidentCountryIdCheckAction
    with Logging {

  override protected def filter[A](request: SponsorNameRequest[A]): Future[Option[Result]] = {
    val existing = request.userAnswers.get(TaxResidentCountriesListPage()(request.reportId)).getOrElse(Seq.empty)

    val mayBeId = request.getQueryString("id")
    val result: Option[Result] = mayBeId.flatMap(_.toIntOption) match {
      case Some(id) if id >= 0 && id <= existing.size => None
      case _ =>
        logger.error(s"Tax Resident Id check failed for id ${mayBeId.getOrElse("None")} , existingSize ${existing.size}")
        Some(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
    }

    Future.successful(result)
  }

}

trait TaxResidentCountryIdCheckAction extends ActionFilter[SponsorNameRequest]
