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

package controllers.actions

import models.SubmissionsConstants.RegimeType
import models.requests.HasReportIdRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Call, Result}

import scala.concurrent.Future

trait RegimeTypeFiltering[R[A] <: HasReportIdRequest[A]] extends ActionFilter[R] with Logging {

  def errorCall: Call

  def regime: RegimeType

  override protected def filter[A](request: R[A]): Future[Option[Result]] =
    Future.successful {
      val actualRegime = request.reportId.regime
      Option.when(actualRegime != regime) {
        logger.error(s"Redirecting as Regime - ${actualRegime.value} is not allowed")
        Redirect(errorCall)
      }
    }
}
