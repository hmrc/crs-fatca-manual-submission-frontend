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

import controllers.actions.*
import models.SubmissionsConstants.{CRS, FATCA}
import models.TypeOfReport
import models.viewModels.SendAReportSections
import pages.manual.FINamePage
import pages.manual.reportdetails.DraftReportTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SendAReportService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SendAReportView

import javax.inject.Inject

class SendAReportController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  service: SendAReportService,
  view: SendAReportView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>
      request.userAnswers.get(DraftReportTypePage(request.reportId)) match {
        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

        case Some(reportType) =>
          val isNilReport    = reportType == TypeOfReport.NilReport
          val accountsStatus = service.accountStatus(request.userAnswers, request.reportId)

          val sections = SendAReportSections(
            financialInstitutionDetails = Option.unless(isNilReport && request.reportId.regime == CRS)(
              service.reportingFIStatus(request.userAnswers, request.reportId)
            ),
            sponsorDetails = Option.when(request.reportId.regime == FATCA)(
              service.sponsorStatus(request.userAnswers, request.reportId)
            ),
            filerCategory = Option.when(request.reportId.regime == FATCA)(
              service.fillerStatus(request.userAnswers, request.reportId)
            ),
            accounts = Option.unless(isNilReport)(accountsStatus),
            accountHolders = Option.unless(isNilReport)(
              service.accountHolderStatus(request.userAnswers, request.reportId, accountsStatus)
            ),
            controllingPersons = Option
              .unless(isNilReport)(
                service.cpsoStatus(request.userAnswers, request.reportId)
              )
              .flatten,
            userAnswers = request.userAnswers,
            reportId = request.reportId,
            isNilReport = isNilReport,
            fiName = request.userAnswers.get(FINamePage()(request.reportId)).getOrElse("")
          )

          Ok(view(sections))
      }
  }
}
