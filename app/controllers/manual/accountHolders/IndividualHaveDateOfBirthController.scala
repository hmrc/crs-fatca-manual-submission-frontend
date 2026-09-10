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
import forms.manual.accountHolders.IndividualHaveDateOfBirthFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.FINamePage
import pages.manual.accountHolders.{IndividualHaveDateOfBirthPage, IndividualNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.accountHolders.IndividualHaveDateOfBirthView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualHaveDateOfBirthController @Inject() (
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: IndividualHaveDateOfBirthFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IndividualHaveDateOfBirthView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountHolderIdRequired() {
      implicit request =>

        implicit val reportId: ReportId = request.reportId

        val preparedForm =
          request.userAnswers
            .get(IndividualHaveDateOfBirthPage(request.accountHolderId))
            .fold(form)(form.fill)

        (
          request.userAnswers.get(FINamePage()),
          request.userAnswers.get(IndividualNamePage(request.accountHolderId))
        ) match {
          case (Some(fiName), Some(individualName)) =>
            val accountHolderName =
              s"${individualName.FirstName} ${individualName.LastName}".trim
            Ok(
              view(
                preparedForm,
                mode,
                fiName,
                accountHolderName,
                reportId.regime
              )
            )

          case _ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndAccountHolderIdRequired().async {
      implicit request =>

        implicit val reportId: ReportId = request.reportId

        (
          request.userAnswers.get(FINamePage()),
          request.userAnswers.get(IndividualNamePage(request.accountHolderId))
        ) match {
          case (Some(fiName), Some(individualName)) =>
            val accountHolderName =
              s"${individualName.FirstName} ${individualName.LastName}".trim
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(
                        formWithErrors,
                        mode,
                        fiName,
                        accountHolderName,
                        reportId.regime
                      )
                    )
                  ),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(
                      request.userAnswers.setWithReportId(
                        IndividualHaveDateOfBirthPage(request.accountHolderId),
                        value
                      )
                    )
                    _ <- repository.set(updatedAnswers)
                  } yield Redirect(
                    navigator.nextPage(
                      IndividualHaveDateOfBirthPage(request.accountHolderId),
                      mode,
                      updatedAnswers
                    )
                  )
              )

          case _ =>
            Future.successful(
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            )
        }
    }
}
