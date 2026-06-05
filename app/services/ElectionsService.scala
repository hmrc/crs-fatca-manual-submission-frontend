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
import connectors.ElectionsConnector
import models.UserAnswers
import models.elections.ElectionsSent
import models.elections.RegimeType.{CRS, FATCA}
import models.requests.{CrsElectionsRequest, ElectionsSubmissionRequest, FatcaElectionsRequest}
import pages.*
import pages.Page.{electionCRSPages, electionFATCAPages}
import pages.elections.*
import repositories.SessionRepository
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.{ExecutionContext, Future}

class ElectionsService @Inject() (connector: ElectionsConnector, sessionRepository: SessionRepository)(implicit ec: ExecutionContext) {

  private case class RequestWithFiName(electionsSubmissionDetails: ElectionsSubmissionRequest, fiName: String)

  def submit(userAnswers: UserAnswers, reportingYear: Int)(implicit
    hc: HeaderCarrier
  ): Future[Unit] = for {
    requestBodyWithFIName <- toRequest(userAnswers, reportingYear)
    _                     <- connector.submit(requestBodyWithFIName.electionsSubmissionDetails)
    updatedUA             <- updateUA(userAnswers, reportingYear, requestBodyWithFIName.fiName)
    _                     <- sessionRepository.set(updatedUA)
  } yield ()

  private def updateUA(userAnswers: UserAnswers, reportingYear: Int, fiName: String): Future[UserAnswers] =
    Future.fromTry(userAnswers.get(CRSContractsPage) match {
      case Some(_) => userAnswers.removeAll(electionCRSPages).flatMap(_.set(ElectionsSentPage, ElectionsSent(CRS, reportingYear, fiName)))
      case None    => userAnswers.removeAll(electionFATCAPages).flatMap(_.set(ElectionsSentPage, ElectionsSent(FATCA, reportingYear, fiName)))
    })

  private def toRequest(userAnswers: UserAnswers, reportingYear: Int): Future[RequestWithFiName] =
    userAnswers.get(FiDetailsPage) match {
      case Some(fiDetail) =>
        Future.successful(
          RequestWithFiName(
            ElectionsSubmissionRequest(
              fiId = fiDetail.fiId,
              reportingPeriod = reportingYear.toString,
              crsDetails = buildCRSDetails(userAnswers),
              fatcaDetails = buildFATCADetails(userAnswers)
            ),
            fiDetail.fiName
          )
        )
      case None => Future.failed(InternalServerException("Unable to find FI Details"))
    }

  private def buildCRSDetails(userAnswers: UserAnswers): Option[CrsElectionsRequest] =
    for {
      hasContracts       <- userAnswers.get(CRSContractsPage)
      hasDormantAccounts <- userAnswers.get(CRSDormantAccountsPage)
      hasThresholds      <- userAnswers.get(CRSThresholdsPage)
    } yield {
      val hasCARF = userAnswers.get(CrsGrossProceedsPage)
      CrsElectionsRequest(hasCARF = hasCARF, hasContracts = hasContracts, hasDormantAccounts = hasDormantAccounts, hasThresholds = hasThresholds)
    }

  private def buildFATCADetails(userAnswers: UserAnswers): Option[FatcaElectionsRequest] =
    for {
      hasTreasuryRegulations <- userAnswers.get(IsUsTreasuryRegulatedPage)
      hasThresholds          <- userAnswers.get(IsApplyingThresholdsPage)
    } yield FatcaElectionsRequest(hasThresholds = hasThresholds, hasTreasuryRegulations = hasTreasuryRegulations)

}
