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

package services

import base.SpecBase
import connectors.SubscriptionConnector
import models.subscription.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionServiceSpec extends SpecBase {

  private val mockConnector = mock[SubscriptionConnector]
  private val service       = new SubscriptionService(mockConnector)

  given HeaderCarrier    = HeaderCarrier()
  given ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val subscriptionId = SubscriptionID("XCRFA123456789")

  private val response =
    DisplaySubscriptionResponse(
      DisplayResponseDetail(
        CrfaSubscriptionDetails(
          crfaReference = "XCRFA123456789",
          tradingName = Some("Test Trading Name"),
          gbUser = true,
          primaryContact = ContactInformation(
            contactInformation = IndividualDetails(
              firstName = "Jack",
              lastName = "Witchell"
            ),
            email = "jack@example.com",
            phone = Some("01234567890"),
            mobile = None
          ),
          secondaryContact = Some(
            ContactInformation(
              contactInformation = OrganisationDetails("Test Organisation"),
              email = "org@example.com",
              phone = None,
              mobile = Some("07123456789")
            )
          )
        )
      )
    )

  "SubscriptionService" - {

    "subscription" - {

      "must call the connector with a ReadSubscriptionRequest built from the subscription id" in {
        when(
          mockConnector.readSubscription(any[ReadSubscriptionRequest])(using any[HeaderCarrier])
        ).thenReturn(Future.successful(response))

        service.subscription(subscriptionId).futureValue mustBe response

        verify(mockConnector, times(1))
          .readSubscription(ReadSubscriptionRequest(subscriptionId.value))
      }
    }
  }
}
