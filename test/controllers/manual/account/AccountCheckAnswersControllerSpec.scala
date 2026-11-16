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
import models.viewModels.AccountId
import pages.ReportIdPage
import pages.manual.account.{AccountIdPage, CurrentAccountIdPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class AccountCheckAnswersControllerSpec extends SpecBase {

  "AccountCheckAnswersController" - {

    "must redirect to the accounts overview when Continue is selected" in {
      val accountId = AccountId("42")
      val answers = emptyUserAnswers
        .withPage(ReportIdPage, reportId)
        .withPage(AccountIdPage(accountId), accountId)
        .withPage(CurrentAccountIdPage(), accountId)

      val application = applicationBuilder(Some(answers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.AccountCheckAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AccountsController.onPageLoad().url
      }
    }
  }
}
