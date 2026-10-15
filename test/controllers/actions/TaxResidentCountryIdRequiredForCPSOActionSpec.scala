package controllers.actions

import base.SpecBase
import models.requests.{CPSONameRequest, TaxResidentCountryIdForCPSORequest}
import org.mockito.MockitoSugar
import play.api.mvc.Result

import scala.concurrent.Future

class TaxResidentCountryIdRequiredForCPSOActionSpec extends SpecBase with MockitoSugar {

  class Harness extends TaxResidentCountryIdRequiredForCPSOActionImpl() {

    def callRefine[A](request: CPSONameRequest[A]): Future[Either[Result, TaxResidentCountryIdForCPSORequest[A]]] =
      refine(request)
  }

}
