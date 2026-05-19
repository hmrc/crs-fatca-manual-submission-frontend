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

import models.UserData
import models.requests.{DataRequest, OptionalDataRequest}
import pages.FiNamePage
import play.api.mvc.ActionTransformer
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataCreationActionImpl @Inject() (implicit val executionContext: ExecutionContext, sessionRepository: SessionRepository) extends DataCreationAction {

  override protected def transform[A](request: OptionalDataRequest[A]): Future[DataRequest[A]] =
    request.userData match {
      case None =>
        val data = UserData(request.fatcaId).set(FiNamePage, "Test FI").get // TODO : Need to replaced once we integrate
        sessionRepository.set(data) // TODO : Need to replaced once we integrate
        Future.successful(DataRequest(request.request, request.userId, data, request.fatcaId))
      case Some(data) => Future.successful(DataRequest(request.request, request.userId, data, request.fatcaId))
    }
}

trait DataCreationAction extends ActionTransformer[OptionalDataRequest, DataRequest]
