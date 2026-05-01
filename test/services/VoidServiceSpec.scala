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
import connectors.FatcaVoidConnector
import models.VoidFatcaRequest
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{times, verify, when}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class VoidServiceSpec extends SpecBase {
  private val mockConnector = mock[FatcaVoidConnector]
  private val service       = new VoidService(mockConnector)

  given HeaderCarrier = HeaderCarrier()

  "fatcaVoid" - {
    "should call the connector with the correct request" in {
      when(mockConnector.submit(any[VoidFatcaRequest]())(any(), any())).thenReturn(Future.successful(()))

      service.fatcaVoid("testMessageRefId", "testFiid")

      verify(mockConnector, times(1)).submit(any[VoidFatcaRequest]())(any(), any())
    }
  }

}
