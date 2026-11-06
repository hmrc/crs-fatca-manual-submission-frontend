package views.manual.cpso

import base.SpecBase
import forms.manual.cpso.IsCpsoSectionCompleteFormProvider
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import views.html.manual.cpso.IsCpsoSectionCompleteView

import scala.jdk.CollectionConverters.*

class IsCpsoSectionCompleteViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: IsCpsoSectionCompleteView                            = application.injector.instanceOf[IsCpsoSectionCompleteView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  private val fiName = "FI Name"

  val formProvider = new IsCpsoSectionCompleteFormProvider()

  private def render(
    mode: Mode = NormalMode,
    fiName: String = fiName,
    regimeType: RegimeType = CRS
  ): Document =
    Jsoup.parse(view(formProvider(regimeType), mode, regimeType.value, fiName).body)

  "page title" - {
    "must use the controlling person wording for the crs regime" in {
      render().title() mustBe
        "Have you finished adding the controlling persons for the financial institution?" +
        " - Send a CRS or FATCA report - GOV.UK"
    }

    "must use the substantial owners wording for the fatca regime" in {
      render(regimeType = FATCA).title() mustBe
        "Have you finished adding the substantial owners for the financial institution?" +
        " - Send a CRS or FATCA report - GOV.UK"
    }
  }

  "page heading" - {
    "must use the controlling person header wording for the crs regime" in {
      render().select("h1").text() mustBe "Have you finished adding the controlling persons for FI Name?"
    }

    "must use the substantial owners header wording for the fatca regime" in {
      render(regimeType = FATCA).select("h1").text() mustBe "Have you finished adding the substantial owners for FI Name?"
    }
  }

  "paragraph" - {
    "must use the controlling person paragraph wording for the crs regime" in {
      render().select("p").text().contains("You can still change the information for the controlling persons later.") mustBe true
    }

    "must use the csubstantial owners paragraph wording for the crs regime" in {
      render(regimeType = FATCA).select("p").text().contains("You can still change the information for the substantial owners later.") mustBe true
    }
  }

  "submit button" - {

    "must render a continue button inside the form" in {
      val doc = render()

      doc.select("form button.govuk-button").size() mustBe 1
      doc.select("#submit").text() mustBe "Continue"
    }
  }

  "yes or no radios" - {

    "must render a yes and a no option bound to the value field" in {
      val radios = render().select("input[type=radio]")

      radios.size() mustBe 2
      radios.eachAttr("name").asScala.toSet mustBe Set("value")
      radios.eachAttr("value").asScala.toList mustBe List("true", "false")
    }

    "must label the options Yes and No" in {
      val doc = render()

      doc.select("label[for=value]").text() mustBe "Yes"
      doc.select("label[for=value-no]").text() mustBe "No"
    }

    "must have hidden legend label that has same value as header" in {
      val doc = render()

      doc.select("legend.govuk-visually-hidden").text().contains("Have you finished adding the controlling persons for FI Name?") mustBe true

      render(regimeType = FATCA)
        .select("legend.govuk-visually-hidden")
        .text()
        .contains("Have you finished adding the substantial owners for FI Name?") mustBe true
    }
  }
}
