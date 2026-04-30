package services

import connectors.FatcaVoidConnector
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class VoidService @Inject() (fatcaConnector: FatcaVoidConnector) {

  def fatcaVoid(messageRefId: String, fiid: String)(using hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    val request = models.VoidFatcaRequest(messageRefId, fiid)
    fatcaConnector.submit(request)
  }

}
