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

package controllers.manual

import config.FrontendAppConfig
import controllers.actions.Actions
import forms.manual.HasFinishedCheckingFormProvider
import models.ServiceErrors.{MissingUTR, NoFiDetailFound}
import models.SubmissionsConstants.{CRS, RegimeType}
import models.response.FIDetail
import models.response.TINType.UTR
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.*
import play.twirl.api.Html
import services.ViewFIService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckDetailsForFinancialInstitutionUtil
import views.html.CheckDetailsForFinancialInstitutionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsForFinancialInstitutionController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  actions: Actions,
  view: CheckDetailsForFinancialInstitutionView,
  service: ViewFIService,
  util: CheckDetailsForFinancialInstitutionUtil,
  appConfig: FrontendAppConfig,
  formProvider: HasFinishedCheckingFormProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.withReportIdRequired().async {
    implicit request =>
      handleFiDetailErrors(
        service
          .getFIDetail(request.fatcaId, request.reportId.fiId)
          .map(
            fiDetail => Ok(renderPage(form, fiDetail, request.reportId.regime))
          ),
        request.reportId.fiId
      )
  }

  def onSubmit(): Action[AnyContent] = actions.withReportIdRequired().async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            handleFiDetailErrors(
              service
                .getFIDetail(request.fatcaId, request.reportId.fiId)
                .map(
                  fiDetail => BadRequest(renderPage(formWithErrors, fiDetail, request.reportId.regime))
                ),
              request.reportId.fiId
            ),
          _ => Future.successful(Redirect(controllers.manual.routes.SendAReportController.onPageLoad()))
        )
  }

  private def renderPage(form: Form[Boolean], fiDetail: FIDetail, regime: RegimeType)(implicit
    request: Request[_],
    messages: Messages
  ): Html = {
    val maybeUtr = fiDetail.TINDetails
      .getOrElse(Seq.empty)
      .find(_.TINType == UTR)
      .map(_.TIN)

    val utr = maybeUtr.getOrElse {
      if (regime == CRS) throw MissingUTR else ""
    }

    view(
      form,
      list = util.getRows(fiDetail, regime),
      regime = regime,
      fiName = fiDetail.FIName,
      fiIsUser = fiDetail.IsFIUser,
      hasGiin = fiDetail.GIIN.isDefined,
      utr = utr,
      linkUrl = appConfig.changeFiUrl(fiDetail.FIID)
    )
  }

  private def handleFiDetailErrors(result: Future[Result], fiId: String): Future[Result] =
    result.recover {
      case NoFiDetailFound =>
        logger.error(s"Failed to retrieve FIDetail for FiID $fiId")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case MissingUTR =>
        logger.error(s"Missing UTR from FIDetail for FiID $fiId when regime is CRS")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
}
