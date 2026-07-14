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
import forms.manual.filercategory.WhatTypeOfFilerIsSponsorFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.filercategory.WhatTypeOfFilerIsSponsorPage
import pages.manual.sponsor.SponsorNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.filercategory.WhatTypeOfFilerIsSponsorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatTypeOfFilerIsSponsorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  formProvider: WhatTypeOfFilerIsSponsorFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatTypeOfFilerIsSponsorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      request.userAnswers
        .get(SponsorNamePage())
        .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)) {
          sponsorName =>
            val preparedForm = request.userAnswers.get(WhatTypeOfFilerIsSponsorPage()) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, mode, sponsorName))
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      request.userAnswers
        .get(SponsorNamePage())
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))) {
          sponsorName =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, sponsorName))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatTypeOfFilerIsSponsorPage(), value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(WhatTypeOfFilerIsSponsorPage(), mode, updatedAnswers))
              )
        }
  }
}
