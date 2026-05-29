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

package services

import com.google.inject.Inject
import connectors.FinancialInstitutionsConnector
import models.FIDetail
import models.ServiceErrors.NoFiDetailFound
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.{ExecutionContext, Future}

class ViewFIService @Inject() (connector: FinancialInstitutionsConnector)(implicit ec: ExecutionContext) {

  def getFIDetail(subId: String, fiId: String)(using
    hc: HeaderCarrier
  ): Future[FIDetail] =
    connector.viewFi(subId, fiId).flatMap {
      maybeFi =>
        maybeFi.map(Future.successful).getOrElse(Future.failed(NoFiDetailFound))
    }

}
