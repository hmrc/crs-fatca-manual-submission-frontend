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

import connectors.{AddressLookupConnector, DatabaseConnector}
import controllers.actions.*
import forms.manual.sponsor.UKPostcodeFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.sponsor.{AddressLookupPage, SponsorNamePage, UKPostcodePage}
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.sponsor.UKPostcodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UKPostcodeController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  reportIdAction: ReportIdRequiredAction,
  formProvider: UKPostcodeFormProvider,
  addressLookupConnector: AddressLookupConnector,
  val controllerComponents: MessagesControllerComponents,
  view: UKPostcodeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>

      implicit val reportId: ReportId = request.reportId

      request.userAnswers
        .get(SponsorNamePage())
        .fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())) {
          name =>
            val preparedForm = request.userAnswers.get(UKPostcodePage()) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, mode, name))
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>

      implicit val reportId: ReportId = request.reportId
      request.userAnswers
        .get(SponsorNamePage())
        .fold(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))) {
          sponsorName =>
            val formReturned = form.bindFromRequest()
            formReturned
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, sponsorName))),
                postcode =>
                  addressLookupConnector.findByPostCode(postcode.toUpperCase).flatMap {
                    case Nil =>
                      val formError = formReturned.withError(FormError("value", List("uKPostcode.error.notfound")))
                      Future.successful(BadRequest(view(formError, mode, sponsorName)))
                    case address =>
                      for {
                        uaWithUKPostcode    <- Future.fromTry(request.userAnswers.setWithReportId(UKPostcodePage(), postcode))
                        uaWithAddressLookup <- Future.fromTry(uaWithUKPostcode.setWithReportId(AddressLookupPage(), address))
                        _                   <- repository.set(uaWithAddressLookup)
                      } yield Redirect(navigator.nextPage(UKPostcodePage(), mode, uaWithAddressLookup))
                  }
              )
        }
  }
}
