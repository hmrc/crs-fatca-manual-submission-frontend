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

import models.ServiceErrors.Elections_Error
import models.elections.{CrsElectionsDetails, ElectionDetails, FatcaElectionsDetails, YesNoNa}
import models.requests.ElectionsSubmissionDetails
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{a, include, must, mustBe, mustEqual}
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.InternalServerException
import utils.ISpecBase

import scala.concurrent.duration.DurationInt
import scala.concurrent.Await

class ElectionsConnectorISpec extends AnyFreeSpec with ISpecBase {

  lazy val connector: ElectionsConnector = app.injector.instanceOf[ElectionsConnector]

  val fiid = "1234567890"
  val url  = s"/elections/view/$fiid"

  override def beforeEach(): Unit =
    server.resetAll()

  "ElectionsConnector" - {
    "viewElections" - {
      "should return a list of ElectionDetails" in new TestContext {
        stubGetResponse(url, OK, Json.toJson(fullElectionDetails).toString)

        private val result = connector.viewElections(fiid)

        result.futureValue mustBe fullElectionDetails
      }

      "should return a list of ElectionDetails from particular reporting year" in new TestContext {
        val year = 2023
        val url  = s"/elections/view/$fiid/$year"

        stubGetResponse(url, OK, Json.toJson(Seq(detailsFrom2023)).toString)

        private val result = connector.viewElections(fiid, Some(year))

        result.futureValue mustBe Seq(detailsFrom2023)
      }

      "should return a list of ElectionDetails with crs and fatca as None" in new TestContext {
        stubGetResponse(url, OK, Json.toJson(noElectionDetails).toString)

        private val result = connector.viewElections(fiid)

        result.futureValue mustBe noElectionDetails
      }

      "should return a failed future with Elections_Error on a non-OK response" in new TestContext {
        stubGetResponse(url, NOT_FOUND)

        private val result = connector.viewElections(fiid)

        result.failed.futureValue mustBe Elections_Error
      }
    }

    "submit" - {
      val submitUrl = "/crs-fatca-reporting/elections/submit"
      val testFiId = "TestFIID"
      val reportingYear = "2026"

      val request = ElectionsSubmissionDetails(testFiId, reportingYear, None, None)

      "should return the Response when Backend return successful Response" in {
        stubPostResponse(submitUrl, NO_CONTENT)

        val result = Await.result(connector.submit(request), 2.seconds)

        result mustBe()
      }

      "should return Future Failure When Backend return non 200 response" in {
        stubPostResponse(submitUrl, INTERNAL_SERVER_ERROR)

        val result = connector.submit(request)

        val exception = result.failed.futureValue

        exception mustBe a[InternalServerException]
        exception.getMessage must include("Unable to submit Elections request")

      }
    }
  }

  trait TestContext {

    val fullCrs: CrsElectionsDetails = CrsElectionsDetails(
      hasCARF = Some(YesNoNa.Yes),
      hasContracts = Some(YesNoNa.No),
      hasDormantAccounts = Some(YesNoNa.NA),
      hasThresholds = Some(YesNoNa.Yes)
    )

    val fullFatca: FatcaElectionsDetails = FatcaElectionsDetails(
      hasThresholds = Some(YesNoNa.No),
      hasTreasuryRegulations = Some(YesNoNa.NA)
    )

    val emptyCrs: CrsElectionsDetails     = CrsElectionsDetails(None, None, None, None)
    val emptyFatca: FatcaElectionsDetails = FatcaElectionsDetails(None, None)

    val detailsFrom2024: ElectionDetails =
      ElectionDetails(
        crs = Some(fullCrs),
        fatca = Some(fullFatca),
        reportingPeriod = "2024"
      )

    val detailsFrom2023: ElectionDetails =
      ElectionDetails(
        crs = Some(fullCrs),
        fatca = Some(fullFatca),
        reportingPeriod = "2023"
      )

    val noElectionDetails: Seq[ElectionDetails] = Seq(
      ElectionDetails(
        crs = None,
        fatca = None,
        reportingPeriod = "2024"
      )
    )

    val fullElectionDetails: Seq[ElectionDetails] = Seq(
      detailsFrom2024,
      detailsFrom2023
    )
  }

}
