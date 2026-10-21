package views.manual.cpso

import base.SpecBase
import models.{CheckMode, Mode, NormalMode}
import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.ControllerHelpers.request2flash
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.RemoveTaxResidentCountryView


class RemoveTaxResidentCountryViewSpec extends SpecBase {
  private val application = applicationBuilder().build()

  private val view: RemoveTaxResidentCountryView = application.injector.instanceOf[RemoveTaxResidentCountryView]

  private val messagesControllerComponents: MessagesControllerComponents =
    application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))
}
