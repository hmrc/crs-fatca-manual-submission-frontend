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
import forms.manual.account.WhatAccountTypeFormProvider
import models.manual.account.WhatAccountType
import models.{Mode, NumberType, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.account.{NumberTypePage, WhatAccountTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.account.WhatAccountTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatAccountTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  accountCRSOnlyFilterAction: AccountCRSOnlyFilterAction,
  formProvider: WhatAccountTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatAccountTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (actions.withReportIdRequiredAndAccountIdRequired() andThen accountCRSOnlyFilterAction).async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      request.userAnswers.get(NumberTypePage(request.accountId)) match {
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        case Some(numberType) =>
          val preparedForm = request.userAnswers.get(WhatAccountTypePage(request.accountId)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          numberType match {
            case NumberType.Iban | NumberType.Semp =>
              Future
                .fromTry(
                  request.userAnswers.setWithReportId(WhatAccountTypePage(request.accountId), WhatAccountType.Depository)
                )
                .map {
                  updatedAnswers =>
                    Redirect(navigator.nextPage(WhatAccountTypePage(request.accountId), mode, updatedAnswers))
                }

            case _ =>
              val items: Seq[RadioItem] = WhatAccountType.options(numberType, reportId.reportingYear)
              Future.successful(Ok(view(preparedForm, mode, items)))
          }
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (actions.withReportIdRequiredAndAccountIdRequired() andThen accountCRSOnlyFilterAction).async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      (for {
        numberType <- request.userAnswers.get(NumberTypePage(request.accountId))
      } yield {
        val items: Seq[RadioItem] = WhatAccountType.options(numberType, reportId.reportingYear)

        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, items))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(WhatAccountTypePage(request.accountId), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(WhatAccountTypePage(request.accountId), mode, updatedAnswers))
          )
      }).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}
