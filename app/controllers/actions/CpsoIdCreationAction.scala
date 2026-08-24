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

import connectors.DatabaseConnector
import models.requests.{CPSOIdRequest, ReportIdRequest}
import models.viewModels.manual.cpso.CPSOId
import models.{ReportId, UserAnswers}
import pages.manual.cpso.{CPSOsPage, CurrentCPSOIdPage}
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CpsoIdCreationActionImpl @Inject() (repository: DatabaseConnector)(implicit val executionContext: ExecutionContext) extends CpsoIdCreationAction {

  override protected def transform[A](request: ReportIdRequest[A]): Future[CPSOIdRequest[A]] = {
    given reportId: ReportId = request.reportId
    request.userAnswers.get(CurrentCPSOIdPage()) match {
      case None =>
        val id: CPSOId = createAndSetId(request)
        Future.successful(
          CPSOIdRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            cpsoId = id
          )
        )
      case Some(id) =>
        Future.successful(
          CPSOIdRequest(
            request = request.request,
            userId = request.userId,
            userAnswers = request.userAnswers,
            fatcaId = request.fatcaId,
            reportId = request.reportId,
            cpsoId = id
          )
        )
    }
  }

  private def createAndSetId[A](request: ReportIdRequest[A])(implicit reportId: ReportId) = {
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val existingIds = request.userAnswers
      .get(CPSOsPage())
      .map(
        cp => cp.cpsos.keySet
      )
      .getOrElse(Set.empty)
    val newId = CPSOId.generate(existingIds)

    request.userAnswers
      .get(CurrentCPSOIdPage())
      .fold {
        request.userAnswers
          .set(CurrentCPSOIdPage(), newId)
          .foreach(repository.set)
      }(
        _ => ()
      )
    newId
  }
}

trait CpsoIdCreationAction extends ActionTransformer[ReportIdRequest, CPSOIdRequest]
