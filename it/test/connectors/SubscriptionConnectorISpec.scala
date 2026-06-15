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

package connectors

import models.ServiceErrors.Downstream_Error
import models.subscription.*
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import play.api.http.Status.*
import play.api.libs.json.Json
import utils.ISpecBase

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class SubscriptionConnectorISpec extends ISpecBase {

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  "SubscriptionConnector" - {

    "readSubscription" - {

      val readSubscriptionUrl = "/crs-fatca-registration/subscription/read-subscription"

      val request = ReadSubscriptionRequest("test-safe-id")

      val displaySubscriptionResponse = DisplaySubscriptionResponse(
        success = DisplayResponseDetail(
          crfaSubscriptionDetails = CrfaSubscriptionDetails(
            crfaReference = "crfa-reference",
            tradingName = Some("Test Trading Name"),
            gbUser = true,
            primaryContact = ContactInformation(
              contactInformation = OrganisationDetails("Primary Organisation"),
              email = "primary@example.com",
              phone = Some("01234567890"),
              mobile = Some("07123456789")
            ),
            secondaryContact = Some(
              ContactInformation(
                contactInformation = IndividualDetails(
                  firstName = "Secondary",
                  lastName = "Contact"
                ),
                email = "secondary@example.com",
                phone = Some("01234567891"),
                mobile = None
              )
            )
          )
        )
      )

      "should return the DisplaySubscriptionResponse when Backend returns a successful response" in {
        stubPostResponse(
          readSubscriptionUrl,
          OK,
          Json.toJson(displaySubscriptionResponse).toString()
        )

        val result = Await.result(connector.readSubscription(request), 2.seconds)

        result mustBe displaySubscriptionResponse
      }

      "should return Future Failure when Backend returns a non 200 response" in {
        stubPostResponse(readSubscriptionUrl, INTERNAL_SERVER_ERROR)

        val result: Future[DisplaySubscriptionResponse] = connector.readSubscription(request)

        val exception = result.failed.futureValue

        exception mustBe Downstream_Error
      }

      "should return Future Failure when Backend returns invalid JSON" in {
        val invalidResponseJson = Json.obj(
          "success" -> Json.obj(
            "crfaSubscriptionDetails" -> Json.obj(
              "crfaReference" -> "crfa-reference"
            )
          )
        )

        stubPostResponse(
          readSubscriptionUrl,
          OK,
          invalidResponseJson.toString()
        )

        val result: Future[DisplaySubscriptionResponse] = connector.readSubscription(request)

        val exception = result.failed.futureValue

        exception mustBe Downstream_Error
      }

      "should return Future Failure when Backend returns malformed JSON" in {
        stubPostResponse(readSubscriptionUrl, OK, "not-json")

        val result: Future[DisplaySubscriptionResponse] = connector.readSubscription(request)

        val exception = result.failed.futureValue

        exception mustBe Downstream_Error
      }
    }
  }
}