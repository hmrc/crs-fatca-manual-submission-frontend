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

package pages.manual.sponsor

import models.{ReportId, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.{Success, Try}

final case class HaveSponsorPage()(implicit reportId: ReportId) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ reportId.mongoKey \ "haveSponsor"

  override def cleanupWithReportId(
    value: Option[Boolean],
    userData: UserAnswers
  )(implicit reportId: ReportId): Try[UserAnswers] =
    value match {
      case Some(false) => userData.remove(SponsorNamePage())
      case _           => Success(userData)
    }
}
