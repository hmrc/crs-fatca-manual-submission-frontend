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
import models.ServiceErrors.NoFiDetailFound
import models.{FIDetail, ViewFIDetailsResponse}
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialInstitutionsConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig)(using ec: ExecutionContext) extends Logging {

  def viewFi(subscriptionId: String, fiId: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[FIDetail]] =
    http
      .get(url"${config.fIManagementUrl}/crs-fatca-fi-management/financial-institutions/$subscriptionId/$fiId")
      .execute[HttpResponse]
      .flatMap {
        response =>
          response.status match {
            case OK =>
              response.json.validate[ViewFIDetailsResponse] match {
                case JsSuccess(viewFiDetails, _) =>
                  Future.successful(viewFiDetails.ViewFIDetails.ResponseDetails.FIDetails.headOption)
                case JsError(errors) =>
                  logger.error(s"Failed to parse an FI for subscriptionId: $subscriptionId errors: $errors")
                  Future.failed(NoFiDetailFound)
              }
            case UNPROCESSABLE_ENTITY if (Json.parse(response.body) \ "errorDetail" \ "errorCode").asOpt[String].contains("001") =>
              logger.warn(s"No FI found for subscriptionId: $subscriptionId")
              Future.successful(None)
            case _ =>
              Future.failed(NoFiDetailFound)
          }
      }

}
