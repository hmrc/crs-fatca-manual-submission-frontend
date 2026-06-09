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
import models.{FIDetail, ViewFIDetailsResponse}
import models.ServiceErrors.NoFiDetailFound
import models.subscription.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ConfirmationEmailRecipientsServiceSpec extends SpecBase {

  private val mockViewFIService       = mock[ViewFIService]
  private val mockSubscriptionService = mock[SubscriptionService]

  given HeaderCarrier    = HeaderCarrier()
  given ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val fiId           = "test-fi-id"
  private val subscriptionId = "XCRFA123456789"

  private val service =
    new ConfirmationEmailRecipientsService(
      mockViewFIService,
      mockSubscriptionService
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewFIService, mockSubscriptionService)
  }

  "ConfirmationEmailRecipientsService" - {

    "getEmailRecipients" - {

      "must return only subscription contact emails when the FI is the logged in user" in {
        val fiDetail =
          fiDetailResponse(
            isFiUser = true,
            primaryFiEmail = Some("fi-primary@example.com"),
            secondaryFiEmail = Some("fi-secondary@example.com")
          )

        val subscription =
          subscriptionResponse(
            primaryEmail = "user-primary@example.com",
            secondaryEmail = Some("user-secondary@example.com")
          )

        when(mockViewFIService.getFIDetail(any[String](), any[String]())(using any[HeaderCarrier]()))
          .thenReturn(Future.successful(fiDetail))

        when(mockSubscriptionService.subscription(any[SubscriptionID]())(using any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(subscription))

        val result = service.getEmailRecipients(fiId, subscriptionId).futureValue

        result mustBe Seq(
          "user-primary@example.com",
          "user-secondary@example.com"
        )
      }

      "must return subscription and FI contact emails when the FI is not the logged in user" in {
        val fiDetail =
          fiDetailResponse(
            isFiUser = false,
            primaryFiEmail = Some("fi-primary@example.com"),
            secondaryFiEmail = Some("fi-secondary@example.com")
          )

        val subscription =
          subscriptionResponse(
            primaryEmail = "user-primary@example.com",
            secondaryEmail = Some("user-secondary@example.com")
          )

        when(mockViewFIService.getFIDetail(any[String](), any[String]())(using any[HeaderCarrier]()))
          .thenReturn(Future.successful(fiDetail))

        when(mockSubscriptionService.subscription(any[SubscriptionID]())(using any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(subscription))

        val result = service.getEmailRecipients(fiId, subscriptionId).futureValue

        result mustBe Seq(
          "user-primary@example.com",
          "user-secondary@example.com",
          "fi-primary@example.com",
          "fi-secondary@example.com"
        )
      }

      "must remove duplicate email addresses" in {
        val fiDetail =
          fiDetailResponse(
            isFiUser = false,
            primaryFiEmail = Some("user-primary@example.com"),
            secondaryFiEmail = Some("fi-secondary@example.com")
          )

        val subscription =
          subscriptionResponse(
            primaryEmail = "user-primary@example.com",
            secondaryEmail = Some("fi-secondary@example.com")
          )

        when(mockViewFIService.getFIDetail(any[String](), any[String]())(using any[HeaderCarrier]()))
          .thenReturn(Future.successful(fiDetail))

        when(mockSubscriptionService.subscription(any[SubscriptionID]())(using any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(subscription))

        val result = service.getEmailRecipients(fiId, subscriptionId).futureValue

        result mustBe Seq(
          "user-primary@example.com",
          "fi-secondary@example.com"
        )
      }

      "must handle missing secondary contacts" in {
        val fiDetail =
          fiDetailResponse(
            isFiUser = false,
            primaryFiEmail = Some("fi-primary@example.com"),
            secondaryFiEmail = None
          )

        val subscription =
          subscriptionResponse(
            primaryEmail = "user-primary@example.com",
            secondaryEmail = None
          )

        when(mockViewFIService.getFIDetail(any[String](), any[String]())(using any[HeaderCarrier]()))
          .thenReturn(Future.successful(fiDetail))

        when(mockSubscriptionService.subscription(any[SubscriptionID]())(using any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(Future.successful(subscription))

        val result = service.getEmailRecipients(fiId, subscriptionId).futureValue

        result mustBe Seq(
          "user-primary@example.com",
          "fi-primary@example.com"
        )
      }

      "must return a failed future when FI detail cannot be found" in {
        when(mockViewFIService.getFIDetail(any[String](), any[String]())(using any[HeaderCarrier]()))
          .thenReturn(Future.failed(NoFiDetailFound))

        val result = service.getEmailRecipients(fiId, subscriptionId)

        result.failed.futureValue mustBe NoFiDetailFound
      }
    }
  }

  private def subscriptionResponse(
    primaryEmail: String,
    secondaryEmail: Option[String]
  ): DisplaySubscriptionResponse =
    DisplaySubscriptionResponse(
      DisplayResponseDetail(
        CrfaSubscriptionDetails(
          crfaReference = subscriptionId,
          tradingName = Some("Test Trading Name"),
          gbUser = true,
          primaryContact = ContactInformation(
            contactInformation = IndividualDetails(
              firstName = "Primary",
              lastName = "Contact"
            ),
            email = primaryEmail,
            phone = None,
            mobile = None
          ),
          secondaryContact = secondaryEmail.map {
            email =>
              ContactInformation(
                contactInformation = OrganisationDetails("Secondary Contact"),
                email = email,
                phone = None,
                mobile = None
              )
          }
        )
      )
    )

  private def fiDetailResponse(
    isFiUser: Boolean,
    primaryFiEmail: Option[String],
    secondaryFiEmail: Option[String]
  ): FIDetail = {
    val primaryContactDetails =
      primaryFiEmail
        .map {
          email =>
            Json.obj(
              "ContactName"  -> "Primary FI Contact",
              "EmailAddress" -> email,
              "PhoneNumber"  -> "01234567890"
            )
        }
        .getOrElse(Json.parse("null"))

    val secondaryContactDetails =
      secondaryFiEmail
        .map {
          email =>
            Json.obj(
              "ContactName"  -> "Secondary FI Contact",
              "EmailAddress" -> email,
              "PhoneNumber"  -> "01234567890"
            )
        }
        .getOrElse(Json.parse("null"))

    val json: JsValue =
      Json.obj(
        "ViewFIDetails" -> Json.obj(
          "ResponseCommon" -> Json.obj(
            "OriginatingSystem"  -> "SomeSystem",
            "TransmittingSystem" -> "TransmitSystem",
            "RequestType"        -> "VIEW",
            "Regime"             -> "FATCA",
            "ResponseParameters" -> Json.arr()
          ),
          "ResponseDetails" -> Json.obj(
            "FIDetails" -> Json.arr(
              Json.obj(
                "FIID"           -> "FI123",
                "FIName"         -> "Example Bank",
                "SubscriptionID" -> "SUB001",
                "TINDetails" -> Json.arr(
                  Json.obj(
                    "TINType"  -> "EIN",
                    "TIN"      -> "12-3456789",
                    "IssuedBy" -> "US"
                  )
                ),
                "GIIN"     -> "ABC123DEF",
                "IsFIUser" -> isFiUser,
                "AddressDetails" -> Json.obj(
                  "AddressLine1" -> "123 Main St",
                  "AddressLine2" -> "Suite 100",
                  "AddressLine3" -> null,
                  "AddressLine4" -> null,
                  "CountryCode"  -> "US",
                  "PostalCode"   -> "12345"
                ),
                "PrimaryContactDetails"   -> primaryContactDetails,
                "SecondaryContactDetails" -> secondaryContactDetails
              )
            )
          )
        )
      )

    json
      .as[ViewFIDetailsResponse]
      .ViewFIDetails
      .ResponseDetails
      .FIDetails
      .head
  }
}
