package views.manual.sponsor

import base.SpecBase
import forms.SponsorResidentForTaxFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.sponsor.SponsorResidentForTaxView

class SponsorResidentForTaxViewSpec extends SpecBase {
  private val application = applicationBuilder().build()

  private val view: SponsorResidentForTaxView                            = application.injector.instanceOf[SponsorResidentForTaxView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  val formProvider = new SponsorResidentForTaxFormProvider()
  val form         = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "SponsorResidentForTaxView" - {
    val sponsorName = "Sponsor Name"

    val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, sponsorName, None)
    lazy val doc                            = Jsoup.parse(renderedHtml.body)
    println(renderedHtml.body)

    "must display title" in {
      doc.title() must include(s"Where is the sponsor resident for tax")
    }

    "must display heading" in {
      doc.select("h1").text() must include(s"Where is Sponsor Name resident for tax?")
    }

    "must display paragraph" in {
      doc.select("p").text() must include("If they are resident for tax in more than one country, then you can add other countries on the next page.")
    }

    "must contain a country select" in {

      doc.select("select#country").hasAttr("name") mustBe true
      doc.select("select#country").attr("name") mustBe "country"
    }

    "must display button" in {
      doc.select("button.govuk-button").text() mustBe "Save and continue"
    }

  }
}
