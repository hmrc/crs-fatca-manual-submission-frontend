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

import models.viewModels.TaskStatus
import models.viewModels.TaskStatus.{Completed, Incomplete}
import models.{viewModels, ReportId, UserAnswers}
import pages.manual.reportingFI.HaveYouFinishedCheckingFIDetailsPage

import javax.inject.Inject

class SendAReportService @Inject() {

  def reportingFIStatus(userAnswers: UserAnswers, reportId: ReportId): TaskStatus =
    userAnswers
      .get(HaveYouFinishedCheckingFIDetailsPage(reportId))
      .fold(Incomplete) {
        value => if value then Completed else Incomplete
      }

}
