package views.manual.cpso

import base.SpecBase
import forms.manual.cpso.ResidentForTaxFormProvider
import models.SubmissionsConstants.CRS
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.ResidentForTaxView

class ResidentForTaxViewSpec extends SpecBase {
  private val application = applicationBuilder().build()

  private val view: ResidentForTaxView                                   = application.injector.instanceOf[ResidentForTaxView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  val form = new ResidentForTaxFormProvider()(CRS)

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

}
