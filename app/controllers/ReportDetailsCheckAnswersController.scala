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

package controllers

import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.TypeOfReportSummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ReportDetailsCheckAnswersView

import javax.inject.Inject

class ReportDetailsCheckAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ReportDetailsCheckAnswersView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val year = 2026 // GET PROPERLY
      val list = SummaryListViewModel(
        rows = Seq(
          TypeOfReportSummary.row(year, request.userAnswers)
        ).flatten
      )
      Ok(view(list))

  }

  def onSaveAndContinue: Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>
      Redirect(controllers.routes.UnderConstructionController.onPageLoad())

  }
}
