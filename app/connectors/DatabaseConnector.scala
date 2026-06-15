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
import models.UserAnswers
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.libs.ws.writeableOf_JsValue

import scala.concurrent.{ExecutionContext, Future}

class DatabaseConnector @Inject() (client: HttpClientV2, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {
  private val baseUrl = s"${config.crsFatcaManualBackendUrl}/crs-fatca-manual-submission/user-answer"

  def get()(implicit headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] =
    client
      .get(url"$baseUrl/get")
      .execute[HttpResponse](using readRaw, ec)
      .flatMap {
        response =>
          response.status match {
            case OK        => Future.successful(Some(response.json.as[UserAnswers]))
            case NOT_FOUND => Future.successful(None)
            case _         => Future.failed(Downstream_Error)
          }
      }

  def set(userAnswers: UserAnswers)(implicit headerCarrier: HeaderCarrier): Future[Unit] =
    client
      .post(url"$baseUrl/save")
      .withBody(Json.toJson(userAnswers))
      .execute[HttpResponse]
      .flatMap {
        response =>
          response.status match {
            case OK => Future.successful(())
            case _ =>
              Future.failed(InternalServerException("Unable to save UserAnswer"))
          }
      }
}
