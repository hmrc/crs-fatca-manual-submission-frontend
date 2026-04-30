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
import models.ServiceErrors.Downstream_Error
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

class DatabaseConnector @Inject() (client: HttpClientV2, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {
  private val url = config.crsFatcaManualBackendUrl

  def get()(implicit headerCarrier: HeaderCarrier): Future[Option[JsValue]] =
    client
      .get(url"$url/crs-fatca-manual-submission/submissionList")
      .execute[HttpResponse](using readRaw, ec)
      .flatMap {
        response =>
          logger.info(s"status is ${response.status}")
          response.status match {
            case OK        => Future.successful(Some(response.json))
            case NOT_FOUND => Future.successful(None)
            case _         => Future.failed(Downstream_Error)
          }
      }

}
