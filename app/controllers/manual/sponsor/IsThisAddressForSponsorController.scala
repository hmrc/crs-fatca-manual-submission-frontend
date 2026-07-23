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

package controllers.manual.sponsor

import connectors.DatabaseConnector
import controllers.actions.*
import forms.manual.sponsor.IsThisAddressForSponsorFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.sponsor.{AddressLookupPage, IsThisAddressForSponsorPage, SponsorNamePage, WhatIsAddressForSponsorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.sponsor.IsThisAddressForSponsorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisAddressForSponsorController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  formProvider: IsThisAddressForSponsorFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsThisAddressForSponsorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      (for {
        sponsorName <- request.userAnswers.get(SponsorNamePage())
        addresses   <- request.userAnswers.get(AddressLookupPage())
        address     <- addresses.headOption.flatMap(_.toAddress)
      } yield {
        val preparedForm = request.userAnswers.get(IsThisAddressForSponsorPage()) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, mode, address, sponsorName))
      }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      (for {
        sponsorName <- request.userAnswers.get(SponsorNamePage())
        addresses   <- request.userAnswers.get(AddressLookupPage())
        address     <- addresses.headOption.flatMap(_.toAddress)
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, address, sponsorName))),
          value =>
            for {
              answersWithBoolean <- Future.fromTry(request.userAnswers.setWithReportId(IsThisAddressForSponsorPage(), value))
              answersWithMaybeAddress <-
                if (value) { Future.fromTry(answersWithBoolean.setWithReportId(WhatIsAddressForSponsorPage(), address)) }
                else { Future.successful(answersWithBoolean) }
              _ <- repository.set(answersWithMaybeAddress)
            } yield Redirect(navigator.nextPage(IsThisAddressForSponsorPage(), mode, answersWithMaybeAddress))
        )).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)))
  }
}
