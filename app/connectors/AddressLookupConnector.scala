/*
 * Copyright 2023 HM Revenue & Customs
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
import models.ServiceErrors.AddressLookup_Error
import models.SubmissionsConstants.CRFA
import models.requests.LookupAddressByPostcode
import models.response.AddressLookup
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressLookupConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig) extends Logging {

  def findByPostCode(postCode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[AddressLookup]] = {

    val addressLookupUrl = url"${config.addressLookUpUrl}/lookup"

    implicit val reads: Reads[Seq[AddressLookup]] = AddressLookup.addressesLookupReads

    val lookupAddressByPostcode = LookupAddressByPostcode(postCode, None)

    http
      .post(addressLookupUrl)
      .setHeader("X-Hmrc-Origin" -> CRFA.toString)
      .withBody(Json.toJson(lookupAddressByPostcode))
      .execute[HttpResponse]
      .flatMap {
        case response if response.status == OK =>
          Future.successful(
            response.json
              .as[Seq[AddressLookup]]
              .filterNot(
                address => address.addressLine1.isEmpty && address.addressLine2.isEmpty
              )
              .sorted
          )
        case response =>
          logger.error(s"Address Lookup failed with status ${response.status} Response body: ${response.body}")
          Future.failed(AddressLookup_Error)
      } recover {
      case e: Exception =>
        logger.error("Exception in Address Lookup", e)
        throw e
    }
  }

}
