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
import models.SubmissionsConstants.CRS
import models.{Mode, NormalMode, ReportId}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.when
import pages.ReportIdPage
import pages.manual.sponsor.SponsorNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class FilerCategoryControllerSpec extends SpecBase {

  val mockDbConnector: DatabaseConnector = mock[DatabaseConnector]
  val mode: Mode                         = NormalMode

  "FilerCategoryController onPageLoad" - {
    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)

    "must redirect to JourneyRecoveryController when no UserAnswers are found" in {

      when(mockDbConnector.get()(any)) thenReturn Future.successful(None)

      val application = applicationBuilder(maybeUserAnswers = None)
        .overrides(bind[DatabaseConnector].toInstance(mockDbConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FilerCategoryController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to WhatTypeOfFilerIsSponsorController when a sponsor name is present" in {
      val userAnswers = ua.withPage(SponsorNamePage()(reportId), "Sponsor Name")

      when(mockDbConnector.get()(any)) thenReturn Future.successful(Some(userAnswers))

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers))
        .overrides(bind[DatabaseConnector].toInstance(mockDbConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FilerCategoryController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.WhatTypeOfFilerIsSponsorController.onPageLoad(mode).url
      }
    }

    "must redirect to WhatTypeOfFilerController when no sponsor name is present" in {

      when(mockDbConnector.get()(any)) thenReturn Future.successful(Some(ua))

      val application = applicationBuilder(maybeUserAnswers = Some(ua))
        .overrides(bind[DatabaseConnector].toInstance(mockDbConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.FilerCategoryController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.WhatTypeOfFilerController.onPageLoad(mode).url
      }
    }
  }
}
