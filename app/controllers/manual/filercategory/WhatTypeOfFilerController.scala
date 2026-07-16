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
import forms.manual.filercategory.WhatTypeOfFilerFormProvider
import models.manual.filercategory.WhatTypeOfFiler
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.FINamePage
import pages.manual.filercategory.WhatTypeOfFilerPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.filercategory.WhatTypeOfFilerView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatTypeOfFilerController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  formProvider: WhatTypeOfFilerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatTypeOfFilerView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[WhatTypeOfFiler] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      request.userAnswers
        .get(FINamePage())
        .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)) {
          fiName =>
            val preparedForm = request.userAnswers.get(WhatTypeOfFilerPage()) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, mode, fiName))
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      request.userAnswers
        .get(FINamePage())
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))) {
          fiName =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, fiName))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(WhatTypeOfFilerPage(), value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(WhatTypeOfFilerPage(), mode, updatedAnswers))
              )
        }
  }
}
