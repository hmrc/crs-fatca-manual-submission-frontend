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

package utils

import generators.Generators
import models.SubmissionsConstants.{FATCA, FATCA1, PASSED}
import models.{SubmissionsConstants, SubmittedReport, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import queries.Settable
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.LocalDateTime

trait ISpecBase
    extends AnyFreeSpec
    with GuiceOneServerPerSuite
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with WireMockHelper
    with Generators {

  val userAnswersId: String         = "internalId"
  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)
  val submittedReport: SubmittedReport = SubmittedReport(
    fiId = "id",
    fiName = "name",
    fileName = "fileName",
    submissionStatus = PASSED,
    uploadDateTime = LocalDateTime.now(),
    regime = FATCA,
    reportingYear = "2016",
    submissionCaseId = "123",
    submissionType = SubmissionsConstants.XML,
    submissionFileType = FATCA1,
    messageRefId = "ref1",
    submissionDeleteStatus = None,
    originalMessageRefId = None
  )
  
  val mockResponse = 
  """
    |          {"submissionsList":[{"fiId":"SAR5797336789","fiName":"Test FI Name","fileName":"GB2023GB-SAR1625999529-SVTEST3799","submissionStatus":"FAILED","uploadDateTime":"2026-06-02T15:05:48.724Z","reportingYear":"2023","submissionCaseId":"CRS-SUB-46014","submissionType":"XML","messageRefId":"GB2023GB-SAR5797336789-SVTEST3799","submissionFileType":"CRS701","regime":"CRS"},{"fiId":"SAR5797336789","fiName":"Test FI Name","fileName":"GB2023GB-SAR5797336789-SVTEST3799","submissionStatus":"PASSED","uploadDateTime":"2026-06-02T11:08:02.915Z","reportingYear":"2023","submissionCaseId":"CRS-SUB-45015","submissionType":"XML","messageRefId":"GB2023GB-SAR5797336789-SVTEST3799","submissionFileType":"CRS701","regime":"CRS"},{"fiId":"SAR5797336789","fiName":"Barclays","fileName":"GB2023GB-SAR5797336789-SWTEST605161","submissionStatus":"PASSED","uploadDateTime":"2026-06-02T11:20:56.973Z","reportingYear":"2023","submissionCaseId":"FAT-SUB-5009","submissionType":"XML","messageRefId":"GB2023GB-SAR5797336789-SWTEST605161","submissionFileType":"FATCA1","regime":"FATCA","submissionDeleteStatus":true}]} 
    |""".stripMargin
  val repository: SessionRepository = app.injector.instanceOf[SessionRepository]
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  def config: Map[String, String] = Map(
    "microservice.services.auth.host" -> WireMockConstants.stubHost,
    "microservice.services.auth.port" -> WireMockConstants.stubPort.toString,
    "microservice.services.crs-fatca-manual-submission.host" -> WireMockConstants.stubHost,
    "microservice.services.crs-fatca-manual-submission.port" -> WireMockConstants.stubPort.toString,
    "microservice.services.crs-fatca-reporting.host" -> WireMockConstants.stubHost,
    "microservice.services.crs-fatca-reporting.port" -> WireMockConstants.stubPort.toString,
    "mongodb.uri" -> mongoUri,
    "microservice.services.crs-fatca-fi-management.port"     -> WireMockConstants.stubPort.toString,
    "microservice.services.crs-fatca-fi-management.host"     -> WireMockConstants.stubHost,
    "microservice.services.crs-fatca-reporting.port" -> WireMockConstants.stubPort.toString,
    "mongodb.uri"                                            -> mongoUri,
    "play.filters.csrf.header.bypassHeaders.Csrf-Token"      -> "nocheck",
    "microservice.services.crs-fatca-registration.host" -> WireMockConstants.stubHost,
    "microservice.services.crs-fatca-registration.port" -> WireMockConstants.stubPort.toString,
    //    "logger.root"                                             -> "INFO",
    //    "logger.controllers"                                      -> "DEBUG"
  )

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(20, Seconds)))

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build()

  implicit class UserAnswersExtension(userData: UserAnswers) {

    def withPage[T](page: Settable[T], value: T)(implicit writes: Writes[T]): UserAnswers =
      userData.set(page, value).success.value

  }

}
