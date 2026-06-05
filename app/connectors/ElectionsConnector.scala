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

package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import models.ServiceErrors.Elections_Error
import models.elections.ElectionDetails
import models.requests.ElectionsSubmissionRequest
import play.api.Logging
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue

import scala.concurrent.{ExecutionContext, Future}

class ElectionsConnector @Inject() (client: HttpClientV2, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {
  private val url = config.crsFatcaReportingBackendUrl

  def viewElections(fiId: String, reportingYear: Option[Int] = None)(implicit hc: HeaderCarrier): Future[Seq[ElectionDetails]] =
    val endpoint = reportingYear.fold(url"$url/elections/view/$fiId")(
      reportingYear => url"$url/elections/view/$fiId/$reportingYear"
    )
    client
      .get(endpoint)
      .execute[HttpResponse]
      .flatMap {
        response =>
          response.status match
            case OK => Future.successful(response.json.as[Seq[ElectionDetails]])
            case _  => Future.failed(Elections_Error)
      }

  def submit(requestBody: ElectionsSubmissionRequest)(using hc: HeaderCarrier): Future[Unit] =
    val endPoint = url"$url/crs-fatca-reporting/elections/submit"

    client
      .post(endPoint)
      .withBody(Json.toJson(requestBody))
      .execute[HttpResponse]
      .flatMap {
        response =>
          response.status match {
            case NO_CONTENT => Future.successful(())
            case _ =>
              Future.failed(InternalServerException("Unable to submit Elections request"))
          }
      }
}
