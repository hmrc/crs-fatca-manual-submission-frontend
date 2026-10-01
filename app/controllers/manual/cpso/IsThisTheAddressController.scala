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

package controllers.manual.cpso

import connectors.DatabaseConnector
import controllers.actions.*
import forms.manual.cpso.IsThisTheAddressFormProvider
import models.response.AddressLookup
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{AddressLookupPage, AddressUkPage, IsThisTheAddressPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.IsThisTheAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisTheAddressController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: IsThisTheAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsThisTheAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired() {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val regime                      = reportId.regime.value.toLowerCase()
      val form                        = formProvider(reportId.regime)

      val preparedForm = request.userAnswers
        .get(IsThisTheAddressPage(request.cpsoId, reportId))
        .fold(form)(form.fill)

      val address = request.userAnswers
        .get(AddressLookupPage(request.cpsoId, request.reportId))
        .flatMap(_.headOption.flatMap(_.toAddress))

      address
        .map(
          addr => Ok(view(preparedForm, mode, addr, request.cpsoName, regime))
        )
        .getOrElse {
          logger.warn("Missing individual or org name")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired().async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      val regime                      = reportId.regime.value.toLowerCase()
      val form                        = formProvider(reportId.regime)
      val address = request.userAnswers
        .get(AddressLookupPage(request.cpsoId, request.reportId))
        .flatMap(_.headOption.flatMap(_.toAddress))

      address
        .map {
          addr =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, addr, request.cpsoName, regime))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(IsThisTheAddressPage(request.cpsoId, reportId), value))
                    answersWithMaybeAddress <-
                      if (value) { Future.fromTry(updatedAnswers.setWithReportId(AddressUkPage(request.cpsoId, reportId), addr.ukAddress)) }
                      else { Future.successful(updatedAnswers) }
                    _ <- repository.set(answersWithMaybeAddress)
                  } yield Redirect(navigator.nextPage(IsThisTheAddressPage(request.cpsoId, reportId), mode, answersWithMaybeAddress))
              )
        }
        .getOrElse {
          logger.warn("Missing individual or org name")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }

  }
}
