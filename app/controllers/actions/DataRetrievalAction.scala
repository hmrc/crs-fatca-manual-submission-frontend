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

import connectors.DatabaseConnector
import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject() (
  val userDataConnector: DatabaseConnector
)(implicit val executionContext: ExecutionContext)
    extends DataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    userDataConnector.get().map {
      data =>
        OptionalDataRequest(request.request,
                            request.userId,
                            Some(UserAnswers(id = request.fatcaId, data = data.getOrElse(Json.obj()).as[JsObject])),
                            request.fatcaId
        )
    }

}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalDataRequest]
