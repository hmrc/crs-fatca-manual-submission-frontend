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

package controllers.manual.filercategory

import base.SpecBase
import connectors.DatabaseConnector
import models.ReportId
import models.SubmissionsConstants.CRS
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.ReportIdPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.CheckAnswersUtil
import views.html.manual.filercategory.FilerCategoryCheckAnswersView

import scala.concurrent.Future

class FilerCategoryCheckAnswersControllerSpec extends SpecBase {

  private val mockUtil: CheckAnswersUtil = mock[CheckAnswersUtil]
  private val mockSessionRepository      = mock[DatabaseConnector]

  def onPageLoadRoute: String =
    controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onPageLoad().url

  def onSubmitRoute: String =
    controllers.manual.filercategory.routes.FilerCategoryCheckAnswersController.onSubmit().url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUtil)
    reset(mockSessionRepository)
  }
  implicit private val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")

  private val ua = emptyUserAnswers
    .withPage(ReportIdPage, reportId)

  "FilerCategoryCheckAnswersController" - {

    ".onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val list: SummaryList = SummaryList(Seq.empty)
        when(mockUtil.getFilerCategoryRows(any())(any(), any())) thenReturn list
        when(mockSessionRepository.get()(any())) thenReturn Future.successful(Some(ua))

        val application =
          applicationBuilder(maybeUserAnswers = Some(ua))
            .overrides(
              bind[DatabaseConnector].toInstance(mockSessionRepository),
              bind[CheckAnswersUtil].toInstance(mockUtil)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[FilerCategoryCheckAnswersView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list)(request, messages(application)).toString

          verify(mockUtil).getFilerCategoryRows(meq(ua))(any(), any())
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(maybeUserAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, onPageLoadRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    ".onSubmit" - {

      "must redirect to the Send A Report page" in {

        val application = applicationBuilder(maybeUserAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(POST, onSubmitRoute)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.manual.routes.SendAReportController.onPageLoad().url
        }
      }
    }
  }
}
