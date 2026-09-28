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

import connectors.{AddressLookupConnector, DatabaseConnector}
import controllers.actions.*
import forms.manual.cpso.UkPostCodeFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{AddressLookupPage, UkPostCodePage}
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.UkPostCodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkPostCodeController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: UkPostCodeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UkPostCodeView,
  addressLookupConnector: AddressLookupConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val regime                      = reportId.regime.value.toLowerCase()

      val preparedForm = request.userAnswers.get(UkPostCodePage(request.cpsoId, reportId)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, regime, request.cpsoName))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val regime                      = reportId.regime.value.toLowerCase()
      val formReturned                = form.bindFromRequest()

      formReturned.fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, regime, request.cpsoName))),
        postcode =>
          addressLookupConnector.findByPostCode(postcode.toUpperCase).flatMap {
            case Nil =>
              val formError = formReturned.withError(FormError("value", List("uKPostcode.error.notfound")))
              Future.successful(BadRequest(view(formError, mode, regime, request.cpsoName)))
            case address =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(UkPostCodePage(request.cpsoId, reportId), postcode))
                uaWithAddressLookup <- Future
                  .fromTry(updatedAnswers.setWithReportId(AddressLookupPage(request.cpsoId, reportId), address))

                _ <- repository.set(uaWithAddressLookup)
              } yield Redirect(navigator.nextPage(UkPostCodePage(request.cpsoId, reportId), mode, uaWithAddressLookup))
          }
      )
  }
}
