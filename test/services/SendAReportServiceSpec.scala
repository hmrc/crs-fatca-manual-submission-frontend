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

package services

import base.SpecBase
import models.viewModels.TaskStatus.{Completed, Incomplete}
import pages.manual.cpso.IsCpsoSectionCompletePage

class SendAReportServiceSpec extends SpecBase {

  private val service = new SendAReportService()

  "SendAReportService" - {

    "reporting CPSO Complete Status" - {
      "should return incomplete when page is not answered" in {
        service.reportingCPSOCompleteStatus(emptyUserAnswers, reportId) mustBe Incomplete
      }

      "should return incomplete when page is answered as false" in {
        service.reportingCPSOCompleteStatus(emptyUserAnswers
                                              .withPage(IsCpsoSectionCompletePage(reportId), false),
                                            reportId
        ) mustBe Incomplete
      }

      "should return completed when page is answered as true" in {
        service.reportingCPSOCompleteStatus(emptyUserAnswers
                                              .withPage(IsCpsoSectionCompletePage(reportId), true),
                                            reportId
        ) mustBe Completed
      }
    }

  }

}
