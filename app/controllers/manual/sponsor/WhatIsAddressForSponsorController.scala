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
import forms.manual.sponsor.WhatIsAddressForSponsorFormProvider
import models.response.AddressLookup
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.sponsor.{AddressLookupPage, SponsorNamePage, WhatIsAddressForSponsorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.sponsor.WhatIsAddressForSponsorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatIsAddressForSponsorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  formProvider: WhatIsAddressForSponsorFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatIsAddressForSponsorView
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
      } yield {
        val preparedForm = request.userAnswers.get(WhatIsAddressForSponsorPage()) match {
          case None => form
          case Some(savedAddress) =>
            addresses.find(_.toAddress.contains(savedAddress)) match {
              case Some(matched) => form.fill(matched.format)
              case None          => form
            }
        }

        val options: Seq[RadioItem] = addresses.map(
          address => RadioItem(content = Text(s"${address.formatRadios}"), value = Some(s"${address.format}"))
        )

        Ok(view(preparedForm, mode, sponsorName, options))
      }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      (for {
        sponsorName <- request.userAnswers.get(SponsorNamePage())
        addresses   <- request.userAnswers.get(AddressLookupPage())
      } yield {
        val options: Seq[RadioItem] = addresses.map(
          address => RadioItem(content = Text(s"${address.formatRadios}"), value = Some(s"${address.format}"))
        )

        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, sponsorName, options))),
            selectedValue =>
              addresses.find(_.format == selectedValue).flatMap(_.toAddress) match {
                case None =>
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url))
                case Some(address) =>
                  for {
                    updatedAnswers <-
                      Future.fromTry(request.userAnswers.setWithReportId(WhatIsAddressForSponsorPage(), address))
                    _ <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(WhatIsAddressForSponsorPage(), mode, updatedAnswers))
              }
          )
      }).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad().url)))
  }
}
