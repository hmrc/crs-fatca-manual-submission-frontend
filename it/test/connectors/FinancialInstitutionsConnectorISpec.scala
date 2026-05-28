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

import models.ServiceErrors.NoFiDetailFound
import models.ViewFIDetailsResponse
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import play.api.http.Status.{MULTI_STATUS, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.Json
import utils.ISpecBase

import scala.concurrent.Await

class FinancialInstitutionsConnectorISpec extends ISpecBase {
  private val subscriptionId                                 = "XLF12313120312"
  private val fiId                                           = "2132137132"
  private lazy val connector: FinancialInstitutionsConnector = app.injector.instanceOf[FinancialInstitutionsConnector]
  private val viewFiUrl                                      = s"/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId"

  private val successResponse = Json.obj(
    "ViewFIDetails" -> Json.obj(
      "ResponseCommon" -> Json.obj(
        "OriginatingSystem"  -> "SomeSystem",
        "TransmittingSystem" -> "TransmitSystem",
        "RequestType"        -> "VIEW",
        "Regime"             -> "FATCA",
        "ResponseParameters" -> Json.arr(
          Json.obj(
            "ParamName"  -> "param1",
            "ParamValue" -> "value1"
          )
        )
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
            "IsFIUser" -> true,
            "AddressDetails" -> Json.obj(
              "AddressLine1" -> "123 Main St",
              "AddressLine2" -> "Suite 100",
              "AddressLine3" -> null,
              "AddressLine4" -> null,
              "CountryCode"  -> "US",
              "PostalCode"   -> "12345"
            ),
            "PrimaryContactDetails" -> Json.obj(
              "ContactName"  -> "John Doe",
              "EmailAddress" -> "john@example.com",
              "PhoneNumber"  -> "555-1234"
            ),
            "SecondaryContactDetails" -> null
          )
        )
      )
    )
  )
  private val noFiResponse = """
{
  "errorDetail": {
    "errorCode": "001",
    "errorMessage": "Invalid request"
  }
}
"""
  "FinancialInstitutionConnector" - {
    "viewFi" - {
      "should return successful FI detail in case of a 200 response" in {
        stubGet(viewFiUrl, OK, successResponse.toString)
        val result           = Await.result(connector.viewFi(subscriptionId, fiId), 2.seconds)
        val expectedResponse = Json.toJson(successResponse).as[ViewFIDetailsResponse].ViewFIDetails.ResponseDetails.FIDetails.headOption
        result mustBe expectedResponse
      }

      "should return NoFiDetailFound if an invalid payload is received" in {
        stubGet(viewFiUrl, OK, Json.obj().toString)
        val result = connector.viewFi(subscriptionId, fiId)
        result.failed.futureValue mustBe NoFiDetailFound
      }

      "should return None if api responds with UNPROCESSABLE_ENTITY" in {
        stubGet(viewFiUrl, UNPROCESSABLE_ENTITY, noFiResponse)
        val result           = Await.result(connector.viewFi(subscriptionId, fiId), 2.seconds)
        result mustBe None
      }

      "should return NoFiDetailFound for any other http response" in {
        stubGet(viewFiUrl, MULTI_STATUS, Json.obj().toString)
        val result = connector.viewFi(subscriptionId, fiId)
        result.failed.futureValue mustBe NoFiDetailFound
      }
    }
  }
}
