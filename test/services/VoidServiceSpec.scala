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
      when(mockConnector.submit(any[VoidFatcaRequest]())(any(),any())).thenReturn(Future.successful(()))

      service.fatcaVoid("testMessageRefId", "testFiid")

      verify(mockConnector, times(1)).submit(any[VoidFatcaRequest]())(any(),any())
    }
  }

}
