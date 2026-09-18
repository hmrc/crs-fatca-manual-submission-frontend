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

package pages.manual.account

import base.SpecBase
import models.ReportId
import models.SubmissionsConstants.CRS
import models.viewModels.AccountId

class DoYouNeedToAddPaymentsPageSpec extends SpecBase {

  "cleanup" - {
    implicit val reportId: ReportId = ReportId(CRS, 2023, None, "test1")

    "must remove CurrentAccountPaymentIndexPage when DoYouNeedToAddPaymentsPage is populated with a true value" in {

      val accountId = AccountId("TestAccountId")
      val userAnswers = emptyUserAnswers
        .withPage(CurrentAccountPaymentIndexPage(accountId), 0)

      userAnswers.get(CurrentAccountPaymentIndexPage(accountId)) mustBe Some(0)

      val ua = userAnswers.withPage(DoYouNeedToAddPaymentsPage(accountId), true)

      val result = DoYouNeedToAddPaymentsPage(accountId).cleanupWithReportId(Some(true), ua).success.value

      mustBeRemoved(result, CurrentAccountPaymentIndexPage(accountId))
    }
  }
}
