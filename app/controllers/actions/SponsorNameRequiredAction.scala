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
import models.requests.{ReportIdRequest, SponsorNameRequest}
import pages.manual.sponsor.SponsorNamePage
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SponsorNameRequiredActionImpl @Inject() (implicit
  val executionContext: ExecutionContext
) extends SponsorNameRequiredAction
    with Logging {

  override protected def refine[A](request: ReportIdRequest[A]): Future[Either[Result, SponsorNameRequest[A]]] =
    Future.successful {
      request.userAnswers.get(SponsorNamePage()(request.reportId)) match {
        case Some(name) =>
          Right(toSponsorNameRequest(request, name))

        case None =>
          logger.error("Unable to find SponsorName in User Answer")
          Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  private def toSponsorNameRequest[A](
    request: ReportIdRequest[A],
    sponsorName: String
  ): SponsorNameRequest[A] =
    SponsorNameRequest(
      request = request.request,
      userId = request.userId,
      userAnswers = request.userAnswers,
      fatcaId = request.fatcaId,
      reportId = request.reportId,
      sponsorName = sponsorName
    )

}

trait SponsorNameRequiredAction extends ActionRefiner[ReportIdRequest, SponsorNameRequest]
