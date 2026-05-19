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

package base

import controllers.actions.*
import models.SubmissionsConstants.{FATCA, FATCA1, PASSED}
import models.{SubmissionsConstants, SubmittedReport, UserData}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.test.FakeRequest
import queries.Settable
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with BeforeAndAfterEach {

  val userAnswersId: String = "id"
  def now: LocalDateTime    = LocalDateTime.now()

  def emptyUserData: UserData              = UserData(userAnswersId)
  implicit val hc: HeaderCarrier           = HeaderCarrier()
  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(userData: Option[UserData] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[FrontendDataRetrievalAction].to(new FakeFrontendDataRetrievalAction(userData)),
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userData))
      )

  implicit class UserAnswersExtension(userData: UserData) {

    def withPage[T](page: Settable[T], value: T)(implicit writes: Writes[T]): UserData =
      userData.set(page, value).success.value
  }

  // TEST DATA:
  val submittedReport: SubmittedReport = SubmittedReport(
    fiId = "id",
    fiName = "name",
    fileName = "fileName",
    submissionStatus = PASSED,
    uploadDateTime = now,
    regime = FATCA,
    reportingYear = "2016",
    submissionCaseId = "123",
    submissionType = SubmissionsConstants.XML,
    submissionFileType = FATCA1,
    messageRefId = "ref1",
    submissionDeleteStatus = None,
    originalMessageRefId = None
  )

}
