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
import forms.manual.cpso.CPSOIndividualDateOfBirthFormProvider
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.manual.cpso.{CPSOIndividualDateOfBirthPage, IndividualNamePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.manual.cpso.CPSOIndividualDateOfBirthView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CPSOIndividualDateOfBirthController @Inject()(
  override val messagesApi: MessagesApi,
  repository: DatabaseConnector,
  navigator: ManualSubmissionNavigator,
  actions: Actions,
  formProvider: CPSOIndividualDateOfBirthFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CPSOIndividualDateOfBirthView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndCPSOIdCreation() {
      implicit request =>

        implicit val reportId: ReportId = request.reportId

        val preparedForm =
          request.userAnswers
            .get(CPSOIndividualDateOfBirthPage(request.cpsoId))
            .fold(form)(form.fill)

        request.userAnswers
          .get(IndividualNamePage(request.cpsoId)) match {

          case Some(individualName) =>
            val cpsoName =
              s"${individualName.firstName} ${individualName.lastName}".trim

            Ok(
              view(
                preparedForm,
                mode,
                cpsoName
              )
            )

          case None =>
            logger.error("Mandatory individual name is missing from User Answers")
            Redirect(
              controllers.routes.JourneyRecoveryController.onPageLoad()
            )
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    actions.withReportIdRequiredAndCPSOIdCreation().async {
      implicit request =>

        implicit val reportId: ReportId = request.reportId

        request.userAnswers
          .get(IndividualNamePage(request.cpsoId)) match {

          case Some(individualName) =>
            val cpsoName =
              s"${individualName.firstName} ${individualName.lastName}".trim

            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(
                        formWithErrors,
                        mode,
                        cpsoName
                      )
                    )
                  ),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(
                      request.userAnswers.setWithReportId(
                        CPSOIndividualDateOfBirthPage(request.cpsoId),
                        value
                      )
                    )
                    _ <- repository.set(updatedAnswers)
                  } yield Redirect(
                    navigator.nextPage(
                      CPSOIndividualDateOfBirthPage(request.cpsoId),
                      mode,
                      updatedAnswers
                    )
                  )
              )
          case None =>
            logger.error("Mandatory individual name is missing from User Answers")
            Future.successful(
              Redirect(
                controllers.routes.JourneyRecoveryController.onPageLoad()
              )
            )
        }
    }
}
