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

package connectors

import models.ServiceErrors.Downstream_Error
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{a, include, must, mustBe}
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import utils.ISpecBase

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class DatabaseConnectorSpec extends AnyFreeSpec with ISpecBase {

  lazy val connector: DatabaseConnector = app.injector.instanceOf[DatabaseConnector]
  val url                               = "/crs-fatca-manual-submission/submissionList"
  val jsValue: JsValue                  = Json.toJson(emptyUserAnswers)
  "DatabaseConnector" - {

    "get" - {
      "should return the Response when mongo return some data in Response" in {
        stubGetResponse(url, OK, jsValue.toString)

        val result = Await.result(connector.get(), 2.seconds)

        result mustBe Some(jsValue)
      }

      "should return None when mongo return no data" in {
        stubGetResponse(url, NOT_FOUND, Json.obj().toString)

        val result = Await.result(connector.get(), 2.seconds)

        result mustBe None
      }

      "should Downstream_Error when mongo return an error" in {
        stubGetResponse(url, INTERNAL_SERVER_ERROR, "")

        val result = connector.get()

        result.failed.value mustBe Downstream_Error

      }
    }
  }
}
