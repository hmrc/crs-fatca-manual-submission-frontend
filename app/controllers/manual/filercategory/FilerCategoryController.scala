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

package controllers.manual.filercategory

import connectors.DatabaseConnector
import controllers.actions.*
import models.Mode
import pages.manual.sponsor.SponsorNamePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FilerCategoryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: FrontendDataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: DatabaseConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>
      sessionRepository.get().map {
        maybeUa =>
          maybeUa.fold {
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          } {
            ua =>
              ua.get(SponsorNamePage()(request.reportId)) match {
                case Some(_) => Redirect(controllers.manual.filercategory.routes.WhatTypeOfFilerIsSponsorController.onPageLoad(mode))
                case None    => Redirect(controllers.manual.filercategory.routes.WhatTypeOfFilerController.onPageLoad(mode))
              }
          }
      }
  }

}
