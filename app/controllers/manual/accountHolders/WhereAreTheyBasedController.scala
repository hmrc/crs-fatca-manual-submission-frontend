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

import connectors.DatabaseConnector
import controllers.actions.*
import forms.manual.accountHolders.WhereAreTheyBasedFormProvider
import models.manual.accountHolders.IndividualOrOrganisation.{Individual, Organisation}
import models.viewModels.AccountHolderId
import models.{Mode, ReportId, UserAnswers}
import navigation.ManualSubmissionNavigator
import pages.manual.accountHolders.{AccountHolderIndividualNamePage, AccountHolderOrganisationNamePage, IndividualOrOrganisationPage, WhereAreTheyBasedPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.WhereAreTheyBasedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhereAreTheyBasedController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: WhereAreTheyBasedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhereAreTheyBasedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  private def getAccountHolderName(
    userAnswers: UserAnswers,
    accountHolderId: AccountHolderId
  )(implicit reportId: ReportId): Option[String] =
    userAnswers.get(IndividualOrOrganisationPage(accountHolderId)) match {
      case Some(Individual) =>
        userAnswers
          .get(AccountHolderIndividualNamePage(accountHolderId))
          .map(_.fullName)

      case Some(Organisation) =>
        userAnswers
          .get(AccountHolderOrganisationNamePage(accountHolderId))

      case None =>
        None
    }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountHolderIdRequired() {
      implicit request =>

        implicit val reportId: ReportId = request.reportId

        getAccountHolderName(request.userAnswers, request.accountHolderId) match {
          case Some(name) =>
            val preparedForm =
              request.userAnswers.get(WhereAreTheyBasedPage(request.accountHolderId)) match {
                case None        => form
                case Some(value) => form.fill(value)
              }

            Ok(view(preparedForm, mode, name))

          case None =>
            logger.error("Account holder name is missing")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountHolderIdRequired().async {
      implicit request =>

        implicit val reportId: ReportId = request.reportId

        getAccountHolderName(request.userAnswers, request.accountHolderId) match {
          case Some(name) =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(view(formWithErrors, mode, name))
                  ),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(
                      request.userAnswers.setWithReportId(
                        WhereAreTheyBasedPage(request.accountHolderId),
                        value
                      )
                    )
                    _ <- repository.set(updatedAnswers)
                  } yield Redirect(
                    navigator.nextPage(
                      WhereAreTheyBasedPage(request.accountHolderId),
                      mode,
                      updatedAnswers
                    )
                  )
              )

          case None =>
            logger.error("Account holder name is missing")
            Future.successful(
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            )
        }
    }
}
