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
import models.SubmissionsConstants.RegimeType
import models.UserAnswers
import play.api.Logging
import play.api.http.Status.*
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

class DatabaseConnector @Inject() (client: HttpClientV2, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {
  private val url = config.crsFatcaManualBackendUrl

  def get()(implicit headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] =
    client
      .get(url"$url/crs-fatca-manual-submission/submissionList")
      .execute[HttpResponse](using readRaw, ec)
      .flatMap {
        response =>
          response.status match {
            case OK        => Future.successful(Some(response.json.as[UserAnswers]))
            case NOT_FOUND => Future.successful(None)
            case _         => Future.failed(Downstream_Error)
          }
      }
    
  //service layer
//  def getDocument(regime: RegimeType, reportingYear: Int)(implicit headerCarrier: HeaderCarrier) ={
//    val empty= List(Report(regime, reportingYear, None, None))
////    val x = get().map(_.map(_.get(ReportsPage).getOrElse(Reports(empty))))
//     val s =  get().map{maybeUserAnswers =>
//        maybeUserAnswers.flatMap{userAnswers=>
//          userAnswers.get(ReportsPage).map(_.allReports.filter(_.reportingYear == reportingYear).filter(_.regime == regime))
//        }
//        }

  
  

}
