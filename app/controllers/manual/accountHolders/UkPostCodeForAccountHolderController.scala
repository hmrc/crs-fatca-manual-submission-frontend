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

package controllers.manual.accountHolders

import connectors.{AddressLookupConnector, DatabaseConnector}
import controllers.actions.*
import forms.manual.accountHolders.UkPostCodeForAccountHolderFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{AddressLookupForAccountHolderPage, IndividualNamePage, UkPostCodeForAccountHolderPage}
import play.api.Logging
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.UkPostCodeForAccountHolderView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UkPostCodeForAccountHolderController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: UkPostCodeForAccountHolderFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UkPostCodeForAccountHolderView,
  addressLookupConnector: AddressLookupConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired() {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      val preparedForm = request.userAnswers
        .get(UkPostCodeForAccountHolderPage(request.accountHolderId, reportId))
        .fold(form)(form.fill)

      // Todo in the future we should cater for organisation-name too
      request.userAnswers
        .get(IndividualNamePage(request.accountHolderId)) match {
        case Some(individualName) =>
          val accountHolderName = s"${individualName.FirstName} ${individualName.LastName}".trim
          Ok(view(preparedForm, mode, accountHolderName))
        case None =>
          logger.warn("Mandatory individual name is missing from User Answers")
          Redirect(
            controllers.routes.JourneyRecoveryController.onPageLoad()
          )

      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.withReportIdRequiredAndAccountHolderIdRequired().async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId

      request.userAnswers
        .get(IndividualNamePage(request.accountHolderId)) match {
        case None =>
          logger.warn("Mandatory individual name is missing from User Answers")
          Future.successful(
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
          )
        case Some(individualName) =>
          val accountHolderName = s"${individualName.FirstName} ${individualName.LastName}".trim
          val formReturned      = form.bindFromRequest()

          formReturned
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, accountHolderName))),
              postcode =>
                addressLookupConnector.findByPostCode(postcode.toUpperCase).flatMap {
                  case Nil =>
                    val formError = formReturned.withError(FormError("value", List("uKPostcode.error.notfound")))
                    Future.successful(BadRequest(view(formError, mode, accountHolderName)))

                  case address =>
                    for {
                      updatedAnswers <- Future
                        .fromTry(request.userAnswers.setWithReportId(UkPostCodeForAccountHolderPage(request.accountHolderId, reportId), postcode))
                      uaWithAddressLookup <- Future
                        .fromTry(updatedAnswers.setWithReportId(AddressLookupForAccountHolderPage(request.accountHolderId, reportId), address))
                      _ <- repository.set(uaWithAddressLookup)
                    } yield Redirect(navigator.nextPage(UkPostCodeForAccountHolderPage(request.accountHolderId, reportId), mode, updatedAnswers))

                }
            )
      }

  }
}
