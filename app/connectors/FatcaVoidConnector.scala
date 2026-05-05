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
import models.VoidFatcaRequest
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FatcaVoidConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {

  def submit(requestBody: VoidFatcaRequest)(implicit
    hc: HeaderCarrier): Future[Unit] = {
    val url = url"${config.crsFatcaManualBackendUrl}/crs-fatca-manual-submission/submitVoidRequest"

    http
      .post(url)
      .withBody(Json.toJson(requestBody))
      .execute[HttpResponse]
      .flatMap {
        response =>
          response.status match {
            case OK =>
              Future.successful(())
            case _ =>
              Future.failed(InternalServerException("Unable to submit fatca void request"))
          }
      }
  }

}
