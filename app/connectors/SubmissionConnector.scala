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
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

class SubmissionConnector @Inject() (client: HttpClientV2, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {
  private val url = config.crsFatcaReportingBackendUrl

  def sendXml(xml: String)(implicit hc: HeaderCarrier): Future[String] =
    val endpoint = url"$url/crs-fatca-reporting/validate-send-submission"

    client
      .post(endpoint)
      .withBody(xml)
      .setHeader("Content-Type" -> "application/xml")
      .execute[HttpResponse]
      .flatMap {
        response =>
          response.status match
            case OK => Future.successful(response.body)
            case _ => Future.failed(Elections_Error)
      }

}
