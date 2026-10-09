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

package controllers.actions

import controllers.routes
import models.ReportId
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.requests.{AccountHolderIdRequest, AccountHolderNameRequest}
import pages.manual.accountHolders.{AccountHolderIndividualNamePage, AccountHolderOrganisationNamePage, IndividualOrOrganisationPage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountHolderNameRequiredActionImpl @Inject() (implicit
  val executionContext: ExecutionContext
) extends AccountHolderNameRequiredAction
    with Logging {

  override protected def refine[A](
    request: AccountHolderIdRequest[A]
  ): Future[Either[Result, AccountHolderNameRequest[A]]] =
    Future.successful {

      implicit val reportId: ReportId = request.reportId

      val accountHolderName =
        request.userAnswers.get(IndividualOrOrganisationPage(request.accountHolderId)) match {
          case Some(Individual) =>
            request.userAnswers
              .get(AccountHolderIndividualNamePage(request.accountHolderId))
              .map(_.fullName)

          case Some(Organisation) =>
            request.userAnswers
              .get(AccountHolderOrganisationNamePage(request.accountHolderId))

          case None =>
            None
        }

      accountHolderName match {
        case Some(name) =>
          Right(toAccountHolderNameRequest(request, name))

        case None =>
          logger.error("Unable to find account holder name in User Answers")
          Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  private def toAccountHolderNameRequest[A](
    request: AccountHolderIdRequest[A],
    accountHolderName: String
  ): AccountHolderNameRequest[A] =
    AccountHolderNameRequest(
      request = request.request,
      userId = request.userId,
      userAnswers = request.userAnswers,
      fatcaId = request.fatcaId,
      reportId = request.reportId,
      accountHolderId = request.accountHolderId,
      accountHolderName = accountHolderName
    )
}

trait AccountHolderNameRequiredAction extends ActionRefiner[AccountHolderIdRequest, AccountHolderNameRequest]
