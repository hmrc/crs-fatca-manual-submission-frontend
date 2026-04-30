package services

import base.SpecBase
import connectors.FatcaVoidConnector
import models.VoidFatcaRequest
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{verify, when}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class VoidServiceSpec extends SpecBase {
  private val mockConnector = mock[FatcaVoidConnector]
  private val service       = new VoidService(mockConnector)

  "fatcaVoid" - {
    "should call the connector with the correct request" ignore {
      when(mockConnector.submit(any[VoidFatcaRequest]())(any[HeaderCarrier](), any[ExecutionContext]())).thenReturn(Future.successful(()))

      service.fatcaVoid("testMessageRefId", "testFiid")

      verify(mockConnector).submit(VoidFatcaRequest("testMessageRefId", "testFiid"))
    }
  }

}
