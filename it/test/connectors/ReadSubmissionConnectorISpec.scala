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

import models.ReadSubmissionResponseDetails
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{a, include, must, mustBe}
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.InternalServerException
import utils.ISpecBase
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class ReadSubmissionConnectorISpec extends ISpecBase {

  lazy val connector: ReadSubmissionConnector = app.injector.instanceOf[ReadSubmissionConnector]
  val readSubmissionUrl = "/crs-fatca-manual-submission/read-submission-history/fiId"
  val response = ReadSubmissionResponseDetails(submissionsList = List(submittedReport))

  "ReadSubmissionConnector" - {

    "submissionList" - {

      "should return the Response when EIS return successful Response" in {
        stubGet(readSubmissionUrl, OK, Json.toJson(response).toString)
        val result : ReadSubmissionResponseDetails = Await.result(connector.getSubmissionsList("fiId"), 2.seconds)
        result mustBe response
      }

      "should return Future Failure When EIS return INTERNAL_SERVER_ERROR" in {
        stubGet(readSubmissionUrl, INTERNAL_SERVER_ERROR, Json.obj().toString)

        val result: Future[ReadSubmissionResponseDetails] = connector.getSubmissionsList("fiId")

        val exception = result.failed.futureValue

        exception mustBe a[InternalServerException]
        exception.getMessage must include("Unable to retrieve submission history")

      }
    }
  }
}
