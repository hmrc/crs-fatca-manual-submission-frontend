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
import forms.manual.cpso.AddressUkFormProvider
import models.SubmissionsConstants.{CRS, FATCA, RegimeType}
import models.viewModels.manual.cpso.CPSOId
import models.{Countries, Mode, ReportId, UkAddress, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{AddressLookupPage, AddressUkPage, SelectAddressPage, UkPostCodePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.AddressUkView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressUkController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: AddressUkFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddressUkView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val regime                      = reportId.regime.value.toLowerCase

      val preparedForm = resolveAddress(request.userAnswers, request.cpsoId).fold(form)(form.fill)

      Ok(view(preparedForm, mode, regime, request.cpsoName, countries(reportId.regime)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndCPSOIdRequiredAndCPSONameRequired().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val cpsoId                      = request.cpsoId
      val regime                      = reportId.regime.value.toLowerCase

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, regime, request.cpsoName, countries(reportId.regime)))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.setWithReportId(AddressUkPage(cpsoId, reportId), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AddressUkPage(cpsoId, reportId), mode, updatedAnswers))
        )
  }

  private def countries(regimeType: RegimeType) = regimeType match {
    case CRS   => Countries.crsUkTerritories
    case FATCA => Countries.ukTerritories
  }

  private def resolveAddress(userAnswers: UserAnswers, cpsoId: CPSOId)(implicit reportId: ReportId): Option[UkAddress] =
    userAnswers
      .get(AddressUkPage(cpsoId, reportId))
      .orElse(
        userAnswers
          .get(SelectAddressPage(cpsoId, reportId))
          .map(_.ukAddress)
      )
      .orElse(
        userAnswers
          .get(AddressLookupPage(cpsoId, reportId))
          .collect {
            case Seq(singleAddress) =>
              singleAddress.toAddress.map(_.ukAddress)
          }
          .flatten
      )
      .orElse(
        userAnswers.get(UkPostCodePage(cpsoId, reportId)).map(UkAddress.from)
      )

}
