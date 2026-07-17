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

import models.ElectionsId
import models.requests.{DataRequest, ElectionIdRequest}
import pages.FiDetailsPage
import play.api.mvc.{ActionRefiner, Results}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ElectionIdRequiredActionImpl @Inject() (implicit
  val executionContext: ExecutionContext
) extends ElectionIdRequiredAction {

  override protected def refine[A](request: DataRequest[A]): scala.concurrent.Future[Either[play.api.mvc.Result, ElectionIdRequest[A]]] =
    Future.successful {
      (request.userAnswers.get(FiDetailsPage), request.request.getQueryString("year")) match {
        case (Some(fiDetail), Some(reportingYear)) =>
          Right(
            ElectionIdRequest(
              request = request.request,
              userId = request.userId,
              userAnswers = request.userAnswers,
              fatcaId = request.fatcaId,
              electionsId = ElectionsId(fiId = fiDetail.fiId, reportingYear = reportingYear.toInt),
              fiDetail = fiDetail
            )
          )
        case _ => Left(Results.Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}

trait ElectionIdRequiredAction extends ActionRefiner[DataRequest, ElectionIdRequest]
