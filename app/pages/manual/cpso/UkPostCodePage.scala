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

package pages.manual.cpso

import models.{ReportId, UserAnswers}
import models.viewModels.manual.cpso.CPSOId
import pages.QuestionPage
import play.api.Logging
import play.api.libs.json.JsPath

import scala.util.{Failure, Success, Try}

final case class UkPostCodePage(currentId: CPSOId, reportId: ReportId) extends QuestionPage[String] with Logging:

  override def path: JsPath = JsPath \ reportId.mongoKey \ "cp-so" \ currentId.value \ "ukPostcode"

  override def cleanupWithReportId(
                                    value: Option[String],
                                    userData: UserAnswers
                                  )(implicit reportId: ReportId): Try[UserAnswers] =
    value match {
      case Some(_) =>
        cleanUpPages
          .foldLeft(Try(userData))(removePage())
          .recoverWith {
            case e =>
              logger.error(
                s"Failed to clean up pages for reportId=$reportId, cpsoId=${currentId.value}",
                e
              )
              Failure(e)
          }
      case _ => Success(userData)
    }

  private val cleanUpPages: Seq[QuestionPage[_]] = List(
    AddressLookupPage(currentId, reportId),
//    SelectAddressPage(currentId, reportId),
    IsThisTheAddressPage(currentId, reportId),
//    UkAddressPage(currentId, reportId)
  )
