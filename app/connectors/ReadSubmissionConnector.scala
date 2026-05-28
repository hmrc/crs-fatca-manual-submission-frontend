/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors

import config.FrontendAppConfig
import models.ReadSubmissionResponseDetails
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubmissionConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig)(using ec: ExecutionContext) extends Logging {

  def getSubmissionsList(fiId: String)(using
    hc: HeaderCarrier
  ): Future[ReadSubmissionResponseDetails] = {
    val url = url"${config.crsFatcaManualBackendUrl}/crs-fatca-manual-submission/read-submission-history/$fiId"

    http
      .get(url)
      .execute[HttpResponse]
      .flatMap {
        response =>
          response.status match {
            case OK =>
              Future.successful(Json.parse(response.body).as[ReadSubmissionResponseDetails])
            case _ =>
              Future.failed(InternalServerException("Unable to retrieve submission history"))
          }
      }
  }

}
