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

package controllers

import controllers.actions._
import forms.TypeOfReportFormProvider
import javax.inject.Inject
import models.{Mode, ReportId}
import navigation.ManualSubmissionNavigator
import pages.TypeOfReportPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.DatabaseConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TypeOfReportView

import scala.concurrent.{ExecutionContext, Future}

class TypeOfReportController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: DatabaseConnector,
                                       navigator: ManualSubmissionNavigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       reportIdAction: ReportIdRequiredAction,
                                       formProvider: TypeOfReportFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: TypeOfReportView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction) {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      val preparedForm = request.userAnswers.get(TypeOfReportPage()) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen reportIdAction).async {
    implicit request =>
      implicit val reportId: ReportId = request.reportId
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(TypeOfReportPage(), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(TypeOfReportPage(), mode, updatedAnswers))
      )
  }
}
