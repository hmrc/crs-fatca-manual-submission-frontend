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

import config.FrontendAppConfig
import models.ServiceErrors.Downstream_Error
import models.subscription.{DisplaySubscriptionResponse, ReadSubscriptionRequest}
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class SubscriptionConnector @Inject() (
  val config: FrontendAppConfig,
  val http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends Logging {

  def readSubscription(
    readSubscriptionRequest: ReadSubscriptionRequest
  )(using hc: HeaderCarrier): Future[DisplaySubscriptionResponse] = {
    val readSubscriptionUrl = url"${config.registrationUrl}/subscription/read-subscription"

    http
      .post(readSubscriptionUrl)
      .withBody(Json.toJson(readSubscriptionRequest))
      .execute[HttpResponse]
      .flatMap {
        case res if res.status == OK =>
          Try(res.json.validate[DisplaySubscriptionResponse]) match {
            case Success(JsSuccess(displaySubscriptionResponse, _)) =>
              Future.successful(displaySubscriptionResponse)

            case Success(JsError(errors)) =>
              logger.error(s"Invalid JSON returned from read-subscription: $errors")
              Future.failed(Downstream_Error)

            case Failure(exception) =>
              logger.error("Unable to parse JSON returned from read-subscription", exception)
              Future.failed(Downstream_Error)
          }

        case res =>
          Future.failed(Downstream_Error)
      }
  }
}
