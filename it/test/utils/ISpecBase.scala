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
    "mongodb.uri"                                            -> mongoUri,
    "play.filters.csrf.header.bypassHeaders.Csrf-Token"      -> "nocheck"
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
