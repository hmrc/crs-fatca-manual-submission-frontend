package views.manual.sponsor

import base.SpecBase
import forms.manual.sponsor.{RemoveTaxResidentCountryFormProvider, SponsorNameFormProvider}
import models.NormalMode
import models.sponsor.RemoveCountryMessage.{AllOtherCountryMessage, NationsWithDefiniteArticlesMessage, OtherCountryMessage}
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.manual.sponsor.{RemoveTaxResidentCountryView, UKPostcodeView}

class RemoveTaxResidentCountryViewSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private val view: RemoveTaxResidentCountryView                         = application.injector.instanceOf[RemoveTaxResidentCountryView]
  private val messagesControllerComponents: MessagesControllerComponents = application.injector.instanceOf[MessagesControllerComponents]
  val formProvider                                                       = new RemoveTaxResidentCountryFormProvider()
  val form                                                               = formProvider()

  implicit private val request: FakeRequest[AnyContent] = FakeRequest()
  implicit private val messages: Messages               = messagesControllerComponents.messagesApi.preferred(Seq(Lang("en")))

  "RemoveTaxResidentCountryView" - {
    val sponsorName = "testName"

    "must display title and Heading" - {
      "definite article countries" in {
        val country                             = "United Kingdom"
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, sponsorName, country, NationsWithDefiniteArticlesMessage)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)

        doc.title() must include(s"Are you sure you want to remove the United Kingdom as a tax resident country for the sponsor?")
        doc.select("h1").text() must include(s"Are you sure you want to remove the United Kingdom as a tax resident country for testName?")

        doc.select(".govuk-radios__label").text().trim().contains("Yes")
        doc.select(".govuk-radios__label").text().trim().contains("No")
        doc.select(".govuk-radios__input").text().trim().contains("true")
        doc.select(".govuk-radios__input").text().trim().contains("false")

        doc.select("#submit").text() must include("Save and continue")
      }
      "other countries" in {
        val country                             = "Zamunda"
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, sponsorName, country, OtherCountryMessage)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)

        doc.title() must include(s"Are you sure you want to remove ‘Other country’ as a tax resident country for the sponsor?")
        doc.select("h1").text() must include(s"Are you sure you want to remove ‘Other country’ as a tax resident country for testName?")

      }
      "all other countries" in {
        val country                             = "Somalia"
        val renderedHtml: HtmlFormat.Appendable = view(form, NormalMode, sponsorName, country, AllOtherCountryMessage)
        lazy val doc                            = Jsoup.parse(renderedHtml.body)

        doc.title() must include(s"Are you sure you want to remove Somalia as a tax resident country for the sponsor?")
        doc.select("h1").text() must include(s"Are you sure you want to remove Somalia as a tax resident country for testName?")
      }
    }
  }
}
