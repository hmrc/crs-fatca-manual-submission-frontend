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
import connectors.FinancialInstitutionsConnector
import models.FIDetail
import models.ServiceErrors.NoFiDetailFound
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.Helpers.*

import scala.concurrent.Future

class ViewFIServiceSpec extends SpecBase {

  private val mockConnector: FinancialInstitutionsConnector = mock[FinancialInstitutionsConnector]
  private val subId: String                                 = "sometestId123"
  private val fiId: String                                  = "testFiid"

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockConnector)
  }

  "ViewFIService" - {

    "getFIDetail" - {

      "return FIDetail when the connector returns Some(fiDetail)" in withService {
        service =>
          val mockFiDetail = mock[FIDetail]

          when(mockConnector.viewFi(eqTo(subId), eqTo(fiId))(any()))
            .thenReturn(Future.successful(Some(mockFiDetail)))

          val result = service.getFIDetail(subId, fiId)

          result.futureValue mustBe mockFiDetail
      }

      "return a failed future with NoFiDetailFound when the connector returns None" in withService {
        service =>
          when(mockConnector.viewFi(eqTo(subId), eqTo(fiId))(any()))
            .thenReturn(Future.successful(None))

          val result = service.getFIDetail(subId, fiId)

          result.failed.futureValue mustBe NoFiDetailFound
      }
    }
  }

  private def withService(testService: ViewFIService => Any): Any = {
    val app = applicationBuilder()
      .overrides(bind[FinancialInstitutionsConnector].toInstance(mockConnector))
      .build()

    running(app) {
      val service = app.injector.instanceOf[ViewFIService]
      testService(service)
    }
  }

}
