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

import base.SpecBase
import connectors.DatabaseConnector
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  class Harness(userDataConnector: DatabaseConnector) extends DataRetrievalActionImpl(userDataConnector) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {
        val emptyJson = Json.obj()
        val connector = mock[DatabaseConnector]
        when(connector.get()(any())) thenReturn Future(None)
        val action = new Harness(connector)

        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id", "FATCAID", Organisation)).futureValue

        result.maybeAnswers.map(_.data mustBe emptyJson)
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val connector = mock[DatabaseConnector]
        when(connector.get()(any())) thenReturn Future(Some(Json.parse(Json.obj("someField" -> "someData").toString)))
        val action = new Harness(connector)

        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id", "FATCAID", Organisation)).futureValue

        result.maybeAnswers mustBe defined
      }
    }
  }
}
