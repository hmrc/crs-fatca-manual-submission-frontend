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

import models.{ReportId, UserAnswers}
import models.viewModels.AccountId
import pages.QuestionPage
import pages.manual.sponsor.CurrentTaxResidentCountryIndexPage
import play.api.libs.json.JsPath

import scala.util.{Success, Try}
//Todo write a test for this
final case class DoYouNeedToAddPaymentsPage(accountId: AccountId)(implicit reportId: ReportId) extends QuestionPage[Boolean]:

  override def path: JsPath = JsPath \ reportId.mongoKey \ "accounts" \ accountId.value \ "doYouNeedToAddPayments"

  override def cleanupWithReportId(
                                    value: Option[Boolean],
                                    userData: UserAnswers
                                  )(implicit reportId: ReportId): Try[UserAnswers] =
    value match {
      case Some(true) => userData.remove(CurrentAccountPaymentIndexPage(accountId))
      case _ => Success(userData)
    }
