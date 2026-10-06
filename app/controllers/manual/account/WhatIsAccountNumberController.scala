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

package controllers.manual.account

import connectors.DatabaseConnector
import controllers.actions.*
import forms.manual.account.WhatIsAccountNumberFormProvider
import models.{Mode, NumberType, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.account.{NumberTypePage, WhatIsAccountNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.WhatIsAccountNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatIsAccountNumberController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: WhatIsAccountNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatIsAccountNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequired() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      request.userAnswers.get(NumberTypePage(request.accountId)) match {
        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

        case Some(numberType) =>
          val form = formProvider(numberType, reportId.regime)

          val preparedForm = request.userAnswers.get(WhatIsAccountNumberPage(request.accountId, request.reportId)) match {
            case None    => form
            case Some(v) => form.fill(v)
          }

          Ok(view(preparedForm, mode, numberType.toString.toUpperCase))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountIdRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      request.userAnswers.get(NumberTypePage(request.accountId)) match {
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case Some(numberType) =>
          val form = formProvider(numberType, reportId.regime)

          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, numberType.toString.toUpperCase))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(WhatIsAccountNumberPage(request.accountId, request.reportId), value))
                  _              <- repository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(WhatIsAccountNumberPage(request.accountId, request.reportId), mode, updatedAnswers))
            )
      }
  }
}
