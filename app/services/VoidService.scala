package services

import connectors.FatcaVoidConnector
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class VoidService @Inject() (fatcaConnector: FatcaVoidConnector)(implicit ec: ExecutionContext, hc: HeaderCarrier) {

  def fatcaVoid(messageRefId: String, fiid: String): Unit = {
    val request = models.VoidFatcaRequest(messageRefId, fiid)
    fatcaConnector.submit(request)
  }

}
