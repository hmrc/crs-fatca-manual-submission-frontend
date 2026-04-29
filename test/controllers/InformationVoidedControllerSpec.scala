package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.InformationVoidedView

class InformationVoidedControllerSpec extends SpecBase {

  "InformationVoided Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val fiName      = "[fiName]"
      val dateTime    = "[on 28 April 2026 at 3:36pm]"
      val mris        = Seq("[GB2026GB-ABC1234567890-FATCA_003]") // , "[GB2026GB-ABC1234567890-FATCA_004]")
      val emailString = "email1@test.com"

      running(application) {
        val request = FakeRequest(GET, routes.InformationVoidedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InformationVoidedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fiName, dateTime, mris, emailString)(request, messages(application)).toString
      }
    }
  }

  "formatEmailList" - {
    val controller = new InformationVoidedController(null, null, null, null, null, null)
    "return a singular email" in {
      controller.formatEmailList(Seq("email1@test.com")) mustBe "email1@test.com"
    }

    "return two emails joined with 'and'" in {
      controller.formatEmailList(Seq("email1@test.com", "email2@test.com")) mustBe
        "email1@test.com and email2@test.com"
    }

    "return three emails with commas and 'and' before the last when given three emails" in {
      controller.formatEmailList(Seq("email1@test.com", "email2@test.com", "email3@test.com")) mustBe
        "email1@test.com, email2@test.com and email3@test.com"
    }

    "return four emails with commas and 'and' before the last when given four emails" in {
      controller.formatEmailList(Seq("email1@test.com", "email2@test.com", "email3@test.com", "email4@test.com")) mustBe
        "email1@test.com, email2@test.com, email3@test.com and email4@test.com"
    }
  }
}
