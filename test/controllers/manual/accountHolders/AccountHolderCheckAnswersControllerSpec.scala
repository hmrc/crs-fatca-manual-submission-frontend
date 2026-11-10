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

package controllers.manual.accountHolders

import base.SpecBase
import models.NormalMode
import models.manual.accountHolders.{IndividualName, IndividualOrOrganisation}
import models.viewModels.AccountHolderId
import pages.ReportIdPage
import pages.manual.accountHolders.{AccountHolderIndividualNamePage, CurrentAccountHolderIdPage, IndividualOrOrganisationPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.CheckAnswersUtil
import views.html.manual.accountHolders.AccountHolderCheckAnswersView

class AccountHolderCheckAnswersControllerSpec extends SpecBase {

  private val accountHolderId = AccountHolderId("holderId")

  private val userAnswers = emptyUserAnswers
    .withPage(ReportIdPage, reportId)
    .withPage(CurrentAccountHolderIdPage()(reportId), accountHolderId)
    .withPage(IndividualOrOrganisationPage(accountHolderId)(reportId), IndividualOrOrganisation.Individual)
    .withPage(AccountHolderIndividualNamePage(accountHolderId)(reportId), IndividualName("firstName", "lastName"))

  "AccountHolderCheckAnswersController" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.manual.accountHolders.routes.AccountHolderCheckAnswersController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AccountHolderCheckAnswersView]
        val rows    = new CheckAnswersUtil().getAccountHolderRows(userAnswers)(messages(application), reportId, accountHolderId)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(rows)(request, messages(application)).toString
      }
    }

    "must redirect to the /under-construction page when submitted" in {
      val application = applicationBuilder(maybeUserAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, controllers.manual.accountHolders.routes.AccountHolderCheckAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.manual.accountHolders.routes.AccountHolderListController.onPageLoad(NormalMode).url
      }
    }
  }
}
