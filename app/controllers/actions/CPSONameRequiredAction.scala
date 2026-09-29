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
import models.SubmissionsConstants.CRS
import models.manual.cpso.IndividualOrOrganisation
import models.requests.{CPSOIdRequest, CPSONameRequest}
import pages.manual.cpso.{IndividualNamePage, IndividualOrOrganisationPage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CPSONameRequiredActionImpl @Inject() (implicit
  val executionContext: ExecutionContext
) extends CPSONameRequiredAction
    with Logging {

  override protected def refine[A](request: CPSOIdRequest[A]): Future[Either[Result, CPSONameRequest[A]]] = {
    implicit val reportId: ReportId = request.reportId
    val ua                          = request.userAnswers
    val checkIndividualName = ua.get(IndividualNamePage(request.cpsoId)) match {
      case Some(name) =>
        Right(toRequest(request, name.fullName))
      case None =>
        logger.error("Unable to find IndividualName in User Answer")
        Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
    Future.successful {
      reportId.regime match {
        case CRS => checkIndividualName
        case _ =>
          ua.get(IndividualOrOrganisationPage(request.cpsoId)) match {
            case Some(IndividualOrOrganisation.Individual) =>
              checkIndividualName
            case _ =>
              logger.error("Unable to find IndividualOrOrganisation-Individual value")
              Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          }
      }
    }
  }

  private def toRequest[A](
    request: CPSOIdRequest[A],
    name: String
  ): CPSONameRequest[A] =
    CPSONameRequest(
      request = request.request,
      userId = request.userId,
      userAnswers = request.userAnswers,
      fatcaId = request.fatcaId,
      reportId = request.reportId,
      cpsoId = request.cpsoId,
      cpsoName = name
    )

}

trait CPSONameRequiredAction extends ActionRefiner[CPSOIdRequest, CPSONameRequest]
