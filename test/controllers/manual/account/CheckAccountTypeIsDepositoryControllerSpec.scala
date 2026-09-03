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

package controllers.manual.account

import base.SpecBase
import connectors.DatabaseConnector
import models.SubmissionsConstants.CRS
import models.manual.account.WhatAccountType
import models.viewModels.AccountId
import models.{NormalMode, ReportId}
import navigation.{FakeManualSubmissionNavigator, ManualSubmissionNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ReportIdPage
import pages.manual.account.{CurrentAccountIdPage, WhatAccountTypePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class CheckAccountTypeIsDepositoryControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val checkAccountTypeIsDepositoryRoute = controllers.manual.account.routes.CheckAccountTypeIsDepositoryController.onChangeRedirect(NormalMode).url

  "CheckAccountTypeIsDepositoryController" - {

    implicit val reportId: ReportId = ReportId(CRS, 2025, None, "TestfiID")
    val accountId: AccountId        = AccountId(value = "SomeId")

    val ua = emptyUserAnswers
      .withPage(ReportIdPage, reportId)
      .withPage(CurrentAccountIdPage(), accountId)

    "must redirect to paymentType controller if account type is not depository" in {

      val userAnswers = ua.withPage(WhatAccountTypePage(accountId), WhatAccountType.Custodial)

      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkAccountTypeIsDepositoryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.account.routes.PaymentTypeController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to underconstruction page when account type is depository" in {

      val mockSessionRepository = mock[DatabaseConnector]
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(maybeUserAnswers = Some(ua.withPage(WhatAccountTypePage(accountId), WhatAccountType.Depository)))
          .overrides(
            bind[ManualSubmissionNavigator].toInstance(new FakeManualSubmissionNavigator(onwardRoute)),
            bind[DatabaseConnector].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, checkAccountTypeIsDepositoryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
