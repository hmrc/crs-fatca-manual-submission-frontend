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
import models.ReportId
import models.requests.{DataRequest, ReportIdRequest}
import pages.ReportIdPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReportIdRequiredActionImpl @Inject() (implicit
  val executionContext: ExecutionContext
) extends ReportIdRequiredAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, ReportIdRequest[A]]] =
    Future.successful {
      request.userAnswers.get(ReportIdPage) match {
        case Some(reportId) =>
          Right(toReportIdRequest(request, reportId))

        case None =>
          println(Console.MAGENTA + s"\n\nReportIdRequiredActionImpl\n\n" +Console.RESET)
          Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  private def toReportIdRequest[A](
    request: DataRequest[A],
    reportId: ReportId
  ): ReportIdRequest[A] =
    ReportIdRequest(
      request = request.request,
      userId = request.userId,
      userAnswers = request.userAnswers,
      fatcaId = request.fatcaId,
      reportId = reportId
    )
}

trait ReportIdRequiredAction extends ActionRefiner[DataRequest, ReportIdRequest]
