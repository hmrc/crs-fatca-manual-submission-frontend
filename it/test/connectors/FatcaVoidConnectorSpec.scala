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

import models.VoidFatcaRequest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{a, include, must, mustBe}
import play.api.http.Status.*
import uk.gov.hmrc.http.InternalServerException
import utils.ISpecBase

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class FatcaVoidConnectorSpec extends ISpecBase {

  lazy val connector: FatcaVoidConnector = app.injector.instanceOf[FatcaVoidConnector]

  "FatcaVoidConnector" - {

    "submit" - {
      val submitUrl = "/crs-fatca-manual-submission/submitVoidRequest"

      val request = VoidFatcaRequest("testMessageRefId","testFiiD")

      "should return the Response when Backend return successful Response" in {
        stubPostResponse(submitUrl, OK)

        val result = Await.result(connector.submit(request), 2.seconds)

        result mustBe ()
      }

      "should return Future Failure When Backend return non 200 response" in {
        stubPostResponse(submitUrl, INTERNAL_SERVER_ERROR)

        val result: Future[Unit] = connector.submit(request)

        val exception = result.failed.futureValue

        exception mustBe a[InternalServerException]
        exception.getMessage must include("Unable to submit fatca void request")

      }
    }
  }
}
