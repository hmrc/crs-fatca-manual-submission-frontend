/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.manual

import base.SpecBase
import controllers.routes
import forms.manual.HasFinishedCheckingFormProvider
import models.ServiceErrors.NoFiDetailFound
import models.SubmissionsConstants.FATCA
import models.response.{FIDetail, TINDetails, TINType}
import models.{ReportId, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.ReportIdPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ViewFIService
import utils.CheckDetailsForFinancialInstitutionUtil
import views.html.CheckDetailsForFinancialInstitutionView

import scala.concurrent.Future

class CheckDetailsForFinancialInstitutionControllerSpec extends SpecBase {

  private val formProvider = new HasFinishedCheckingFormProvider()
  private val form         = formProvider()

  private val fiDetailWithUtr: FIDetail = fiDetail.copy(
    TINDetails = Some(Seq(TINDetails(TINType.UTR, "1234567890", "GB"))),
    GIIN = Some("GIIN123")
  )

  private def baseUa(reportId: ReportId): UserAnswers =
    emptyUserAnswers.withPage(ReportIdPage, reportId)

  private def applicationWithFiDetail(
    userAnswers: UserAnswers = baseUa(reportId),
    detail: FIDetail = fiDetailWithUtr
  ) = {
    val mockService = mock[ViewFIService]
    when(mockService.getFIDetail(any(), any())(using any())).thenReturn(Future.successful(detail))

    applicationBuilder(maybeUserAnswers = Some(userAnswers))
      .overrides(bind[ViewFIService].toInstance(mockService))
      .build()
  }

  private lazy val getRoute  = controllers.manual.routes.CheckDetailsForFinancialInstitutionController.onPageLoad().url
  private lazy val postRoute = controllers.manual.routes.CheckDetailsForFinancialInstitutionController.onSubmit().url

  "CheckDetailsForFinancialInstitution Controller" - {

    "must return OK and the correct view for a GET" in {
      val answers     = baseUa(reportId)
      val application = applicationWithFiDetail(answers)

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[CheckDetailsForFinancialInstitutionView]
        val util    = application.injector.instanceOf[CheckDetailsForFinancialInstitutionUtil]

        status(result) mustBe OK
        contentAsString(result) mustBe view(
          form,
          util.getRows(fiDetailWithUtr, reportId.regime)(messages(application)),
          fiDetailWithUtr.FIName,
          reportId.regime,
          hasGiin = true,
          fiIsUser = fiDetailWithUtr.IsFIUser,
          utr = "1234567890",
          linkUrl = util.changeFiUrl(fiDetailWithUtr.FIID, fiDetailWithUtr.IsFIUser)
        )(request, messages(application)).toString
      }
    }

    "must redirect to Send a report when valid yes data is submitted" in {
      val application = applicationWithFiDetail()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.manual.routes.SendAReportController.onPageLoad().url
      }
    }

    "must redirect to Send a report when valid no data is submitted" in {
      val application = applicationWithFiDetail()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "false"))
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.manual.routes.SendAReportController.onPageLoad().url
      }
    }

    "must return a Bad Request and the form errors when invalid data is submitted" in {
      val application = applicationWithFiDetail()

      running(application) {
        val request        = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))
        val result         = route(application, request).value
        val view           = application.injector.instanceOf[CheckDetailsForFinancialInstitutionView]
        val util           = application.injector.instanceOf[CheckDetailsForFinancialInstitutionUtil]
        val formWithErrors = form.bind(Map("value" -> "invalid"))

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe view(
          formWithErrors,
          util.getRows(fiDetailWithUtr, reportId.regime)(messages(application)),
          fiDetailWithUtr.FIName,
          reportId.regime,
          hasGiin = true,
          fiIsUser = fiDetailWithUtr.IsFIUser,
          utr = "1234567890",
          linkUrl = util.changeFiUrl(fiDetailWithUtr.FIID, fiDetailWithUtr.IsFIUser)
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery when no FI detail is found" in {
      val mockService = mock[ViewFIService]
      when(mockService.getFIDetail(any(), any())(using any())).thenReturn(Future.failed(NoFiDetailFound))
      val application = applicationBuilder(maybeUserAnswers = Some(baseUa(reportId)))
        .overrides(bind[ViewFIService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when a CRS FI has no UTR" in {
      val application = applicationWithFiDetail(detail = fiDetailWithUtr.copy(TINDetails = None))

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must support FATCA FIs without a UTR" in {
      val fatcaReportId = ReportId(FATCA, 2025, None, "TestfiID")
      val answers       = baseUa(fatcaReportId)
      val detail        = fiDetailWithUtr.copy(TINDetails = None)
      val application   = applicationWithFiDetail(answers, detail)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(answers)))

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result  = route(application, request).value

        status(result) mustBe OK
      }
    }
  }
}
